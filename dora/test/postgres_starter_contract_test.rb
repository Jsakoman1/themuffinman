#!/usr/bin/env ruby
# frozen_string_literal: true

require "tmpdir"
require "yaml"
require "fileutils"
require_relative "../lib/dora/postgres_starter"

pack = {"kind" => "dora_starter_pack", "version" => 1, "id" => "spring-vue-postgres-buildable", "technical_only" => true, "template_root" => "templates/starter", "commands" => {"setup" => "true", "test" => "true", "build" => "true"}, "directories" => [], "files" => {}, "postgres" => "Docker Compose PostgreSQL configuration.", "flyway" => "Append-only Flyway migrations.", "health" => "Spring health endpoint.", "boundaries" => ["No product behavior."]}
Dir.mktmpdir("dora-postgres-contract") do |root|
  FileUtils.mkdir_p(File.join(root, "templates/starter"))
  path = File.join(root, "starter.yaml"); File.write(path, YAML.dump(pack))
  result = Dora::PostgresStarter.validate!(path)
  abort "contract lost Flyway declaration" unless result.fetch("flyway").include?("Flyway")
  invalid = Marshal.load(Marshal.dump(pack)); invalid["entity"] = "item"; File.write(path, YAML.dump(invalid))
  begin; Dora::PostgresStarter.validate!(path); abort "product behavior accepted"; rescue ArgumentError => error; abort error.message unless error.message.include?("product"); end
end
puts "Dora Postgres starter contract test passed (technical database setup is required and product behavior is rejected)."
