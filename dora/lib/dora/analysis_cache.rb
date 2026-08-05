# frozen_string_literal: true

require "digest"
require "fileutils"
require "json"

module Dora
  class AnalysisCache
    def self.fetch!(cache_root:, key:, input:, version: 2)
      fail!("cache key is invalid") unless key.is_a?(String) && key.match?(/\A[a-z][a-z0-9_-]*\z/)
      fail!("cache version is invalid") unless version.is_a?(Integer) && version.positive?
      digest = Digest::SHA256.hexdigest(JSON.generate(canonical(input)))
      root = File.expand_path(cache_root)
      FileUtils.mkdir_p(root)
      path = File.join(root, "#{key}-v#{version}-#{digest}.json")
      if File.file?(path)
        cached = JSON.parse(File.read(path))
        return {"value" => cached.fetch("value"), "cache" => {"hit" => true, "key" => key, "input_digest" => digest, "version" => version}}.freeze
      end

      fail!("cache miss requires an analysis block") unless block_given?
      value = yield
      payload = {"kind" => "dora_analysis_cache_entry", "version" => version, "key" => key, "input_digest" => digest, "value" => value}
      File.write(path, JSON.generate(payload))
      {"value" => value, "cache" => {"hit" => false, "key" => key, "input_digest" => digest, "version" => version}}.freeze
    end

    def self.canonical(value)
      case value
      when Hash then value.keys.map(&:to_s).sort.to_h { |key| [key, canonical(value[key] || value[key.to_sym])] }
      when Array then value.map { |item| canonical(item) }
      else value
      end
    end
    private_class_method :canonical

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
