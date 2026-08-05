# frozen_string_literal: true

require "yaml"
require_relative "starter_pack"

module Dora
  class PostgresStarter
    def self.validate!(path)
      pack = StarterPack.load!(path)
      fail!("Postgres starter id is invalid") unless pack["id"] == "spring-vue-postgres-buildable"
      %w[postgres flyway health].each { |field| fail!("Postgres starter is missing #{field}") unless statement?(pack[field]) }
      forbidden = %w[entity entities role roles api_resource seed_data product]
      fail!("Postgres starter must not declare product behavior") if forbidden.any? { |field| pack.key?(field) }
      pack
    end

    def self.statement?(value)
      value.is_a?(String) && !value.strip.empty?
    end
    private_class_method :statement?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
