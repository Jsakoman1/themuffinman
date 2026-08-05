# frozen_string_literal: true

require "yaml"

module Dora
  class PlanCoverage
    def self.review!(registry_path)
      registry = YAML.load_file(registry_path)
      fail!("plan registry kind is invalid") unless registry.is_a?(Hash) && registry["kind"] == "dora_plan_registry" && registry["version"].to_i == 1
      plans = Array(registry["plans"])
      fail!("plan registry has no declared plans") if plans.empty?
      ids = plans.map { |entry| entry["id"] }
      fail!("plan registry ids must be unique and non-empty") if ids.any? { |id| id.to_s.strip.empty? } || ids.uniq.length != ids.length

      root = File.dirname(File.expand_path(registry_path))
      plans.map do |entry|
        path = entry["path"]
        allowed_statuses = Array(entry["allowed_statuses"])
        fail!("plan registry entry #{entry["id"]} has an invalid path") unless safe_relative_path?(path)
        fail!("plan registry entry #{entry["id"]} has no allowed statuses") if allowed_statuses.empty?
        plan_path = File.expand_path(path, root)
        fail!("plan registry entry #{entry["id"]} is missing: #{path}") unless plan_path.start_with?("#{root}/") && File.file?(plan_path)
        plan = YAML.load_file(plan_path)
        fail!("plan registry entry #{entry["id"]} has invalid plan kind") unless %w[work master].include?(plan["kind"])
        status = plan["status"].to_s
        fail!("plan registry entry #{entry["id"]} has disallowed status #{status}") unless allowed_statuses.include?(status)
        {"id" => entry["id"], "path" => path, "status" => status}
      end
    rescue Psych::Exception => error
      fail!("plan registry YAML is invalid: #{error.message}")
    end

    def self.safe_relative_path?(path)
      path.is_a?(String) && !path.empty? && !path.start_with?("/") && !path.split("/").include?("..")
    end
    private_class_method :safe_relative_path?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
