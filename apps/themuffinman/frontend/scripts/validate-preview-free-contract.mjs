import fs from "node:fs"
import path from "node:path"

const root = path.resolve(import.meta.dirname, "..", "src", "modules", "app-shell")
const forbidden = ["ObjectPreviewPanel", "CollectionContextRail", "Open full detail", 'data-archetype="detail-main-utility"', "detail-surface__workspace", "detail-surface__utility"]
const vueFiles = (directory) => fs.readdirSync(directory, {withFileTypes: true}).flatMap((entry) => {
  const file = path.join(directory, entry.name)
  return entry.isDirectory() ? vueFiles(file) : entry.isFile() && file.endsWith(".vue") ? [file] : []
})
const failures = vueFiles(root).flatMap((file) => {
  const source = fs.readFileSync(file, "utf8")
  return forbidden.filter((token) => source.includes(token)).map((token) => `${path.relative(root, file)} contains ${token}`)
})
if (failures.length) throw new Error(`Preview-free contract failed:\n${failures.join("\n")}`)
console.log("Preview-free contract passed: generic preview and context-rail primitives are absent.")
