#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "tmpdir"
require_relative "../lib/dora/project_initializer"
require_relative "../lib/dora/project_doctor"
require_relative "../lib/dora/project_knowledge"

ROOT = File.expand_path("..", __dir__)
Dir.mktmpdir("dora-project-knowledge-doctor") do |root|
  Dora::ProjectInitializer.initialize!(root, project_id: "knowledge-project", manifest_path: File.join(ROOT, "templates/init-manifest.yaml"))
  result = Dora::ProjectKnowledge.validate!(root)
  abort "knowledge validator did not retain product brief" unless result.fetch("product_brief").fetch("kind") == "dora_product_brief"
  FileUtils.rm(File.join(root, "docs/domain-library.yaml"))
  begin
    Dora::ProjectKnowledge.validate!(root)
    abort "knowledge validator accepted a missing domain library"
  rescue ArgumentError => error
    abort "wrong knowledge failure: #{error.message}" unless error.message.include?("domain-library")
  end
end

puts "Dora project knowledge doctor test passed (required agent knowledge fails closed when incomplete)."
