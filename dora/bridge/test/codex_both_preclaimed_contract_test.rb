#!/usr/bin/env ruby
# frozen_string_literal: true

require "json"
require "fileutils"
require "open3"
require "tmpdir"
require "yaml"
require_relative "../../lib/dora/handoff"

ROOT = File.expand_path("../..", __dir__)
WORKSPACE_ROOT = File.dirname(ROOT)
CONTRACT = File.join(ROOT, "bridge/bin/dora-bridge-codex-both-preclaimed-contract")
INSTALLER = File.join(ROOT, "bridge/bin/dora-bridge-install-codex-both-runner-mode")
TEMPLATE = File.join(ROOT, "bridge/templates/codex-both-v3")

def write_registry(home)
  path = File.join(home, ".config/dora-bridge/projects.yaml")
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, YAML.dump({"kind" => "dora_bridge_projects", "version" => 1, "projects" => [{"id" => "dora", "adapter_path" => "unused/.dora/project.yaml", "capabilities" => {"handoff_write" => true}}, {"id" => "doomsday-storage", "adapter_path" => "unused/.dora/project.yaml", "capabilities" => {"handoff_write" => true}}, {"id" => "read-only", "adapter_path" => "unused/.dora/project.yaml"}]}))
  path
end

def write_executable(path, source)
  FileUtils.mkdir_p(File.dirname(path))
  File.write(path, source)
  FileUtils.chmod(0o700, path)
end

def materialize_launcher(root, home, doomsday_root)
  launcher = File.join(root, "codex-both")
  content = File.read(TEMPLATE).gsub("__DORA_WORKSPACE_ROOT__", WORKSPACE_ROOT).gsub("__DOOMSDAY_ROOT__", doomsday_root)
  write_executable(launcher, content)
  launcher
end

Dir.mktmpdir("dora-preclaimed-contract") do |root|
  home = File.join(root, "owner-home")
  doomsday_root = File.join(root, "DoomsDayStorage")
  state_root = File.join(home, ".local/share/dora-bridge")
  registry = write_registry(home)
  FileUtils.mkdir_p(doomsday_root)
  write_executable(File.join(home, ".local/libexec/dora-bridge-runtime"), "#!/usr/bin/env zsh\nexit 0\n")
  capture = File.join(root, "codex-arguments.json")
  write_executable(File.join(home, ".local/bin/codex"), <<~RUBY)
    #!/usr/bin/env ruby
    require "json"
    File.write(#{capture.inspect}, JSON.generate(ARGV))
  RUBY
  launcher = materialize_launcher(root, home, doomsday_root)

  output, status = Open3.capture2e({"HOME" => home}, launcher)
  abort "normal no-argument codex-both invocation did not remain interactive: #{output}" unless status.success?
  interactive_arguments = JSON.parse(File.read(capture))
  abort "normal no-argument codex-both unexpectedly used runner exec mode" unless interactive_arguments == ["-C", WORKSPACE_ROOT, "--add-dir", doomsday_root]

  store = Dora::Handoff.new(state_root: state_root)
  ready = store.create!(project: "dora", title: "Ready", objective: "ready", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "preclaimed-ready")
  claimed = store.claim!(id: ready.fetch("id"), project: "dora", claimed_by: "codex")
  output, status = Open3.capture2e(RbConfig.ruby, CONTRACT, registry, state_root, claimed.fetch("id"))
  abort "preclaimed contract did not return only safe identifiers" unless status.success? && JSON.parse(output) == {"status" => "claimed_handoff", "project" => "dora", "handoff_id" => claimed.fetch("id")}
  output, status = Open3.capture2e(RbConfig.ruby, CONTRACT, registry, state_root, "handoff-00000000-0000-0000-0000-000000000000")
  abort "forged ID was accepted" if status.success? || output.include?(state_root)
  read_only = store.create!(project: "read-only", title: "Read only", objective: "no execution", acceptance_criteria: [], constraints: [], references: [], created_by: "chatgpt", client_request_id: "preclaimed-read-only")
  store.claim!(id: read_only.fetch("id"), project: "read-only", claimed_by: "codex")
  output, status = Open3.capture2e(RbConfig.ruby, CONTRACT, registry, state_root, read_only.fetch("id"))
  abort "cross-project authorization was accepted" if status.success? || output.include?(state_root)

  output, status = Open3.capture2e({"HOME" => home}, launcher, "--dora-preclaimed-handoff", claimed.fetch("id"))
  abort "validated preclaimed runner invocation did not execute Codex: #{output}" unless status.success?
  runner_arguments = JSON.parse(File.read(capture))
  prompt = runner_arguments.last
  expected_prefix = ["exec", "-C", WORKSPACE_ROOT, "--add-dir", doomsday_root, "--color", "never"]
  abort "runner invocation did not use fixed non-interactive Codex exec mode" unless runner_arguments.take(expected_prefix.length) == expected_prefix
  abort "runner invocation did not use a non-empty fixed bootstrap prompt" unless prompt.is_a?(String) && !prompt.empty? && prompt.include?(claimed.fetch("id")) && prompt.include?("handoff-get") && prompt.include?("report completion or any blocker through Dora")
  abort "runner passed anything except the claimed UUID through its launcher interface" unless File.read(TEMPLATE).include?("[[ $# -eq 2 ]]") && !File.read(TEMPLATE).include?("--dora-preclaimed-handoff\" , project")

  File.delete(capture)
  output, status = Open3.capture2e({"HOME" => home}, launcher, "--dora-preclaimed-handoff")
  abort "missing runner handoff ID did not fail closed" unless !status.success? && status.exitstatus == 70 && !File.exist?(capture)
  output, status = Open3.capture2e({"HOME" => home}, launcher, "--dora-preclaimed-handoff", "handoff-00000000-0000-0000-0000-000000000000")
  abort "invalid runner handoff ID did not fail closed" unless !status.success? && status.exitstatus == 70 && !File.exist?(capture)

  template = File.read(TEMPLATE)
  abort "template exposes a custom prompt or shell execution surface in preclaimed mode" if template.match?(/eval|--dora-preclaimed-handoff.*\$@/)
  abort "template lacks a fixed claimed-UUID contract" unless template.include?("--dora-preclaimed-handoff") && template.include?("handoff-get") && template.include?("$codex_bin\" exec")
  abort "template relies on inherited PATH for Codex resolution" unless template.include?("readonly codex_bin=\"$HOME/.local/bin/codex\"") && template.include?("exec \"$codex_bin\"")
  output, status = Open3.capture2e(RbConfig.ruby, INSTALLER)
  abort "installer accepted an implicit host mutation" if status.success? || !output.include?("usage:")
  local_bin = File.join(root, ".local/bin")
  FileUtils.mkdir_p(local_bin)
  target = File.join(local_bin, "codex-both")
  backup = "#{target}.v2-backup"
  File.write(target, "#!/usr/bin/env zsh\n# Owner-local Dora Bridge launcher\n--dora-preclaimed-handoff\n")
  File.write(backup, "V2 backup")
  FileUtils.chmod(0o700, target)
  FileUtils.chmod(0o600, backup)
  output, status = Open3.capture2e({"HOME" => root}, RbConfig.ruby, INSTALLER, "--update")
  abort "explicit V3 update did not safely replace the installed template: #{output}" unless status.success? && output.include?("Updated V3 local runner mode") && File.read(target).include?("$codex_bin\" exec") && File.read(backup) == "V2 backup"
end

puts "Dora preclaimed codex-both contract test passed (interactive owner path, claimed-UUID exec bootstrap, fail-closed IDs, explicit update, and no-autostart installation)."
