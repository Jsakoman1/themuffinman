#!/usr/bin/env ruby
# frozen_string_literal: true
require "yaml"
root = File.expand_path("..", __dir__)
pack = YAML.load_file(File.join(root, "starters/spring-vue-postgres-buildable.yaml"))
abort "Postgres starter is not technical only" unless pack["technical_only"] && pack["id"] == "spring-vue-postgres-buildable"
%w[docker-compose.yml .env.example backend/pom.xml backend/src/main/java/example/TechnicalApplication.java backend/src/main/resources/application.properties frontend/package.json].each { |path| abort "starter omits #{path}" unless File.file?(File.join(root, "templates/starters/spring-vue-postgres", path)) }
content = Dir[File.join(root, "templates/starters/spring-vue-postgres/**/*")].select { |path| File.file?(path) }.map { |path| File.read(path) }.join
%w[Flyway postgres actuator spring-boot-starter-web].each { |term| abort "starter omits #{term}" unless content.downcase.include?(term.downcase) }
commands = pack.fetch("commands")
abort "starter omits local backend run command" unless commands.fetch("run_backend").include?("spring-boot:run")
abort "starter health command ignores SERVER_PORT" unless commands.fetch("health").include?("SERVER_PORT")
abort "starter contains product entity" if content.downcase.include?("inventoryitem")
puts "Dora Spring/Vue/Postgres buildable starter test passed (technical PostgreSQL, Flyway, health, and no product behavior)."
