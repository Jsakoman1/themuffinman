#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/template_freshness"

def project(root, name, reference, retired: nil)
  FileUtils.mkdir_p(File.join(root, name, "templates"))
  FileUtils.mkdir_p(File.join(root, name, "docs"))
  File.write(File.join(root, name, reference), "current")
  File.write(File.join(root, name, "templates/prompt.md"), ["See #{reference}", retired].compact.join("\n"))
  config = File.join(root, name, "freshness.yaml")
  File.write(config, YAML.dump({"kind" => "dora_template_freshness", "version" => 1, "templates" => [{"path" => "templates/prompt.md", "required_references" => [reference], "retired_references" => ["docs/retired.md"]}]}))
  [File.join(root, name), config]
end

Dir.mktmpdir("dora-template-freshness") do |root|
  alpha_root, alpha_config = project(root, "alpha", "docs/alpha.md")
  beta_root, beta_config = project(root, "beta", "docs/beta.md", retired: "docs/retired.md")
  abort "fresh alpha template failed" unless Dora::TemplateFreshness.check!(alpha_config, project_root: alpha_root).first.fetch("fresh")
  beta = Dora::TemplateFreshness.check!(beta_config, project_root: beta_root).first
  abort "retired beta reference passed" if beta.fetch("fresh") || beta.fetch("retired_references") != ["docs/retired.md"]
end

puts "Dora template freshness test passed (two independent project configurations)."
