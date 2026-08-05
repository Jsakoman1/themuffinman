# frozen_string_literal: true

module Dora
  module Plugins
    class VueSurfaceHygiene
      def self.analyze_declared!(root:, inputs:)
        hygiene = scan!(root: root, source_glob: inputs.fetch("source_glob"), required_markers: inputs.fetch("required_markers", []))
        files = hygiene.fetch("files").map { |path| [path, File.read(File.join(root, path))] }.to_h
        forbidden = Array(inputs.fetch("forbidden_markers", [])).flat_map do |marker|
          files.select { |_path, content| content.include?(marker) }.keys.map { |path| {"marker" => marker, "path" => path} }
        end
        required_read_markers = Array(inputs.fetch("required_read_markers", []))
        required_entrypoints = Array(inputs.fetch("required_entrypoints", []))
        {
          "hygiene" => hygiene,
          "stale_markers" => forbidden,
          "read_markers" => required_read_markers.map { |marker| {"marker" => marker, "present" => files.values.any? { |content| content.include?(marker) }} },
          "entrypoints" => required_entrypoints.map { |marker| {"marker" => marker, "present" => files.values.any? { |content| content.include?(marker) }} }
        }
      rescue KeyError => error
        fail!("Vue surface inputs are incomplete: #{error.message}")
      end

      def self.scan!(root:, source_glob:, required_markers: [])
        fail!("source glob is invalid") unless safe_relative_path?(source_glob)
        root = File.expand_path(root)
        files = Dir[File.join(root, source_glob)].select { |path| File.file?(path) }.sort
        fail!("declared source glob has no files") if files.empty?
        markers = Array(required_markers)
        {"files" => files.map { |path| path.delete_prefix("#{root}/") }, "missing_markers" => markers.reject { |marker| files.any? { |path| File.read(path).include?(marker) } }}
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
