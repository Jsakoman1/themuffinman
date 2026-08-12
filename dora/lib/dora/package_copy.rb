# frozen_string_literal: true

require "fileutils"

module Dora
  class PackageCopy
    EXCLUDED_SOURCE_ENTRIES = %w[.DS_Store .git .idea].freeze

    def self.copy!(source_root:, destination:)
      root = File.realpath(source_root)
      fail!("Dora package source is not a directory") unless Dir.exist?(root)
      fail!("Dora package destination is not a directory") unless Dir.exist?(destination)

      entries = Dir.children(root).reject { |entry| EXCLUDED_SOURCE_ENTRIES.include?(entry) }.sort
      FileUtils.cp_r(entries.map { |entry| File.join(root, entry) }, destination) unless entries.empty?
      true
    rescue Errno::ENOENT
      fail!("Dora package source is unavailable")
    end

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
