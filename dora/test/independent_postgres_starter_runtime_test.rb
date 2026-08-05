#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "open3"
require "socket"
require "tmpdir"
require "yaml"
require_relative "../lib/dora/starter_pack"

ROOT = File.expand_path("..", __dir__)
DOCKER_BIN = "/Applications/Docker.app/Contents/Resources/bin/docker"

def run_command!(root, command, environment)
  output, status = Open3.capture2e(environment, "/bin/zsh", "-lc", command, chdir: root)
  raise "independent Postgres consumer command failed: #{command}\n#{output}" unless status.success?
  output
end

def free_port
  TCPServer.open("127.0.0.1", 0) { |server| server.addr[1] }
end

def process_running?(pid)
  Process.kill(0, pid)
  true
rescue Errno::ESRCH, Errno::EPERM
  false
end

abort "Docker Desktop CLI is unavailable at #{DOCKER_BIN}" unless File.executable?(DOCKER_BIN)

Dir.mktmpdir("dora-executable-postgres-consumer") do |sandbox|
  project = File.join(sandbox, "neutral-postgres-consumer")
  FileUtils.mkdir_p(File.join(project, ".dora"))
  Dora::StarterPack.apply!(File.join(ROOT, "starters/spring-vue-postgres-buildable.yaml"), project_root: project)
  commands = YAML.load_file(File.join(project, ".dora/project-commands.yaml")).fetch("commands")
  backend_log = File.join(project, "backend.log")
  backend_pid = nil
  compose_started = false
  database_port = free_port
  backend_port = free_port
  environment = {"PATH" => "#{File.dirname(DOCKER_BIN)}:#{ENV.fetch("PATH")}", "COMPOSE_PROJECT_NAME" => "dora_runtime_#{Process.pid}", "SERVER_PORT" => backend_port.to_s}
  File.write(File.join(project, ".env"), "POSTGRES_DB=app\nPOSTGRES_USER=app\nPOSTGRES_PASSWORD=dora-runtime-proof-secret\nPOSTGRES_PORT=#{database_port}\n")

  begin
    run_command!(project, commands.fetch("database"), environment)
    compose_started = true
    30.times do
      output, status = Open3.capture2e(environment, DOCKER_BIN, "compose", "--env-file", ".env", "exec", "-T", "postgres", "pg_isready", "-U", "app", "-d", "app", chdir: project)
      break if status.success? && output.include?("accepting connections")
      sleep 1
    end
    _output, ready = Open3.capture2e(environment, DOCKER_BIN, "compose", "--env-file", ".env", "exec", "-T", "postgres", "pg_isready", "-U", "app", "-d", "app", chdir: project)
    raise "temporary PostgreSQL did not become ready" unless ready.success?

    backend_pid = Process.spawn(environment, "/bin/zsh", "-lc", commands.fetch("run_backend"), chdir: project, out: backend_log, err: backend_log, pgroup: true)
    health_output = nil
    45.times do
      output, status = Open3.capture2e(environment, "/bin/zsh", "-lc", commands.fetch("health"), chdir: project)
      if status.success?
        health_output = output
        break
      end
      raise "generated backend stopped before health check\n#{File.read(backend_log)}" unless process_running?(backend_pid)
      sleep 1
    end
    raise "generated backend did not return health\n#{File.read(backend_log)}" unless health_output&.include?("UP")
    abort "Compose command is not declared" unless commands.fetch("database").include?("docker compose")
    abort "health command is not declared" unless commands.fetch("health").include?("actuator/health")

    content = Dir[File.join(project, "**", "*")].select { |path| File.file?(path) }.map { |path| File.binread(path).downcase }.join("\n")
    %w[muffinman inventoryitem reservation booking].each { |term| abort "executable consumer leaked product term: #{term}" if content.include?(term) }
  ensure
    if backend_pid
      begin
        Process.kill("TERM", -backend_pid) if process_running?(backend_pid)
        Process.wait(backend_pid)
      rescue Errno::ESRCH, Errno::ECHILD, Errno::EPERM
        nil
      end
    end
    Open3.capture2e(environment, DOCKER_BIN, "compose", "--env-file", ".env", "down", "--volumes", "--remove-orphans", chdir: project) if compose_started
  end
end

puts "Dora executable Postgres consumer test passed (fresh temporary PostgreSQL, generated backend, and declared health command passed; scoped resources were cleaned up)."
