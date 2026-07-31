import assert from "node:assert/strict"

const partsFor = (value, timezone) => Object.fromEntries(new Intl.DateTimeFormat("en-CA", {timeZone: timezone, year: "numeric", month: "2-digit", day: "2-digit", hour: "2-digit", minute: "2-digit", hourCycle: "h23"}).formatToParts(new Date(value)).filter(part => part.type !== "literal").map(part => [part.type, part.value]))
const dayKey = (value, timezone) => { const parts = partsFor(value, timezone); return `${parts.year}-${parts.month}-${parts.day}` }
const minutes = (value, timezone) => { const parts = partsFor(value, timezone); return Number(parts.hour) * 60 + Number(parts.minute) }

const instant = "2026-07-29T22:30:00Z"
assert.equal(dayKey(instant, "Europe/Zurich"), "2026-07-30")
assert.equal(minutes(instant, "Europe/Zurich"), 30)
assert.equal(dayKey(instant, "America/Los_Angeles"), "2026-07-29")
assert.equal(minutes(instant, "America/Los_Angeles"), 930)

const assignColumns = (events) => {
  const activeEnds = []
  return events.map(({start, end}) => {
    activeEnds.forEach((activeEnd, index) => { if (activeEnd <= start) activeEnds[index] = -1 })
    const available = activeEnds.findIndex(activeEnd => activeEnd < 0)
    const column = available < 0 ? activeEnds.length : available
    activeEnds[column] = end
    return column
  })
}
assert.deepEqual(assignColumns([{start: 540, end: 660}, {start: 570, end: 630}, {start: 660, end: 720}]), [0, 1, 0])
console.log("Calendar timezone layout checks passed.")
