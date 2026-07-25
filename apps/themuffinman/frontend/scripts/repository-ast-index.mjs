#!/usr/bin/env node

import fs from 'node:fs';
import path from 'node:path';
import process from 'node:process';
import { parse } from '@babel/parser';

const appRoot = path.resolve(new URL('../..', import.meta.url).pathname);
const root = path.resolve(appRoot, '../..');
const sourceRoot = path.join(appRoot, 'frontend', 'src');
const output = process.argv.includes('--json');

function filesUnder(directory) {
  if (!fs.existsSync(directory)) return [];
  return fs.readdirSync(directory, { withFileTypes: true }).flatMap((entry) => {
    const file = path.join(directory, entry.name);
    if (entry.isDirectory()) return filesUnder(file);
    return /\.(vue|ts|js|mjs)$/.test(entry.name) ? [file] : [];
  });
}

function scriptContent(file, source) {
  if (!file.endsWith('.vue')) return { source, language: file.endsWith('.ts') ? 'typescript' : 'javascript' };
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

function inspect(file) {
  const raw = fs.readFileSync(file, 'utf8');
  const { source, language } = scriptContent(file, raw);
  const result = {
    path: path.relative(root, file),
    imports: [],
    symbols: [],
    nodeTypes: {},
  };
  if (!source.trim()) return result;

  let tree;
  try {
    tree = parse(source, {
      sourceType: 'unambiguous',
      plugins: [
        language === 'typescript' ? 'typescript' : 'jsx',
        'decorators-legacy',
        'classProperties',
        'objectRestSpread',
        'optionalChaining',
        'nullishCoalescingOperator',
        'importMeta',
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
    if (node.type === 'FunctionDeclaration' || node.type === 'ClassDeclaration' || node.type === 'InterfaceDeclaration' || node.type === 'TSTypeAliasDeclaration') {
      const name = identifierName(node.id);
      if (name) result.symbols.push({ kind: node.type, name });
    }
    if (node.type === 'VariableDeclarator') {
      const name = identifierName(node.id);
      if (name) result.symbols.push({ kind: 'Variable', name });
    }
    for (const [key, value] of Object.entries(node)) {
      if (key === 'loc' || key === 'start' || key === 'end') continue;
      if (Array.isArray(value)) value.forEach((child) => stack.push(child));
      else stack.push(value);
    }
  }
  result.imports = [...new Set(result.imports)].sort();
  result.symbols = result.symbols.filter((symbol, index, list) => list.findIndex((item) => item.kind === symbol.kind && item.name === symbol.name) === index);
  return result;
}

const entries = filesUnder(sourceRoot).map(inspect);
const report = {
  version: 1,
  kind: 'frontend_ast_index',
  generatedAt: new Date().toISOString(),
  sourceRoot: path.relative(root, sourceRoot),
  files: entries,
  summary: {
    files: entries.length,
    parseErrors: entries.filter((entry) => entry.parseError).length,
    imports: entries.reduce((sum, entry) => sum + entry.imports.length, 0),
    symbols: entries.reduce((sum, entry) => sum + entry.symbols.length, 0),
  },
};

if (output) process.stdout.write(`${JSON.stringify(report)}\n`);
else console.log(`Frontend AST index: ${report.summary.files} files, ${report.summary.symbols} symbols, ${report.summary.imports} imports, ${report.summary.parseErrors} parse errors`);

if (report.summary.parseErrors > 0) process.exitCode = 1;
