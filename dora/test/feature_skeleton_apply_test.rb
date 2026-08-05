#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "fileutils"
require_relative "../lib/dora/feature_skeleton_apply"

input = {
  "kind" => "dora_feature_skeleton", "version" => 1,
  "capability" => "record-supply", "entity" => "supply",
  "fields" => [{"id" => "name", "type" => "string", "confirmed" => true}],
  "permission" => "household-write", "workflow" => "supply-active",
  "api_operations" => [{"id" => "create-supply", "purpose" => "Record one supply.", "confirmed" => true}],
  "ui_blueprint" => "list-detail", "confirmation" => true
}

Dir.mktmpdir("dora-feature-skeleton-apply") do |root|
  Dir.chdir(root) do
    destination = "generated"
    dry_run = Dora::FeatureSkeletonApply.apply!(document: input, destination: destination, dry_run: true)
    abort "dry-run created a destination" if File.exist?(destination)
    abort "dry-run did not report the migration" unless dry_run.fetch("files").include?("backend/src/main/resources/db/migration/V__create_supply.sql")

    result = Dora::FeatureSkeletonApply.apply!(document: input, destination: destination, dry_run: false)
    migration = File.join(destination, "backend/src/main/resources/db/migration/V__create_supply.sql")
    vue = File.join(destination, "frontend/src/features/record-supply/feature-view.vue")
    abort "apply did not write a reviewable migration" unless result.fetch("mode") == "applied" && File.read(migration).include?("REVIEW REQUIRED")
    abort "apply did not write a Vue skeleton" unless File.file?(vue)

    begin
      Dora::FeatureSkeletonApply.apply!(document: input, destination: destination, dry_run: false)
      abort "collision was accepted"
    rescue ArgumentError => error
      abort error.message unless error.message.include?("existing paths")
    end
  end
end

begin
  Dora::FeatureSkeletonApply.apply!(document: input, destination: "../unsafe", dry_run: true)
  abort "path traversal was accepted"
rescue ArgumentError => error
  abort error.message unless error.message.include?("relative path")
end

puts "Dora feature skeleton apply test passed (dry-run, reviewed placeholders, collision rejection, and traversal rejection are enforced)."
