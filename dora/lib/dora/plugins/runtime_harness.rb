# frozen_string_literal: true

require "yaml"

module Dora
  module Plugins
    class RuntimeHarness
      def self.contract!(path)
        trace = YAML.load_file(path)
        raise ArgumentError, "runtime trace kind is invalid" unless trace["kind"] == "dora_runtime_trace" && trace["version"].to_i == 1
        %w[command evidence_path viewport].each { |field| raise ArgumentError, "runtime trace is missing #{field}" if trace[field].to_s.empty? }
        raise ArgumentError, "runtime trace evidence_path must be relative" if trace["evidence_path"].start_with?("/")
        trace.slice("command", "evidence_path", "viewport")
      end
    end
  end
end
