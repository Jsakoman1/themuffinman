#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "tmpdir"

ROOT = File.expand_path("../..", __dir__)
LAUNCHER = File.join(ROOT, "bin/dora")
PINNED_SOURCE = File.expand_path("../dora", ROOT)

def run_launcher(environment = {}, *arguments, launcher: LAUNCHER, chdir: ROOT)
  Open3.capture2e(environment, launcher, *arguments, chdir: chdir)
end

legacy_binary = %w[dora bin dora].join("/")
launcher = File.read(LAUNCHER)
abort "verified Dora launcher falls back to the embedded Dora tree" if launcher.include?("File.join(ROOT, \"dora\")") || launcher.include?("../#{legacy_binary}")
abort "verified Dora launcher reads source code before checking the pinned Git identity" unless launcher.index('"rev-parse", "HEAD"') < launcher.index("require bootstrap_path")

output, status = run_launcher({}, "help")
abort "verified Dora launcher did not execute the MuffinMan Dora CLI: #{output}" unless status.success? && output.include?("Dora commands:")

output, status = run_launcher({"DORA_SOURCE_PATH" => PINNED_SOURCE}, "doctor", "../dora/.dora/project.yaml")
abort "verified Dora launcher did not accept a matching local override for Dora Doctor: #{output}" unless status.success? && output.include?("PASSED project-root") && output.include?("PASSED project-knowledge")

output, status = run_launcher({"DORA_SOURCE_PATH" => File.join(ROOT, "dora")}, "help")
abort "verified Dora launcher accepted the legacy embedded Dora tree" if status.success?
abort "verified Dora launcher did not explain its expected source pin" unless output.include?("Expected a local Dora source") && output.include?("5f5a915ab043060cbe17735e4f2a8c6628e47447")

output, status = run_launcher({"DORA_SOURCE_PATH" => File.join(ROOT, "missing-dora-source")}, "help")
abort "verified Dora launcher accepted a missing local override" if status.success?
abort "verified Dora launcher did not explain its missing source pin" unless output.include?("Expected a local Dora source") && output.include?("5f5a915ab043060cbe17735e4f2a8c6628e47447")

Dir.mktmpdir("muffinman-verified-dora-launcher") do |sandbox|
  consumer = File.join(sandbox, "consumer")
  FileUtils.mkdir_p(File.join(consumer, "bin"))
  isolated_launcher = File.join(consumer, "bin/dora")
  FileUtils.cp(LAUNCHER, isolated_launcher)
  FileUtils.chmod(0o755, isolated_launcher)
  FileUtils.cp(File.join(ROOT, "dora-source.yaml"), File.join(consumer, "dora-source.yaml"))
  File.symlink(PINNED_SOURCE, File.join(sandbox, "dora"))

  output, status = run_launcher({}, "help", launcher: isolated_launcher, chdir: consumer)
  abort "verified Dora launcher read the missing embedded tree instead of the independent source: #{output}" unless status.success? && output.include?("Dora commands:")
end

legacy_invocations = [File.join(ROOT, "Makefile"), File.join(ROOT, ".dora/project.yaml"), *Dir[File.join(ROOT, "scripts/audits/*.rb")]].select do |path|
  content = File.read(path)
  content.include?("exec(\"#{legacy_binary}\"") || content.include?("capture3(\"#{legacy_binary}\"") || content.include?(" then #{legacy_binary} ") || content.start_with?("#{legacy_binary} ")
end
abort "active MuffinMan Dora invocations still use the embedded tree: #{legacy_invocations.join(', ')}" unless legacy_invocations.empty?

puts "Verified independent Dora invocation test passed (pinned local source only, no embedded fallback)."
