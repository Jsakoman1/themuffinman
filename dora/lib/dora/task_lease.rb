# frozen_string_literal: true

require "time"
require "yaml"
require "fileutils"

module Dora
  class TaskLease
    STATES = %w[active handed_off expired].freeze

    def self.load!(path, now: Time.now.utc)
      fail!("task lease registry is missing") unless File.file?(path)
      validate!(YAML.load_file(path), now: now)
    rescue Psych::Exception => error
      fail!("task lease registry YAML is invalid: #{error.message}")
    end

    def self.inspect!(path, now: Time.now.utc)
      load!(path, now: now)
    end

    def self.acquire!(path:, work_plan:, task:, holder:, expires_at:, now: Time.now.utc)
      mutate!(path) do |document|
        expire_elapsed!(document, now)
        validated = validate!(document, now: now)
        conflict = validated.fetch("leases").find { |lease| lease.fetch("state") != "expired" && lease.fetch("work_plan") == work_plan && lease.fetch("task") == task }
        fail!("task lease conflict: #{work_plan}##{task} is held by #{conflict.fetch("holder")}") if conflict
        lease = {"id" => "lease-#{task}-#{holder}-#{now.to_i}", "work_plan" => work_plan, "task" => task, "holder" => holder, "acquired_at" => now.utc.iso8601, "expires_at" => expires_at, "state" => "active"}
        document.fetch("leases") << lease
        validate!(document, now: now)
      end
    end

    def self.handoff!(path:, lease_id:, to:, reason:, now: Time.now.utc)
      mutate!(path) do |document|
        expire_elapsed!(document, now)
        validate!(document, now: now)
        lease = document.fetch("leases").find { |candidate| candidate["id"] == lease_id }
        fail!("task lease is not declared: #{lease_id}") unless lease
        fail!("task lease cannot hand off an expired record") unless lease["state"] == "active"
        fail!("task lease handoff must change holder") if lease["holder"] == to
        lease["handoff"] = {"from" => lease.fetch("holder"), "to" => to, "at" => now.utc.iso8601, "reason" => reason}
        lease["holder"] = to
        lease["state"] = "handed_off"
        validate!(document, now: now)
      end
    end

    def self.validate!(document, now: Time.now.utc)
      fail!("task lease registry must be a mapping") unless document.is_a?(Hash) && document["kind"] == "dora_task_lease_registry" && document["version"].to_i == 1
      leases = document["leases"]
      fail!("task lease registry leases must be a list") unless leases.is_a?(Array)
      normalized = leases.map { |lease| validate_lease!(lease, now) }
      ids = normalized.map { |lease| lease.fetch("id") }
      fail!("task lease ids must be unique") unless ids.uniq.length == ids.length
      current_claims = normalized.reject { |lease| lease.fetch("state") == "expired" }.map { |lease| [lease.fetch("work_plan"), lease.fetch("task")] }
      fail!("task lease conflict: more than one current holder claims the same task") unless current_claims.uniq.length == current_claims.length
      {"kind" => "dora_task_lease_registry", "version" => 1, "leases" => normalized, "authority_boundary" => "Task leases coordinate declared holders only. They do not start work, permit mutation, verify evidence, approve changes, or resolve conflicts outside the local registry."}.freeze
    end

    def self.mutate!(path)
      FileUtils.mkdir_p(File.dirname(File.expand_path(path)))
      File.open(path, File::RDWR | File::CREAT, 0o600) do |file|
        file.flock(File::LOCK_EX)
        content = file.read
        document = content.strip.empty? ? {"kind" => "dora_task_lease_registry", "version" => 1, "leases" => []} : YAML.safe_load(content)
        result = yield(document)
        file.rewind
        file.truncate(0)
        file.write(YAML.dump(document).sub(/\A---\n/, ""))
        file.flush
        file.fsync
        result
      ensure
        file.flock(File::LOCK_UN) if file
      end
    rescue Psych::Exception => error
      fail!("task lease registry YAML is invalid: #{error.message}")
    end
    private_class_method :mutate!

    def self.expire_elapsed!(document, now)
      Array(document["leases"]).each do |lease|
        next unless lease.is_a?(Hash) && lease["state"] != "expired"

        lease["state"] = "expired" if Time.iso8601(lease["expires_at"].to_s).utc <= now
      rescue ArgumentError
        # The subsequent contract validation reports the invalid timestamp deterministically.
      end
    end
    private_class_method :expire_elapsed!

    def self.validate_lease!(lease, now)
      fail!("task lease must be a mapping") unless lease.is_a?(Hash)
      %w[id work_plan task holder acquired_at expires_at state].each { |field| fail!("task lease is missing #{field}") unless lease.key?(field) }
      fail!("task lease id is invalid") unless identifier?(lease["id"])
      fail!("task lease work_plan is invalid") unless safe_relative_path?(lease["work_plan"])
      fail!("task lease task is invalid") unless identifier?(lease["task"])
      fail!("task lease holder is invalid") unless identifier?(lease["holder"])
      fail!("task lease state is invalid") unless STATES.include?(lease["state"])
      acquired_at = parse_time!(lease.fetch("acquired_at"), "acquired_at")
      expires_at = parse_time!(lease.fetch("expires_at"), "expires_at")
      fail!("task lease expires_at must be after acquired_at") unless expires_at > acquired_at
      fail!("task lease is expired; record it as expired or acquire a new lease") if lease["state"] != "expired" && expires_at <= now
      handoff = validate_handoff!(lease["handoff"], lease, acquired_at, expires_at)
      fail!("task lease handed_off state requires handoff") if lease["state"] == "handed_off" && handoff.nil?
      fail!("task lease active state cannot include handoff") if lease["state"] == "active" && handoff
      lease.slice("id", "work_plan", "task", "holder", "acquired_at", "expires_at", "state").merge("handoff" => handoff).compact
    end
    private_class_method :validate_lease!

    def self.validate_handoff!(handoff, lease, acquired_at, expires_at)
      return nil if handoff.nil?
      fail!("task lease handoff must be a mapping") unless handoff.is_a?(Hash)
      %w[from to at reason].each { |field| fail!("task lease handoff is missing #{field}") unless handoff.key?(field) }
      fail!("task lease handoff from is invalid") unless identifier?(handoff["from"])
      fail!("task lease handoff to is invalid") unless identifier?(handoff["to"])
      fail!("task lease handoff must change holder") if handoff["from"] == handoff["to"]
      fail!("task lease handoff to must match holder") unless handoff["to"] == lease["holder"]
      handoff_at = parse_time!(handoff.fetch("at"), "handoff at")
      fail!("task lease handoff is outside lease interval") unless handoff_at >= acquired_at && handoff_at < expires_at
      fail!("task lease handoff reason is invalid") unless statement?(handoff["reason"])
      handoff.slice("from", "to", "at", "reason")
    end
    private_class_method :validate_handoff!

    def self.parse_time!(value, label)
      Time.iso8601(value.to_s).utc
    rescue ArgumentError
      fail!("task lease #{label} is invalid")
    end
    private_class_method :parse_time!

    def self.identifier?(value)
      value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/)
    end
    private_class_method :identifier?

    def self.safe_relative_path?(value)
      value.is_a?(String) && !value.empty? && !value.start_with?("/") && !value.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
