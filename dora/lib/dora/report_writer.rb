# frozen_string_literal: true

require "fileutils"
require "json"

module Dora
  class ReportWriter
    def self.write_json!(root:, relative_path:, payload:)
      write!(root: root, relative_path: relative_path, content: JSON.pretty_generate(payload) + "\n")
    end

    def self.write_text!(root:, relative_path:, content:)
      fail!("report content must be a string") unless content.is_a?(String)
      write!(root: root, relative_path: relative_path, content: content)
    end

    def self.write!(root:, relative_path:, content:)
      fail!("report path must be project-relative") unless safe_relative_path?(relative_path)
      project_root = File.expand_path(root)
      destination = File.expand_path(relative_path, project_root)
      fail!("report path resolves outside project root") unless destination.start_with?("#{project_root}/")
      FileUtils.mkdir_p(File.dirname(destination))
      atomic_write(destination, content)
      relative_path
    end
    private_class_method :write!

    def self.atomic_write(destination, content)
      temporary = "#{destination}.tmp.#{$$}.#{Thread.current.object_id.to_s(36)}"
      File.open(temporary, "w") { |file| file.write(content); file.flush; file.fsync }
      File.rename(temporary, destination)
    ensure
      FileUtils.rm_f(temporary) if temporary && File.exist?(temporary)
    end
    private_class_method :atomic_write

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
