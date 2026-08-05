# frozen_string_literal: true

module Dora
  module Plugins
    class VueNavigation
      def self.analyze_declared!(root:, inputs:)
        analyze!(root: root, router_path: inputs.fetch("router_path"), navigation_paths: inputs.fetch("navigation_paths"), required_surfaces: inputs.fetch("required_surfaces", []))
      rescue KeyError => error
        fail!("Vue navigation inputs are incomplete: #{error.message}")
      end

      def self.analyze!(root:, router_path:, navigation_paths:, required_surfaces:)
        root = File.expand_path(root)
        paths = [router_path, *Array(navigation_paths)]
        fail!("navigation path is invalid") unless paths.all? { |path| safe_relative_path?(path) }
        router = read_under!(root, router_path)
        navigation = Array(navigation_paths).map { |path| read_under!(root, path) }.join("\n")
        routes = router.scan(/path:\s*['\"]([^'\"]+)/).flatten.uniq.sort
        surfaces = Array(required_surfaces).map do |surface|
          {"id" => surface, "in_navigation" => navigation.include?(surface), "in_router" => routes.any? { |route| route == "/#{surface}" || route.start_with?("/#{surface}/") }}
        end
        {"routes" => routes, "surfaces" => surfaces}
      end

      def self.read_under!(root, relative)
        path = File.expand_path(relative, root)
        fail!("declared navigation path is missing") unless path.start_with?("#{root}/") && File.file?(path)
        File.read(path)
      end
      private_class_method :read_under!

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
