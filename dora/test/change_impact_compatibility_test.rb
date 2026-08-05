#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require_relative "../lib/dora/change_check"
require_relative "../lib/dora/change_impact"

Dir.mktmpdir("dora-change-impact-compatibility") do |root|
  ["alpha", "beta"].each do |name|
    config = {"kind" => "dora_change_impact", "version" => 1, "rules" => [{"id" => name, "path_prefixes" => ["#{name}/"], "validations" => ["#{name}-test"], "documentation" => ["docs/#{name}.md"], "runtime_evidence" => ["runtime/#{name}"], "decisions" => ["#{name.upcase}-1"]}]}
    path = File.join(root, "#{name}.yaml")
    File.write(path, YAML.dump(config))
    impact = Dora::ChangeImpact.assess!(path, ["#{name}/entry.rb", "notes.txt"])
    facade = Dora::ChangeCheck.check!(path, ["#{name}/entry.rb", "notes.txt"])
    fields = %w[changed_paths classifications validations documentation runtime_evidence decisions unmatched_paths]
    abort "change impact compatibility drifted for #{name}" unless fields.all? { |field| impact.fetch(field) == facade.fetch(field) }
    abort "change impact lost canonical path detail" unless impact.fetch("path_impacts").map { |entry| entry.fetch("path") } == ["#{name}/entry.rb", "notes.txt"]
  end
end

puts "Dora change impact compatibility test passed (the canonical impact service and compatibility facade agree for two projects)."
