#!/usr/bin/env node

import { createRequire } from 'node:module';
import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';

function fail(message) {
  throw new Error(message);
}

function readArguments(args) {
  const result = { sourceRoots: [] };
  for (let index = 0; index < args.length; index += 1) {
    const value = args[index];
    if (value === '--project-root' && args[index + 1]) result.projectRoot = args[++index];
    else if (value === '--source-root' && args[index + 1]) result.sourceRoots.push(args[++index]);
    else if (value === '--package-root' && args[index + 1]) result.packageRoot = args[++index];
    else if (value === '--parser-module' && args[index + 1]) result.parserModule = args[++index];
    else fail('usage: node typescript-vue-ast-index.mjs --project-root <path> --source-root <path> [...] --package-root <path> --parser-module <module>');
  }
  if (!result.projectRoot || !result.packageRoot || !result.parserModule || result.sourceRoots.length === 0) fail('project root, source roots, package root, and parser module are required');
  return result;
}

function contained(projectRoot, configuredPath, predicate, label) {
  const resolved = path.resolve(projectRoot, configuredPath);
  if (!resolved.startsWith(`${projectRoot}${path.sep}`) || !predicate(resolved)) fail(`${label} is invalid or missing: ${configuredPath}`);
  return resolved;
}

function filesUnder(directory) {
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const file = path.join(directory, entry.name);
    if (entry.isDirectory()) return filesUnder(file);
    return /\.(vue|ts|tsx|js|mjs)$/.test(entry.name) ? [file] : [];
  });
}

function scriptContent(file, source) {
  if (!file.endsWith('.vue')) return { source, language: /\.(ts|tsx)$/.test(file) ? 'typescript' : 'javascript' };
  const match = source.match(/<script\b([^>]*)>([\s\S]*?)<\/script>/i);
  if (!match) return { source: '', language: 'javascript' };
  return { source: match[2], language: /lang=["']ts["']/.test(match[1]) ? 'typescript' : 'javascript' };
}

function identifierName(node) {
  if (!node) return null;
  if (node.type === 'Identifier') return node.name;
  if (node.type === 'StringLiteral') return node.value;
  return null;
}

function inspect(file, projectRoot, parse) {
  const raw = fs.readFileSync(file, 'utf8');
  const { source, language } = scriptContent(file, raw);
  const result = { path: path.relative(projectRoot, file), imports: [], symbols: [], nodeTypes: {} };
  if (!source.trim()) return result;
  let tree;
  try {
    tree = parse(source, {
      sourceType: 'unambiguous',
      plugins: [
        language === 'typescript' ? 'typescript' : 'jsx', 'decorators-legacy', 'classProperties', 'objectRestSpread',
        'optionalChaining', 'nullishCoalescingOperator', 'importMeta',
      ],
    });
  } catch (error) {
    result.parseError = error.message;
    return result;
  }
  const stack = [tree];
  while (stack.length) {
    const node = stack.pop();
    if (!node || typeof node !== 'object') continue;
    if (node.type) result.nodeTypes[node.type] = (result.nodeTypes[node.type] || 0) + 1;
    if (node.type === 'ImportDeclaration' && node.source?.value) result.imports.push(node.source.value);
    if (['FunctionDeclaration', 'ClassDeclaration', 'InterfaceDeclaration', 'TSTypeAliasDeclaration'].includes(node.type)) {
      const name = identifierName(node.id);
      if (name) result.symbols.push({ kind: node.type, name });
    }
    if (node.type === 'VariableDeclarator') {
      const name = identifierName(node.id);
      if (name) result.symbols.push({ kind: 'Variable', name });
    }
    for (const [key, value] of Object.entries(node)) {
      if (['loc', 'start', 'end'].includes(key)) continue;
      if (Array.isArray(value)) value.forEach((child) => stack.push(child));
      else stack.push(value);
    }
  }
  result.imports = [...new Set(result.imports)].sort();
  result.symbols = result.symbols.filter((symbol, index, list) => list.findIndex((item) => item.kind === symbol.kind && item.name === symbol.name) === index);
  return result;
}

const config = readArguments(process.argv.slice(2));
const projectRoot = path.resolve(config.projectRoot);
const sourceRoots = config.sourceRoots.map((sourceRoot) => contained(projectRoot, sourceRoot, (candidate) => fs.statSync(candidate).isDirectory(), 'source root'));
const packageRoot = contained(projectRoot, config.packageRoot, fs.statSync.bind(fs), 'package root');
if (!fs.statSync(packageRoot).isDirectory()) fail(`package root is not a directory: ${config.packageRoot}`);
const requireFromConsumer = createRequire(path.join(packageRoot, 'package.json'));
const parser = requireFromConsumer(config.parserModule);
if (typeof parser.parse !== 'function') fail(`parser module has no parse function: ${config.parserModule}`);
const entries = sourceRoots.flatMap(filesUnder).sort().map((file) => inspect(file, projectRoot, parser.parse));
const report = {
  version: 1,
  kind: 'typescript_vue_ast_index',
  sourceRoots: sourceRoots.map((sourceRoot) => path.relative(projectRoot, sourceRoot)),
  parser: config.parserModule,
  files: entries,
  summary: {
    files: entries.length,
    parseErrors: entries.filter((entry) => entry.parseError).length,
    imports: entries.reduce((sum, entry) => sum + entry.imports.length, 0),
    symbols: entries.reduce((sum, entry) => sum + entry.symbols.length, 0),
  },
};
process.stdout.write(`${JSON.stringify(report)}\n`);
if (report.summary.parseErrors > 0) process.exitCode = 1;
