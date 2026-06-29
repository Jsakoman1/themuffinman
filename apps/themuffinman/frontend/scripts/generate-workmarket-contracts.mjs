import {mkdir, readdir, readFile, writeFile} from "node:fs/promises"
import path from "node:path"
import {fileURLToPath} from "node:url"
import {parse as parseYaml} from "yaml"

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const backendRoot = path.resolve(__dirname, "../../src/main/java/com/themuffinman/app")
const outputPath = path.resolve(__dirname, "../src/contracts/generated/themuffinmanContract.ts")
const operatingModelPath = path.resolve(__dirname, "../../../../docs/agent-operating-model.yaml")

const adminAgentSafetyFlagIds = [
  "translation_unreliable",
  "ambiguity",
  "destructive_confirmation",
  "multi_actor",
  "current_location",
  "simulation_not_safe"
]

const transportAliases = {
  AppUser: "AppUserResponseDTO",
  AppUserRoleOption: "AppUserRoleOptionDTO",
  AppUserRequest: "AppUserRequestDTO",
  AdminCircleGroup: "AdminCircleGroupResponseDTO",
  AdminCircleOverview: "AdminCircleOverviewDTO",
  AdminApplicationsQuery: "AdminApplicationsQueryDTO",
  AdminAgentPlaygroundRequest: "AdminAgentPlaygroundRequestDTO",
  AdminAgentPlaygroundResponse: "AdminAgentPlaygroundResponseDTO",
  AdminAgentSimulationRequest: "AdminAgentSimulationRequestDTO",
  AdminAgentSimulationResponse: "AdminAgentSimulationResponseDTO",
  CircleBlockCreate: "CircleBlockCreateDTO",
  CircleCandidate: "CircleSearchResultDTO",
  CircleCandidateListResponse: "CircleSearchResultListResponseDTO",
  ChatCircleOption: "ChatCircleOptionDTO",
  ChatContact: "ChatContactDTO",
  ChatConversationSummary: "ChatConversationSummaryDTO",
  ChatMessage: "ChatMessageDTO",
  ChatMessageRequest: "ChatMessageRequestDTO",
  ChatOpenConversationRequest: "ChatOpenConversationRequestDTO",
  ChatWorkspace: "ChatWorkspaceDTO",
  CircleConnectionsQuery: "CircleConnectionsQueryDTO",
  CircleContact: "CircleContactDTO",
  CircleContactListResponse: "CircleContactListResponseDTO",
  CircleGroup: "CircleGroupResponseDTO",
  CircleGroupRequest: "CircleGroupRequestDTO",
  CircleMembership: "CircleMemberDTO",
  CircleOverview: "CircleOverviewDTO",
  CircleRelation: "CircleRelationDTO",
  CircleRequest: "CircleRequestResponseDTO",
  CircleRequestCreate: "CircleRequestCreateDTO",
  CircleRequestListResponse: "CircleRequestListResponseDTO",
  CircleSummary: "CircleSummaryDTO",
  ConnectionCircleUpdateRequest: "ConnectionCircleUpdateDTO",
  DashboardResponse: "DashboardResponseDTO",
  DashboardSections: "DashboardSectionsDTO",
  DashboardSummary: "DashboardSummaryDTO",
  LocationLookupCandidate: "LocationLookupCandidateDTO",
  LocationLookupRequest: "LocationLookupRequestDTO",
  LocationLookupResponse: "LocationLookupResponseDTO",
  LocationReverseLookupRequest: "LocationReverseLookupRequestDTO",
  LocationModeOption: "LocationModeOptionDTO",
  NavigationTarget: "NavigationTargetDTO",
  QuestApplicationAllowedAction: "ApplicationAllowedAction",
  PageQuery: "PageQueryDTO",
  ProfilePrimaryAction: "ProfilePrimaryActionDTO",
  Quest: "QuestResponseDTO",
  QuestAudienceOption: "QuestAudienceOptionDTO",
  QuestApplication: "QuestApplicationResponseDTO",
  QuestApplicationDetail: "QuestApplicationDetailResponseDTO",
  QuestApplicationDetailSections: "QuestApplicationDetailSectionsDTO",
  QuestApplicationListResponse: "QuestApplicationListResponseDTO",
  QuestApplicationRequest: "QuestApplicationRequestDTO",
  QuestApplicationsView: "QuestApplicationsViewDTO",
  QuestDetail: "QuestDetailResponseDTO",
  QuestDetailSections: "QuestDetailSectionsDTO",
  QuestLocationVisibilityOption: "QuestLocationVisibilityOptionDTO",
  QuestListResponse: "QuestListResponseDTO",
  QuestNewsItem: "QuestNewsItemResponseDTO",
  QuestRequest: "QuestRequestDTO",
  QuestSearchRequest: "QuestSearchRequestDTO",
  QuestStatusFilterOption: "QuestStatusFilterOptionDTO",
  QuestStatusOption: "QuestStatusOptionDTO",
  TextPageQuery: "TextPageQueryDTO",
  UserLocationSettings: "UserLocationSettingsDTO",
  UserProfileView: "UserProfileViewDTO",
  UserRatingSummary: "UserRatingSummaryDTO",
  UserReview: "UserReviewResponseDTO",
  UserReviewRequest: "UserReviewRequestDTO",
  WorkmarketOptions: "WorkmarketOptionsDTO"
}

const fieldTypeOverrides = {
  "AppUserResponseDTO.role": "AppUserRole",
  "AuthResponse.role": "AppUserRole"
}

const fieldNullableOverrides = {
  "AuthResponse.profileDescription": true,
  "AuthResponse.profileAvatarDataUrl": true,
  "AuthResponse.token": true
}

const toUpperSnakeCase = (value) => value
  .replace(/([a-z0-9])([A-Z])/g, "$1_$2")
  .replace(/-/g, "_")
  .toUpperCase()

const stripComments = (value) => value
  .replace(/\/\*[\s\S]*?\*\//g, "")
  .replace(/\/\/.*$/gm, "")

async function collectEnumFiles(directory) {
  const entries = await readdir(directory, {withFileTypes: true})
  const nestedFiles = await Promise.all(entries.map(async (entry) => {
    const entryPath = path.join(directory, entry.name)
    if (entry.isDirectory()) {
      return collectEnumFiles(entryPath)
    }

    if (entry.isFile() && entry.name.endsWith(".java")) {
      return [entryPath]
    }

    return []
  }))

  return nestedFiles.flat()
}

async function parseEnumFile(filePath) {
  const source = await readFile(filePath, "utf8")
  const enumNameMatch = source.match(/public enum\s+(\w+)\s*\{/)
  if (!enumNameMatch) {
    return null
  }

  const enumName = enumNameMatch[1]
  const bodyStart = source.indexOf("{", enumNameMatch.index)
  const bodyEnd = source.lastIndexOf("}")
  const enumBody = stripComments(source.slice(bodyStart + 1, bodyEnd))
  const constantBlock = enumBody.includes(";") ? enumBody.split(";")[0] : enumBody
  const values = constantBlock
    .split(",")
    .map((part) => part.trim())
    .filter(Boolean)
    .map((part) => part.match(/^[A-Z0-9_]+/)?.[0] ?? null)
    .filter((part) => part !== null)

  if (values.length === 0) {
    return null
  }

  return {enumName, values}
}

function buildOutput(enums) {
  const lines = [
    "// Generated from backend Java enums and DTOs by scripts/generate-workmarket-contracts.mjs.",
    "// Do not edit this file manually.",
    ""
  ]

  for (const {enumName, values} of enums) {
    const constantName = `${toUpperSnakeCase(enumName)}_VALUES`
    const valuesLiteral = values.map((value) => `"${value}"`).join(", ")
    lines.push(`export const ${constantName} = [${valuesLiteral}] as const`)
    lines.push(`export type ${enumName} = typeof ${constantName}[number]`)
    lines.push("")
  }

  return lines
}

function buildAgentWorkflowOutput(operatingModel) {
  const intentIds = [...new Set((operatingModel.intents ?? []).map((intent) => intent.id))].sort()
  const endpointIds = [...new Set((operatingModel.api?.endpoints ?? []).map((endpoint) => endpoint.id))].sort()
  const unresolvedInputs = [...new Set((operatingModel.intents ?? [])
    .flatMap((intent) => intent.required_inputs ?? []))]
    .sort()

  return [
    `export const AGENT_INTENT_IDS = [${intentIds.map((item) => `"${item}"`).join(", ")}] as const`,
    "export type AgentIntentId = typeof AGENT_INTENT_IDS[number]",
    "",
    `export const AGENT_ENDPOINT_IDS = [${endpointIds.map((item) => `"${item}"`).join(", ")}] as const`,
    "export type AgentEndpointId = typeof AGENT_ENDPOINT_IDS[number]",
    "",
    `export const ADMIN_AGENT_SAFETY_FLAG_IDS = [${adminAgentSafetyFlagIds.map((item) => `"${item}"`).join(", ")}] as const`,
    "export type AdminAgentSafetyFlagIdGenerated = typeof ADMIN_AGENT_SAFETY_FLAG_IDS[number]",
    "",
    `export const AGENT_REQUIRED_UNRESOLVED_INPUTS = [${unresolvedInputs.map((item) => JSON.stringify(item)).join(", ")}] as const`,
    "export type AgentRequiredUnresolvedInput = typeof AGENT_REQUIRED_UNRESOLVED_INPUTS[number]",
    ""
  ]
}

const SIMPLE_TYPE_MAP = new Map([
  ["String", "string"],
  ["CharSequence", "string"],
  ["boolean", "boolean"],
  ["Boolean", "boolean"],
  ["byte", "number"],
  ["Byte", "number"],
  ["short", "number"],
  ["Short", "number"],
  ["int", "number"],
  ["Integer", "number"],
  ["long", "number"],
  ["Long", "number"],
  ["float", "number"],
  ["Float", "number"],
  ["double", "number"],
  ["Double", "number"],
  ["BigDecimal", "number"],
  ["Instant", "string"],
  ["LocalDate", "string"],
  ["LocalDateTime", "string"],
  ["OffsetDateTime", "string"],
  ["UUID", "string"],
  ["Object", "unknown"]
])

const stripPackage = (typeName) => typeName.split(".").at(-1)

const splitTopLevel = (value, separator) => {
  const parts = []
  let current = ""
  let angleDepth = 0
  let parenDepth = 0
  let inString = false

  for (const char of value) {
    if (char === "\"" && !inString) {
      inString = true
    } else if (char === "\"" && inString) {
      inString = false
    }

    if (!inString) {
      if (char === "<") angleDepth += 1
      if (char === ">") angleDepth -= 1
      if (char === "(") parenDepth += 1
      if (char === ")") parenDepth -= 1
    }

    if (char === separator && angleDepth === 0 && parenDepth === 0 && !inString) {
      parts.push(current.trim())
      current = ""
      continue
    }

    current += char
  }

  if (current.trim()) {
    parts.push(current.trim())
  }

  return parts
}

const removeInlineAnnotations = (value) => value.replace(/@[\w.]+(?:\([^)]*\))?\s*/g, "")

const normalizeJavaType = (value) => removeInlineAnnotations(value)
  .replace(/\bfinal\s+/g, "")
  .replace(/\?/g, "")
  .trim()

function mapJavaTypeToTs(rawType) {
  const typeName = normalizeJavaType(rawType)
  const simpleType = stripPackage(typeName)

  if (simpleType.startsWith("List<") || simpleType.startsWith("Set<")) {
    const innerType = simpleType.slice(simpleType.indexOf("<") + 1, -1)
    return `${mapJavaTypeToTs(innerType)}[]`
  }

  if (simpleType.startsWith("Map<")) {
    const [, valueType = "unknown"] = splitTopLevel(simpleType.slice(4, -1), ",")
    return `Record<string, ${mapJavaTypeToTs(valueType)}>`
  }

  return SIMPLE_TYPE_MAP.get(simpleType) ?? simpleType
}

function extractClassBody(source, startIndex) {
  const bodyStart = source.indexOf("{", startIndex)
  let depth = 0

  for (let index = bodyStart; index < source.length; index += 1) {
    const char = source[index]
    if (char === "{") depth += 1
    if (char === "}") depth -= 1
    if (depth === 0) {
      return source.slice(bodyStart + 1, index)
    }
  }

  return ""
}

function parseFieldsFromClassBody(body) {
  const matches = [...body.matchAll(/((?:\s*@[\w.]+(?:\([^)]*\))?\s+)*)\s*private\s+([^;\n]+?)\s+([A-Za-z0-9_]+)\s*;/g)]
  return matches.flatMap((match) => {
    const annotationSource = `${match[1]} ${match[2]}`
    if (/\bstatic\b/.test(match[2])) {
      return []
    }
    return {
      name: match[3],
      type: mapJavaTypeToTs(match[2]),
      nullable: annotationSource.includes("@Nullable"),
      optional: annotationSource.includes("@ContractOptional")
    }
  })
}

function parseRecordComponents(source, recordName, startIndex) {
  const signatureStart = source.indexOf(`record ${recordName}`, startIndex)
  if (signatureStart === -1) {
    return []
  }

  const openParenIndex = source.indexOf("(", signatureStart)
  let depth = 0
  let closeParenIndex = -1

  for (let index = openParenIndex; index < source.length; index += 1) {
    const char = source[index]
    if (char === "(") depth += 1
    if (char === ")") depth -= 1
    if (depth === 0) {
      closeParenIndex = index
      break
    }
  }

  if (openParenIndex === -1 || closeParenIndex === -1) {
    return []
  }

  return splitTopLevel(source.slice(openParenIndex + 1, closeParenIndex), ",").map((component) => {
    const nullable = component.includes("@Nullable")
    const optional = component.includes("@ContractOptional")
    const tokens = removeInlineAnnotations(component).trim().split(/\s+/)
    const name = tokens.at(-1)
    const type = tokens.slice(0, -1).join(" ")
    return {
      name,
      type: mapJavaTypeToTs(type),
      nullable,
      optional
    }
  })
}

async function parseDtoFile(filePath) {
  const source = stripComments(await readFile(filePath, "utf8"))
  const classMatch = source.match(/public\s+class\s+(\w+)\s*\{/)
  if (classMatch) {
    const interfaceName = classMatch[1]
    const body = extractClassBody(source, classMatch.index ?? 0)
    const fields = parseFieldsFromClassBody(body).map((field) => ({
      ...field,
      type: fieldTypeOverrides[`${interfaceName}.${field.name}`] ?? field.type,
      nullable: fieldNullableOverrides[`${interfaceName}.${field.name}`] ?? field.nullable
    }))
    return fields.length > 0 ? {interfaceName, fields} : null
  }

  const recordMatch = source.match(/public\s+record\s+(\w+)\s*\(/)
  if (recordMatch) {
    const interfaceName = recordMatch[1]
    const fields = parseRecordComponents(source, interfaceName, recordMatch.index ?? 0).map((field) => ({
      ...field,
      type: fieldTypeOverrides[`${interfaceName}.${field.name}`] ?? field.type,
      nullable: fieldNullableOverrides[`${interfaceName}.${field.name}`] ?? field.nullable
    }))
    return fields.length > 0 ? {interfaceName, fields} : null
  }

  return null
}

function buildDtoOutput(dtos) {
  const lines = []

  for (const {interfaceName, fields} of dtos) {
    lines.push(`export interface ${interfaceName} {`)
    for (const field of fields) {
      const optionalMarker = field.optional ? "?" : ""
      const nullableSuffix = field.nullable ? " | null" : ""
      lines.push(`  ${field.name}${optionalMarker}: ${field.type}${nullableSuffix}`)
    }
    lines.push("}")
    lines.push("")
  }

  for (const [aliasName, targetName] of Object.entries(transportAliases).sort(([left], [right]) => left.localeCompare(right))) {
    lines.push(`export type ${aliasName} = ${targetName}`)
  }

  return lines
}

async function main() {
  const checkOnly = process.argv.includes("--check")
  const javaFiles = await collectEnumFiles(backendRoot)
  const parsedEnums = (await Promise.all(javaFiles.map(parseEnumFile)))
    .filter((entry) => entry !== null)
    .sort((left, right) => left.enumName.localeCompare(right.enumName))

  const dtoFiles = javaFiles.filter((filePath) => filePath.includes("/dto/"))
  const parsedDtos = (await Promise.all(dtoFiles.map(parseDtoFile)))
    .filter((entry) => entry !== null)
    .sort((left, right) => left.interfaceName.localeCompare(right.interfaceName))
  const operatingModel = parseYaml(await readFile(operatingModelPath, "utf8"))

  const outputLines = [
    ...buildOutput(parsedEnums),
    ...buildDtoOutput(parsedDtos),
    ...buildAgentWorkflowOutput(operatingModel)
  ]
  const expectedOutput = `${outputLines.join("\n").trimEnd()}\n`

  if (checkOnly) {
    let currentOutput = ""
    try {
      currentOutput = await readFile(outputPath, "utf8")
    } catch {
      console.error(`Generated frontend contract is missing: ${outputPath}`)
      process.exitCode = 1
      return
    }

    if (currentOutput !== expectedOutput) {
      console.error("Generated frontend contract is stale. Run `npm run generate:contracts` from apps/themuffinman/frontend.")
      process.exitCode = 1
      return
    }

    console.log("Generated frontend contract is fresh.")
    return
  }

  await mkdir(path.dirname(outputPath), {recursive: true})
  await writeFile(outputPath, expectedOutput, "utf8")
  console.log(`Generated frontend contract: ${path.relative(path.resolve(__dirname, ".."), outputPath)}`)
}

await main()
