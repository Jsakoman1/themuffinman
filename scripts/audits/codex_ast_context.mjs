#!/usr/bin/env node

import fs from "node:fs";
import path from "node:path";
import { createRequire } from "node:module";
import { spawnSync } from "node:child_process";

const requireFromRepo = createRequire(import.meta.url);

function readStdin() {
  return new Promise((resolve, reject) => {
    let chunks = "";
    process.stdin.setEncoding("utf8");
    process.stdin.on("data", (chunk) => {
      chunks += chunk;
    });
    process.stdin.on("end", () => resolve(chunks));
    process.stdin.on("error", reject);
  });
}

function loadModule(request, repoRoot) {
  const frontendModules = path.join(repoRoot, "apps/themuffinman/frontend/node_modules");
  return requireFromRepo(requireFromRepo.resolve(request, { paths: [frontendModules] }));
}

function parseScriptAst(parser, source, filePath, startLine = 1) {
  return parser.parse(source, {
    sourceType: "module",
    sourceFilename: filePath,
    startLine,
    errorRecovery: true,
    plugins: [
      "typescript",
      "jsx",
      "decorators-legacy",
      "classProperties",
      "classPrivateProperties",
      "classPrivateMethods",
      "topLevelAwait"
    ]
  });
}

function pushSpan(spans, span) {
  if (!span?.symbol || !span?.lineStart || !span?.lineEnd) return;
  spans.push(span);
}

function topLevelScriptSpans(ast, filePath, source, sourceStartLine = 1) {
  const spans = [];
  const body = ast?.program?.body ?? [];
  for (const node of body) {
    collectNodeSpan(node, filePath, source, spans, sourceStartLine);
  }
  return spans;
}

function collectNodeSpan(node, filePath, source, spans, sourceStartLine) {
  if (!node?.loc) return;

  switch (node.type) {
    case "ExportNamedDeclaration":
    case "ExportDefaultDeclaration":
      if (node.declaration) collectNodeSpan(node.declaration, filePath, source, spans, sourceStartLine);
      return;
    case "FunctionDeclaration":
      pushSpan(spans, makeSpan("script-function", node.id?.name || "anonymousFunction", filePath, node, source, sourceStartLine));
      return;
    case "ClassDeclaration":
      pushSpan(spans, makeSpan("script-class", node.id?.name || "AnonymousClass", filePath, node, source, sourceStartLine));
      for (const member of node.body?.body ?? []) {
        if (member?.type === "ClassMethod" || member?.type === "ClassPrivateMethod") {
          const keyName = member.key?.name || member.key?.id?.name || "anonymousMethod";
          pushSpan(spans, makeSpan("script-method", `${node.id?.name || "AnonymousClass"}.${keyName}`, filePath, member, source, sourceStartLine));
        }
      }
      return;
    case "VariableDeclaration":
      for (const declaration of node.declarations ?? []) {
        const variableName = declaration.id?.name;
        if (variableName) {
          pushSpan(spans, makeSpan("script-variable", variableName, filePath, declaration, source, sourceStartLine));
        }
      }
      return;
    case "TSInterfaceDeclaration":
      pushSpan(spans, makeSpan("script-interface", node.id?.name || "AnonymousInterface", filePath, node, source, sourceStartLine));
      return;
    case "TSTypeAliasDeclaration":
      pushSpan(spans, makeSpan("script-type", node.id?.name || "AnonymousType", filePath, node, source, sourceStartLine));
      return;
    default:
      return;
  }
}

function makeSpan(type, symbol, filePath, node, source, sourceStartLine = 1) {
  const start = node.loc.start.line;
  const end = node.loc.end.line;
  const relativeStart = Math.max(start - sourceStartLine, 0);
  const relativeEnd = Math.max(end - sourceStartLine + 1, relativeStart + 1);
  const snippet = source
    .split("\n")
    .slice(relativeStart, relativeEnd)
    .map((line) => line.trimEnd())
    .filter((line) => line.trim().length > 0)
    .slice(0, 20)
    .join("\n");
  return {
    type,
    symbol,
    file: filePath,
    lineStart: start,
    lineEnd: end,
    snippet: snippet.length > 1200 ? `${snippet.slice(0, 1197)}...` : snippet
  };
}

function changedSpanFilter(spans, changedLines) {
  const changed = new Set(changedLines);
  return spans.filter((span) => {
    for (let line = span.lineStart; line <= span.lineEnd; line += 1) {
      if (changed.has(line)) return true;
    }
    return false;
  });
}

function vueTemplateSpan(vueCompiler, source, filePath, changedLines) {
  const parsed = vueCompiler.parse(source, { filename: filePath });
  const descriptor = parsed.descriptor;
  const spans = [];
  if (descriptor.template?.ast?.loc) {
    const start = descriptor.template.ast.loc.start.line;
    const end = descriptor.template.ast.loc.end.line;
    const overlap = changedLines.some((line) => line >= start && line <= end);
    if (overlap) {
      spans.push(makeSpan("vue-template", `${path.basename(filePath, ".vue")}Template`, filePath, descriptor.template.ast, source, 1));
    }
  }
  return { descriptor, spans };
}

function parseVue(parser, vueCompiler, source, filePath, changedLines) {
  const { descriptor, spans } = vueTemplateSpan(vueCompiler, source, filePath, changedLines);
  const scriptBlocks = [descriptor.script, descriptor.scriptSetup].filter(Boolean);
  for (const block of scriptBlocks) {
    const ast = parseScriptAst(parser, block.content, filePath, block.loc.start.line);
    spans.push(...changedSpanFilter(topLevelScriptSpans(ast, filePath, block.content, block.loc.start.line), changedLines));
  }
  return spans;
}

function ensureJavaHelper(repoRoot) {
  const source = path.join(repoRoot, "scripts/audits/CodexJavaAstContext.java");
  const outDir = path.join(repoRoot, "docs/generated/local-tooling/.cache/codex-java-ast");
  const mainClass = path.join(outDir, "CodexJavaAstContext.class");
  fs.mkdirSync(outDir, { recursive: true });
  const needsCompile =
    !fs.existsSync(mainClass) ||
    fs.statSync(mainClass).mtimeMs < fs.statSync(source).mtimeMs;
  if (!needsCompile) return outDir;

  const compile = spawnSync("javac", ["-d", outDir, source], {
    cwd: repoRoot,
    encoding: "utf8"
  });
  if (compile.status !== 0) {
    throw new Error(compile.stderr || compile.stdout || "javac compilation failed");
  }
  return outDir;
}

function parseJava(repoRoot, filePath, changedLines) {
  const outDir = ensureJavaHelper(repoRoot);
  const run = spawnSync(
    "java",
    [
      "-cp",
      outDir,
      "CodexJavaAstContext",
      repoRoot,
      filePath,
      changedLines.join(",")
    ],
    {
      cwd: repoRoot,
      encoding: "utf8"
    }
  );
  if (run.status !== 0) {
    throw new Error(run.stderr || run.stdout || "java AST helper failed");
  }
  return JSON.parse(run.stdout);
}

async function main() {
  const raw = await readStdin();
  const input = JSON.parse(raw);
  const parser = loadModule("@babel/parser", input.repoRoot);
  const vueCompiler = loadModule("@vue/compiler-sfc", input.repoRoot);
  const result = [];

  for (const file of input.files ?? []) {
    const absolutePath = path.join(input.repoRoot, file.path);
    const source = fs.readFileSync(absolutePath, "utf8");
    const changedLines = Array.isArray(file.changedLines) ? file.changedLines : [];
    const extension = path.extname(file.path);
    let spans = [];

    if (extension === ".vue") {
      spans = parseVue(parser, vueCompiler, source, file.path, changedLines);
    } else if (extension === ".java") {
      spans = parseJava(input.repoRoot, file.path, changedLines);
    } else if ([".ts", ".tsx", ".js", ".jsx", ".mjs"].includes(extension)) {
      const ast = parseScriptAst(parser, source, file.path, 1);
      spans = changedSpanFilter(topLevelScriptSpans(ast, file.path, source, 1), changedLines);
    }

    result.push({
      file: file.path,
      parserBacked: true,
      spans
    });
  }

  process.stdout.write(`${JSON.stringify({ files: result }, null, 2)}\n`);
}

main().catch((error) => {
  process.stderr.write(`${error.stack || error.message}\n`);
  process.exit(1);
});
