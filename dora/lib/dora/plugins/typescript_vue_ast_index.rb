# frozen_string_literal: true

require "json"
require "open3"

module Dora
  module Plugins
    class TypeScriptVueAstIndex
      TOOL = File.expand_path("../../../tools/typescript-vue-ast-index.mjs", __dir__)

      def self.index!(root:, source_roots:, package_root:, parser_module: "@babel/parser")
        paths = Array(source_roots).map { |source_root| source_root.fetch("path") }
        fail!("TypeScript/Vue AST index requires at least one source root") if paths.empty?
        command = ["node", TOOL, "--project-root", File.expand_path(root), "--package-root", package_root, "--parser-module", parser_module]
        paths.each { |path| command.concat(["--source-root", path]) }
        output, status = Open3.capture2e(*command, chdir: root)
        fail!("TypeScript/Vue AST index failed:\n#{output}") unless status.success?
        JSON.parse(output)
      rescue KeyError => error
        fail!("TypeScript/Vue AST source root is incomplete: #{error.message}")
      rescue JSON::ParserError => error
        fail!("TypeScript/Vue AST index returned invalid JSON: #{error.message}")
      end

      def self.fail!(message)
        raise ArgumentError, message
      end
      private_class_method :fail!
    end
  end
end
