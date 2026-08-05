# frozen_string_literal: true

require_relative "source_map"

module Dora
  module Plugins
    class JavaSpring
      def self.discover(root)
        SourceMap.relative_files(root, "**/*.java").map do |path|
          {"path" => path, "kind" => File.read(File.join(root, path)).include?("@SpringBootApplication") ? "spring_boot_application" : "java_source"}
        end
      end
    end
  end
end
