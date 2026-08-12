# frozen_string_literal: true

require_relative "../lib/idc/dossier"

ROOT = File.expand_path("../..", __dir__)

def assert(message, &block)
  raise "assertion failed: #{message}" unless block.call
end

def load_dossier(name)
  IDC::Dossier.load!(File.join(ROOT, "idc/dossiers", name))
end

envelope = IDC::DoraEnvelope.load!(File.join(ROOT, "idc/envelopes/doomsday-storage-v1-dora-read-envelope.yaml"))
assert("historical Dora envelope remains explicitly sanitized") { envelope.data.fetch("sanitized") == true }
assert("historical Dora envelope has no accepted decision") { envelope.data.fetch("accepted_decisions").empty? }

greenfield = load_dossier("doomsday-storage-greenfield-baseline.yaml")
greenfield_render = greenfield.render
assert("greenfield profile is preserved") { greenfield.data.fetch("profile") == "greenfield_product_delivery_baseline" }
assert("historical source staleness is visible") { greenfield_render.include?("[SOURCE STALE] historical-owner-interview") }
assert("greenfield conflict is visible") { greenfield_render.include?("[CONFLICT]") }
assert("greenfield missing context is visible") { greenfield_render.include?("[MISSING_CONTEXT]") }
assert("greenfield owner confirmation is cited") { greenfield_render.include?("sources: historical-owner-interview") }
assert("greenfield has a first vertical slice") { greenfield_render.include?("Candidate first vertical slice") }
assert("greenfield promotion remains owner-gated") { greenfield_render.include?("OWNER ACTION REQUIRED") && greenfield_render.include?("Promotion is manual") }
assert("same greenfield input gives same output") { greenfield_render == load_dossier("doomsday-storage-greenfield-baseline.yaml").render }

research = load_dossier("agricultural-land-options-research-dossier.yaml")
research_render = research.render
assert("research profile is preserved") { research.data.fetch("profile") == "research_dossier" }
assert("synthetic source is labelled stale") { research_render.include?("[SOURCE STALE] synthetic-option-sheet") }
assert("conflicting source is visible") { research_render.include?("[SOURCE CONFLICTING] synthetic-site-notes") }
assert("synthetic research retains conflict") { research_render.include?("[CONFLICT]") }
assert("research recommendation is not a decision") { research_render.include?("[ADVISORY_RECOMMENDATION") && research_render.include?("advisory-only synthetic pilot") }
assert("same research input gives same output") { research_render == load_dossier("agricultural-land-options-research-dossier.yaml").render }

runtime_source = File.read(File.join(ROOT, "idc/lib/idc/dossier.rb")) + File.read(File.join(ROOT, "idc/lib/idc/dora_envelope.rb"))
assert("pilot has no automatic Dora access") { !runtime_source.include?("Dora::") && !runtime_source.include?(".dora/") && !runtime_source.include?("DoomsDayStorage") }

puts "IDC pilot profile tests passed"
