# frozen_string_literal: true

require "yaml"
require "digest"
require_relative "package_copy"

module Dora
  class BootstrapSource
    KIND = "dora_bootstrap_source"
    VERSION = 1
    IMMUTABLE_REF = /\A[0-9a-f]{40}\z/i
    CHECKSUM = /\A[0-9a-f]{64}\z/i

    def self.load!(descriptor_path)
      fail!("bootstrap source descriptor is missing") unless descriptor_path.is_a?(String) && File.file?(descriptor_path)

      descriptor = YAML.load_file(descriptor_path)
      validate!(descriptor, base_directory: File.dirname(File.expand_path(descriptor_path)))
    rescue Psych::Exception => error
      fail!("bootstrap source descriptor YAML is invalid: #{error.message}")
    end

    def self.validate!(descriptor, base_directory: Dir.pwd)
      fail!("bootstrap source descriptor is invalid") unless descriptor.is_a?(Hash) && descriptor["kind"] == KIND && descriptor["version"].to_i == VERSION

      source = descriptor["source"]
      fail!("bootstrap source must be a mapping") unless source.is_a?(Hash)
      fail!("bootstrap source path is required") unless source["path"].is_a?(String) && !source["path"].strip.empty?
      fail!("bootstrap source ref must be a forty-character immutable commit id") unless source["ref"].is_a?(String) && source["ref"].match?(IMMUTABLE_REF)
      fail!("bootstrap source cannot declare a remote URL") if remote_reference?(source["path"])

      root = File.realpath(File.expand_path(source["path"], base_directory))
      fail!("bootstrap source must be a local directory") unless File.directory?(root)
      %w[bin/dora lib/dora].each { |relative| fail!("bootstrap source is missing #{relative}") unless File.exist?(File.join(root, relative)) }
      checksum = source["checksum"]
      fail!("bootstrap source checksum is invalid") unless checksum.nil? || checksum.is_a?(String) && checksum.match?(CHECKSUM)
      fail!("bootstrap source checksum does not match reviewed content") if checksum && checksum.downcase != checksum_for(root)

      return {"path" => root, "ref" => source["ref"].downcase} unless checksum

      {"path" => root, "ref" => source["ref"].downcase, "checksum" => checksum.downcase, "integrity" => "verified"}
    rescue Errno::ENOENT
      fail!("bootstrap source path does not exist")
    end

    def self.remote_reference?(path)
      path.match?(%r{\A(?:https?|ssh|git)://}i) || path.match?(%r{\A[^/\s:]+@[^/\s:]+:})
    end
    private_class_method :remote_reference?

    def self.checksum_for(root)
      entries = Dir[File.join(root, "**/*")].select do |path|
        relative_parts = path.delete_prefix("#{root}/").split("/")
        File.file?(path) && (relative_parts & PackageCopy::EXCLUDED_SOURCE_ENTRIES).empty?
      end.sort
      Digest::SHA256.hexdigest(entries.map { |path| relative = path.delete_prefix("#{root}/"); "#{relative}\0#{Digest::SHA256.file(path).hexdigest}" }.join("\n"))
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
