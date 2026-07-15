#!/usr/bin/env ruby
# frozen_string_literal: true

require "digest"
require "date"
require "fileutils"
require "json"
require "open3"
require "tempfile"
require "time"
require "yaml"

module AuditSupport
  REPO_ROOT = File.expand_path("..", __dir__)
  OUTPUT_ROOT = File.join(REPO_ROOT, "docs/audit-output")
  GENERATED_PATH_PREFIXES = [
    "docs/audit-output/",
    "apps/themuffinman/frontend/dist/",
    "apps/themuffinman/frontend/src/contracts/generated/"
  ].freeze
  AGENT_TRANSIENT_PATTERNS = [].freeze

  module_function

  def ensure_output_dir(*segments)
    path = File.join(OUTPUT_ROOT, *segments)
    FileUtils.mkdir_p(path)
    path
  end

  def write_json(relative_path, payload)
    absolute_path = File.join(REPO_ROOT, relative_path)
    FileUtils.mkdir_p(File.dirname(absolute_path))
    atomic_write(absolute_path, JSON.pretty_generate(payload) + "\n")
  end

  def write_text(relative_path, content)
    absolute_path = File.join(REPO_ROOT, relative_path)
    FileUtils.mkdir_p(File.dirname(absolute_path))
    atomic_write(absolute_path, content)
  end

  def atomic_write(absolute_path, content)
    dir = File.dirname(absolute_path)
    basename = File.basename(absolute_path)
    tmp_path = File.join(dir, "#{basename}.tmp.#{$$}.#{Thread.current.object_id.to_s(36)}")

    File.open(tmp_path, "w") do |tmp|
      tmp.write(content)
      tmp.flush
      tmp.fsync
    end

    File.rename(tmp_path, absolute_path)
  ensure
    FileUtils.rm_f(tmp_path) if tmp_path && File.exist?(tmp_path)
  end

  def clean_text_output(text, max_lines: 80, aggressive: false)
    lines = Array(text.to_s.split(/\r?\n/)).map do |line|
      line.gsub(/\e\[[\d;]*[A-Za-z]/, "").strip
    end.reject(&:empty?)

    noise_patterns = [
      /\A(?:\[(?:INFO|DEBUG|WARNING|WARN)\]|\[\[(?:INFO|DEBUG|WARNING|WARN)\]\])\s*/i,
      /\AScanning for projects\.{3}\z/i,
      /\ADownloading from/i,
      /\ADownloaded from/i,
      /\ACopying \d+ (?:resources?|files?)\z/i,
      /\ACompiling \d+ source files\z/i,
      /\ARecompiling the module\z/i,
      /\AUsing auto detected provider\z/i,
      /\ATests run:/i,
      /\ABuilding (?:TheMuffinMan|.+? \d.*)\z/i,
      /\ANothing to compile\z/i,
      /\ATransforming\.{3}\z/i,
      /\Arendering chunks\.{3}\z/i,
      /\Acomputing gzip size\.{3}\z/i,
      /\A(?:\d+\/\d+)\s+\[[^\]]+\]\s+\d+%$/,
      /\ABUILD SUCCESS\z/i,
      /\ABUILD FAILURE\z/i,
      /\AFinished at:\z/i,
      /\ATotal time:\z/i,
      /\A--- .+ ---\z/i
    ]

    cleaned = lines.map do |line|
      line.sub(/\A(?:\[(?:INFO|DEBUG|WARNING|WARN)\]|\[\[(?:INFO|DEBUG|WARNING|WARN)\]\])\s*/i, "")
    end.reject { |line| noise_patterns.any? { |pattern| line.match?(pattern) } }
    cleaned = cleaned.reject { |line| line.match?(/\A\[[A-Z]+\]\s*$/) }
    if aggressive
      cleaned = cleaned.reject do |line|
        line.match?(%r{\Adist/(?:assets|chunks|css|js)/}) ||
          line.match?(%r{\A(?:\d+\.\d+|\d+,\d+)\s*kB\s*\│\s*gzip:}) ||
          line.match?(%r{\A✓ built in \d+(?:ms|s)\z}) ||
          line.match?(%r{\A- (?:Generated At|Updated At|Cache Status|Payload Checksum|Semantic Checksum):\s*`?.+`?\z}) ||
          line.match?(%r{\A(?:cd\s+[^&]+&&\s+)?(?:npm|yarn|pnpm|ruby|./mvnw)\s+.+\z}) ||
          line.match?(%r{\A>\s+.+\z}) ||
          line.match?(%r{\A(?:Running|Tests run:|Results:|Failures:|Errors:|Skipped:|Time elapsed:)\b})
      end
      cleaned = cleaned.map { |line| line.gsub(/\s{2,}/, " ").strip }
    end
    cleaned = cleaned.first(max_lines) if max_lines && max_lines > 0
    cleaned.join("\n")
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

  def markdown_frontmatter(content)
    match = content.match(/\A---\n(.*?)\n---\n/m)
    return {} unless match

    data = YAML.safe_load(match[1], permitted_classes: [Date, Time], aliases: true)
    data.is_a?(Hash) ? data : {}
  rescue StandardError
    {}
  end

  def markdown_frontmatter_value(content, key)
    frontmatter = markdown_frontmatter(content)
    frontmatter[key] || frontmatter[key.to_sym]
  end

  def run_command(*command)
    stdout, stderr, status = Open3.capture3(*command, chdir: REPO_ROOT)
    raise "Command failed: #{command.join(' ')}\n#{stderr}" unless status.success?

    stdout
  end

  def git_status_entries
    run_command("git", "status", "--short").lines.each_with_object([]) do |line, entries|
      next if line.strip.empty?

      candidate = line[3..]&.strip
      next if candidate.nil? || candidate.empty?

      candidate = candidate.split(" -> ").last
      entries << {status: line[0, 2].strip, path: candidate}
    end
  rescue StandardError
    []
  end

  def git_changed_files
    git_status_entries.map { |entry| entry[:path] }
  end

  def git_status_map
    git_status_entries.each_with_object({}) do |entry, rows|
      rows[entry[:path]] = entry[:status]
    end
  end

  def changed_line_numbers(path, status_map = git_status_map)
    status = status_map[path]
    if status == "??" || status == "A"
      line_count = read(path).lines.size
      return (1..line_count).to_a
    end

    diff = run_command("git", "diff", "--unified=0", "--", path)
    diff.lines.each_with_object([]) do |line, rows|
      next unless line.start_with?("@@")

      segment = line[/\+(\d+)(?:,(\d+))?/, 0]
      next if segment.nil?

      start_line = Regexp.last_match(1).to_i
      count = Regexp.last_match(2).to_i
      count = 1 if count.zero?
      rows.concat((start_line...(start_line + count)).to_a)
    end.uniq.sort
  rescue StandardError
    []
  end

  def diff_stat_for(path, status_map = git_status_map)
    status = status_map[path]
    if status == "??"
      added = read(path).lines.size
      return {path: path, added: added, removed: 0}
    end

    output = run_command("git", "diff", "--numstat", "--", path).strip
    return {path: path, added: 0, removed: 0} if output.empty?

    added, removed, diff_path = output.split("\t", 3)
    {path: diff_path || path, added: added.to_i, removed: removed.to_i}
  rescue StandardError
    nil
  end

  def sha256_for(path)
    absolute = path.start_with?("/") ? path : File.join(REPO_ROOT, path)
    return nil unless File.file?(absolute)

    Digest::SHA256.hexdigest(File.binread(absolute))
  rescue StandardError
    nil
  end

  def truthy?(value)
    value.to_s.match?(/\A(true|1|yes|y)\z/i)
  end

  def generated_path?(relative_path)
    GENERATED_PATH_PREFIXES.any? { |prefix| relative_path.start_with?(prefix) }
  end

  def control_surface_class(relative_path)
    return "generated_control" if generated_path?(relative_path)
    return "agent_transient" if agent_transient_path?(relative_path)

    "live_truth"
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
