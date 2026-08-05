# frozen_string_literal: true

module Dora
  module Plugins
    class VueSurfaceHygiene
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
