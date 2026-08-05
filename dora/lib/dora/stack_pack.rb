# frozen_string_literal: true

require "fileutils"
require "yaml"

module Dora
  class StackPack
    def self.apply!(pack_path, project_root:)
      pack = YAML.load_file(pack_path)
      fail!("stack pack is invalid") unless pack["kind"] == "dora_stack_pack" && pack["version"].to_i == 1
      directories = Array(pack["directories"])
      files = pack.fetch("files")
      fail!("stack pack files must be a mapping") unless files.is_a?(Hash)
      (directories + files.keys).each { |relative| safe_path!(relative) }
      directories.each { |relative| FileUtils.mkdir_p(File.join(project_root, relative)) }
      files.each do |relative, content|
        path = File.join(project_root, relative)
        FileUtils.mkdir_p(File.dirname(path))
        File.write(path, YAML.dump(content).sub(/\A---\n/, ""))
      end
      {"id" => pack.fetch("id"), "directories" => directories, "files" => files.keys}
    end

    def self.safe_path!(relative)
      fail!("stack pack path must be project-relative") unless relative.is_a?(String) && !relative.empty? && !relative.start_with?("/") && !relative.split("/").include?("..")
    end
    private_class_method :safe_path!

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
