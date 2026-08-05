# frozen_string_literal: true

require "fileutils"

module Dora
  class ProjectLauncher
    def self.write!(project_root, template_path:, package_relative: "dora")
      fail!("Dora package path must be project-relative") unless package_relative.is_a?(String) && !package_relative.empty? && !package_relative.start_with?("/") && !package_relative.split("/").include?("..")
      destination = File.join(project_root, "bin/dora")
      FileUtils.mkdir_p(File.dirname(destination))
      content = File.read(template_path).gsub("{{dora_package_path}}", package_relative)
      File.write(destination, content)
      FileUtils.chmod(0o755, destination)
      "bin/dora"
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
