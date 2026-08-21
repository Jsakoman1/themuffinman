#!/usr/bin/env ruby
# frozen_string_literal: true

module WorkArtifactRetention
  EXECUTABLE_KINDS = %w[work master execution-inventory execution_inventory].freeze

  module_function

  def classification(artifact, externally_referenced)
    kind = artifact["kind"]
    status = artifact["status"]
    return "historical_contract" unless EXECUTABLE_KINDS.include?(kind)
    return "active_or_draft" if %w[active draft].include?(status)
    return "externally_referenced_verified" if status == "verified" && externally_referenced
    return "unreferenced_verified" if status == "verified"

    "other_status"
  end

  def reference_sources(root)
    paths = [File.join(root, "AGENTS.md"), File.join(root, "Makefile")]
    paths.concat(Dir[File.join(root, "scripts/**/*")])
    paths.concat(Dir[File.join(root, "docs/**/*")].reject do |path|
      path.start_with?(File.join(root, "docs/work/"), File.join(root, "docs/audit-output/"), File.join(root, "docs/runtime-evidence/"))
    end)
    paths.select { |path| File.file?(path) }
  end

  def referenced_paths(root)
    corpus = reference_sources(root).map { |path| File.read(path, mode: "rb").force_encoding("UTF-8") }.join("\n")
    Dir[File.join(root, "docs/work/*.yaml")].each_with_object({}) do |path, result|
      relative = path.delete_prefix("#{root}/")
      result[relative] = corpus.include?(relative)
    end
  end
end
