# frozen_string_literal: true

module Dora
  module Plugins
    class SpringConfigurationDrift
      def self.analyze_declared!(root:, inputs:)
        analyze!(root: root, properties_path: inputs.fetch("properties_path"), property_prefixes: inputs.fetch("property_prefixes"))
      rescue KeyError => error
        fail!("Spring configuration inputs are incomplete: #{error.message}")
      end

      def self.analyze!(root:, properties_path:, property_prefixes:)
        fail!("properties path is invalid") unless safe_relative_path?(properties_path)
        fail!("property prefixes must be a non-empty list") unless property_prefixes.is_a?(Array) && !property_prefixes.empty?
        absolute = File.expand_path(properties_path, root)
        fail!("properties path resolves outside declared root") unless absolute.start_with?("#{File.expand_path(root)}/") && File.file?(absolute)

        properties = File.readlines(absolute, chomp: true).map do |line|
          next if line.strip.empty? || line.lstrip.start_with?("#")

          name, value = line.split("=", 2)
          next if name.to_s.empty?

          {"name" => name, "environment_name" => value.to_s[/\$\{([^}:]+)(?::[^}]*)?\}/, 1], "secret" => name.match?(/password|secret|api-key|access-key|secret-key/i)}
        end.compact
        tracked = properties.select { |property| property_prefixes.any? { |prefix| property["name"].start_with?(prefix) } }
        {"properties" => properties, "unmapped" => tracked.select { |property| property["environment_name"].nil? }}
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
end
