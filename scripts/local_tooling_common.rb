#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "time"

module LocalToolingCommon
  REPO_ROOT = File.expand_path("..", __dir__)
  OUTPUT_ROOT = File.join(REPO_ROOT, "docs/generated/local-tooling")
  GENERATED_PATH_PREFIXES = [
    "docs/generated/",
    "apps/themuffinman/frontend/dist/",
    "apps/themuffinman/frontend/src/contracts/generated/"
  ].freeze
  AGENT_TRANSIENT_PATTERNS = [
    %r{\A\.agents/todo-plans/},
    %r{\A\.agents/validation-evidence/},
    %r{\A\.agents/.+-plan\.md\z}
  ].freeze

  module_function

  def ensure_output_dir(*segments)
    path = File.join(OUTPUT_ROOT, *segments)
    FileUtils.mkdir_p(path)
    path
  end

  def write_json(relative_path, payload)
    absolute_path = File.join(REPO_ROOT, relative_path)
    FileUtils.mkdir_p(File.dirname(absolute_path))
    File.write(absolute_path, JSON.pretty_generate(payload) + "\n")
  end

  def write_text(relative_path, content)
    absolute_path = File.join(REPO_ROOT, relative_path)
    FileUtils.mkdir_p(File.dirname(absolute_path))
    File.write(absolute_path, content)
  end

  def relative_path(path)
    path.delete_prefix("#{REPO_ROOT}/")
  end

  def repo_glob(*patterns)
    patterns.flat_map do |pattern|
      Dir.glob(File.join(REPO_ROOT, pattern), File::FNM_EXTGLOB)
    end.select { |path| File.file?(path) }.sort
  end

  def read(path)
    File.read(path)
  end

  def run_command(*command)
    stdout, stderr, status = Open3.capture3(*command, chdir: REPO_ROOT)
    raise "Command failed: #{command.join(' ')}\n#{stderr}" unless status.success?

    stdout
  end

  def git_changed_files
    stdout = run_command("git", "status", "--short")
    stdout.lines.each_with_object([]) do |line, paths|
      next if line.strip.empty?

      candidate = line[3..]&.strip
      next if candidate.nil? || candidate.empty?

      candidate = candidate.split(" -> ").last
      paths << candidate
    end
  rescue StandardError
    []
  end

  def truthy?(value)
    value.to_s.match?(/\A(true|1|yes|y)\z/i)
  end

  def generated_path?(relative_path)
    GENERATED_PATH_PREFIXES.any? { |prefix| relative_path.start_with?(prefix) }
  end

  def agent_transient_path?(relative_path)
    AGENT_TRANSIENT_PATTERNS.any? { |pattern| relative_path.match?(pattern) }
  end

  def filter_file_list(files, include_generated: false, include_agents: false)
    rows = files.uniq.map do |path|
      reasons = []
      reasons << "generated" if !include_generated && generated_path?(path)
      reasons << "agent_transient" if !include_agents && agent_transient_path?(path)
      {path: path, excluded: reasons.any?, reasons: reasons}
    end
    {
      included: rows.reject { |row| row[:excluded] }.map { |row| row[:path] },
      excluded: rows.select { |row| row[:excluded] },
      original_file_count: rows.size,
      filtered_file_count: rows.count { |row| row[:excluded] },
      excluded_file_count: rows.count { |row| row[:excluded] }
    }
  end

  def iso_mtime(path)
    File.mtime(path).utc.iso8601
  end

  def domain_for_path(relative_path)
    parts = relative_path.split("/")
    app_index = parts.index("app")
    return "unknown" unless app_index

    parts[app_index + 1] || "unknown"
  end

  def path_category(relative_path)
    return "backend_controller" if relative_path.include?("/controller/")
    return "backend_service" if relative_path.include?("/service/")
    return "backend_repository" if relative_path.include?("/repository/")
    return "backend_mapper" if relative_path.include?("/mapper/")
    return "backend_dto" if relative_path.include?("/dto/")
    return "backend_model" if relative_path.include?("/model/")
    return "frontend_api" if relative_path.include?("/frontend/src/modules/") && relative_path.include?("/api/")
    return "frontend_view" if relative_path.include?("/frontend/src/") && relative_path.match?(%r{/(views|pages)/})
    return "frontend_composable" if relative_path.include?("/frontend/src/") && relative_path.include?("/composables/")
    return "frontend_contract" if relative_path.include?("/frontend/src/contracts/")
    return "frontend_script" if relative_path.include?("/frontend/scripts/")
    return "docs" if relative_path.start_with?("docs/")
    return "script" if relative_path.start_with?("scripts/")

    "other"
  end

  def titleize_slug(slug)
    slug.split(/[_-]/).map { |part| part[0] ? part[0].upcase + part[1..] : part }.join(" ")
  end
end
