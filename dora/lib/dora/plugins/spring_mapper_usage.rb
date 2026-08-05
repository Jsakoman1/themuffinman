# frozen_string_literal: true

module Dora
  module Plugins
    class SpringMapperUsage
      def self.analyze_declared!(root:, inputs:)
        analyze!(root: root, mapper_glob: inputs.fetch("mapper_glob"), source_root: inputs.fetch("source_root"))
      rescue KeyError => error
        fail!("Spring mapper inputs are incomplete: #{error.message}")
      end

      def self.analyze!(root:, mapper_glob:, source_root:)
        fail!("mapper glob is invalid") unless safe_relative_path?(mapper_glob)
        fail!("source root is invalid") unless safe_relative_path?(source_root)
        absolute_root = File.expand_path(root)
        mapper_paths = Dir[File.join(absolute_root, mapper_glob)].select { |path| File.file?(path) }.sort
        source_path = File.expand_path(source_root, absolute_root)
        fail!("source root resolves outside declared root") unless source_path.start_with?("#{absolute_root}/") && Dir.exist?(source_path)
        mapper_types = mapper_paths.map { |path| File.basename(path, ".java") }
        source_paths = Dir[File.join(source_path, "**/*.java")].select { |path| File.file?(path) && !mapper_paths.include?(path) }.sort
        calls = source_paths.flat_map { |path| calls_in(path, mapper_types, absolute_root) }

        mapper_paths.map do |path|
          mapper = File.basename(path, ".java")
          content = File.read(path)
          {"mapper" => mapper, "path" => relative(path, absolute_root), "methods" => content.scan(/public\s+[A-Za-z0-9_<>, ?\[\]]+\s+([a-zA-Z0-9_]+)\s*\(/).flatten.uniq.sort, "usage_count" => calls.count { |call| call["mapper"] == mapper }, "callers" => calls.select { |call| call["mapper"] == mapper }}
        end
      end

      def self.calls_in(path, mapper_types, root)
        content = File.read(path)
        fields = content.scan(/private final ([A-Za-z0-9_]+) ([a-zA-Z0-9_]+);/).select { |type, _field| mapper_types.include?(type) }
        fields.flat_map do |mapper, field|
          content.scan(/#{Regexp.escape(field)}\.([a-zA-Z0-9_]+)\s*\(/).flatten.map do |method|
            {"mapper" => mapper, "method" => method, "file" => relative(path, root)}
          end
        end
      end
      private_class_method :calls_in

      def self.relative(path, root)
        path.delete_prefix("#{root}/")
      end
      private_class_method :relative

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
