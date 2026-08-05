#!/usr/bin/env node

import path from 'node:path';
import process from 'node:process';
import { spawnSync } from 'node:child_process';

const appRoot = path.resolve(new URL('../..', import.meta.url).pathname);
const root = path.resolve(appRoot, '../..');
const tool = path.join(root, 'dora', 'tools', 'typescript-vue-ast-index.mjs');
const result = spawnSync('node', [tool, '--project-root', root, '--source-root', 'apps/themuffinman/frontend/src', '--package-root', 'apps/themuffinman/frontend', '--parser-module', '@babel/parser'], { encoding: 'utf8' });
if (result.error) throw result.error;
process.stdout.write(result.stdout);
process.stderr.write(result.stderr);
process.exitCode = result.status ?? 1;
