# frozen_string_literal: true

require_relative "source_map"

module Dora
  module Plugins
    class TypeScriptVue
      def self.discover(root)
        SourceMap.relative_files_for_extensions(root, %w[ts vue]).map do |path|
          {"path" => path, "kind" => path.end_with?(".vue") ? "vue_component" : "typescript_source"}
        end
      end
    end
  end
end
