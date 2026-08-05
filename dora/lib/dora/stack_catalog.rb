# frozen_string_literal: true

require "yaml"
require_relative "starter_pack"

module Dora
  class StackCatalog
    def self.load!(path = File.expand_path("../../stack-catalog.yaml", __dir__))
      catalog = YAML.load_file(path)
      fail!("stack catalog is invalid") unless catalog.is_a?(Hash) && catalog["kind"] == "dora_stack_catalog" && catalog["version"].to_i == 1 && catalog["stacks"].is_a?(Array) && !catalog["stacks"].empty?
      stacks = catalog.fetch("stacks").map do |stack|
        fail!("stack catalog entry is invalid") unless stack.is_a?(Hash) && identifier?(stack["id"]) && safe_relative_path?(stack["starter_pack"]) && Array(stack["capabilities"]).all? { |item| statement?(item) } && !Array(stack["capabilities"]).empty? && Array(stack["exclusions"]).all? { |item| statement?(item) } && !Array(stack["exclusions"]).empty?
        starter = File.expand_path(stack.fetch("starter_pack"), File.dirname(path))
        StarterPack.load!(starter)
        stack.slice("id", "starter_pack", "capabilities", "exclusions")
      end
      fail!("stack catalog ids must be unique") unless stacks.map { |stack| stack.fetch("id") }.uniq.length == stacks.length
      {"kind" => "dora_stack_catalog", "version" => 1, "stacks" => stacks, "completion_boundary" => "The catalog declares neutral technical starter choices only; it does not provide product logic, validate a stack, or apply a starter."}.freeze
    rescue Psych::Exception => error
      fail!("stack catalog YAML is invalid: #{error.message}")
    end

    def self.find!(id, path = File.expand_path("../../stack-catalog.yaml", __dir__))
      stack = load!(path).fetch("stacks").find { |candidate| candidate.fetch("id") == id }
      fail!("unknown neutral stack: #{id}") unless stack
      stack
    end

    def self.identifier?(value); value.is_a?(String) && value.match?(/\A[a-z][a-z0-9-]*\z/); end
    private_class_method :identifier?
    def self.statement?(value); value.is_a?(String) && !value.strip.empty?; end
    private_class_method :statement?
    def self.safe_relative_path?(value); statement?(value) && !value.start_with?("/") && !value.split("/").include?(".."); end
    private_class_method :safe_relative_path?
    def self.fail!(message); raise ArgumentError, message; end
    private_class_method :fail!
  end
end
