import {readdirSync, readFileSync} from "node:fs"
import {resolve, relative} from "node:path"
import {fileURLToPath} from "node:url"

const frontendRoot = resolve(fileURLToPath(new URL("..", import.meta.url)))
const shellRoot = resolve(frontendRoot, "src/modules/app-shell")
const baseCss = readFileSync(resolve(frontendRoot, "src/styles/base.css"), "utf8")
const scope = process.argv.find(value => value.startsWith("--scope="))?.split("=", 2)[1]

if (scope !== "all") {
  console.error("Usage: node scripts/audit-ui-style-standardization.mjs --scope=all")
  process.exit(2)
}

const walkVue = (directory) => readdirSync(directory, {withFileTypes: true}).flatMap(entry => {
  const path = resolve(directory, entry.name)
  return entry.isDirectory() ? walkVue(path) : entry.name.endsWith(".vue") ? [path] : []
})
const files = walkVue(shellRoot)
const globalTokens = new Set([...baseCss.matchAll(/(^|[;{\s])(\-\-[a-z][\w-]*)\s*:/gmi)].map(match => match[2]))
const localTokens = new Set(["--workspace-rail-width", "--persistent-vision-dock-clearance"])
const undeclared = []
const fragmented = []
const forbiddenModuleIcon = []
const uncontrolledBrandColor = []

for (const file of files) {
  const source = readFileSync(file, "utf8")
  const label = relative(frontendRoot, file)
  if ((source.match(/<style(?:\s|>)/g) ?? []).length > 1) fragmented.push(label)
  for (const token of source.matchAll(/var\((\-\-[a-z][\w-]*)/gi)) {
    if (!globalTokens.has(token[1]) && !localTokens.has(token[1])) undeclared.push(`${label}: ${token[1]}`)
  }
  if (/iconFor\s*=.*[◇▣◎↗⌂▤]/s.test(source) || /workspace-module-rail[\s\S]*[◇▣◎↗⌂▤]/.test(source)) forbiddenModuleIcon.push(label)
  for (const color of source.matchAll(/#[0-9a-f]{3,8}\b/gi)) uncontrolledBrandColor.push(`${label}: ${color[0]}`)
}

const failures = [
  fragmented.length && `Multiple Vue style blocks:\n${fragmented.map(path => `- ${path}`).join("\n")}`,
  undeclared.length && `Undeclared CSS variables:\n${[...new Set(undeclared)].map(item => `- ${item}`).join("\n")}`,
  forbiddenModuleIcon.length && `Forbidden Unicode module icons:\n${forbiddenModuleIcon.map(path => `- ${path}`).join("\n")}`,
  uncontrolledBrandColor.length && `Hard-coded brand colors outside base.css:\n${[...new Set(uncontrolledBrandColor)].map(item => `- ${item}`).join("\n")}`
].filter(Boolean)

if (failures.length) {
  console.error(`UI style standardization audit failed (${files.length} app-shell Vue files).\n${failures.join("\n\n")}`)
  process.exit(1)
}

console.log(`UI style standardization audit passed (all, ${files.length} app-shell Vue files).`)
