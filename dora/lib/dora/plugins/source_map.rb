# frozen_string_literal: true

module Dora
  module Plugins
    class SourceMap
      def self.relative_files_for_extensions(root, extensions)
        pattern = "**/*.{#{extensions.join(",")}}"
        relative_files(root, pattern)
      end

      def self.relative_files(root, pattern)
        Dir[File.join(root, pattern)].select { |path| File.file?(path) }.map { |path| path.delete_prefix("#{root}/") }.sort
      end
    end
  end
end
