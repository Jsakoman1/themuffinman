# frozen_string_literal: true

require "json"
require "open3"

module Dora
  module Plugins
    class JavaAstIndex
      TOOL = File.expand_path("../../../tools/java-ast-index/RepositoryJavaAstIndex.java", __dir__)

      def self.index!(root:, source_roots:)
        paths = Array(source_roots).map { |source_root| source_root.fetch("path") }
        fail!("Java AST index requires at least one source root") if paths.empty?
        command = ["java", TOOL, "--project-root", File.expand_path(root)]
        paths.each { |path| command.concat(["--source-root", path]) }
        output, status = Open3.capture2e(*command, chdir: root)
        fail!("Java AST index failed:\n#{output}") unless status.success?
        JSON.parse(output)
      rescue KeyError => error
        fail!("Java AST source root is incomplete: #{error.message}")
      rescue JSON::ParserError => error
        fail!("Java AST index returned invalid JSON: #{error.message}")
      end

      def self.fail!(message)
        raise ArgumentError, message
      end
      private_class_method :fail!
    end
  end
end
