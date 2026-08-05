#!/usr/bin/env ruby
# frozen_string_literal: true

require "fileutils"
require "json"
require "open3"
require "socket"
require "tmpdir"
require "yaml"

ROOT = File.expand_path("..", __dir__)
CLI = File.join(ROOT, "bin/dora")
fixture = YAML.load_file(File.join(ROOT, "test/fixtures/playwright-runtime-execution.yaml"))
evidence_path = File.expand_path("../#{fixture.fetch("evidence_path")}", ROOT)
port = TCPServer.open("127.0.0.1", 0) { |server| server.addr[1] }
server_script = <<~JS
  const http = require('http');
  http.createServer((request, response) => {
    if (request.url === '/health') { response.writeHead(200, {'content-type': 'application/json'}); response.end('{"dora":"technical-health"}'); return; }
    response.writeHead(404); response.end();
  }).listen(process.env.DORA_TEST_PORT, '127.0.0.1');
JS

Dir.mktmpdir("dora-playwright-runtime") do |root|
  profile = File.join(root, "runtime-proof")
  apply_output, apply_status = Open3.capture2e(CLI, "runtime-profile-apply", "runtime-proof", "--apply", chdir: root)
  abort "runtime profile apply failed: #{apply_output}" unless apply_status.success?
  install_output, install_status = Open3.capture2e("npm", "install", "--ignore-scripts", chdir: profile)
  abort "Playwright package installation failed: #{install_output}" unless install_status.success?
  browser_output, browser_status = Open3.capture2e("npx", "playwright", "install", "chromium", chdir: profile)
  abort "Playwright browser installation failed: #{browser_output}" unless browser_status.success?
  pid = Process.spawn({"DORA_TEST_PORT" => port.to_s}, "node", "-e", server_script, out: File::NULL, err: File::NULL)
  begin
    sleep 0.5
    browser_script = <<~JS
      const { chromium } = require('@playwright/test');
      (async () => {
        const browser = await chromium.launch({headless: true});
        try {
          const page = await browser.newPage();
          await page.goto(process.env.DORA_RUNTIME_BASE_URL + '/health');
          const body = await page.textContent('body');
          if (body !== '{"dora":"technical-health"}') throw new Error('unexpected technical health body: ' + body);
        } finally { await browser.close(); }
      })().catch((error) => { console.error(error); process.exit(1); });
    JS
    output, status = Open3.capture2e({"DORA_RUNTIME_BASE_URL" => "http://127.0.0.1:#{port}"}, "node", "-e", browser_script, chdir: profile)
    abort "neutral Chromium runtime test failed: #{output}" unless status.success?
    FileUtils.mkdir_p(File.dirname(evidence_path))
    File.write(evidence_path, JSON.pretty_generate({"kind" => "dora_runtime_evidence", "version" => 1, "profile" => fixture.fetch("profile"), "base_url" => "http://127.0.0.1:#{port}", "endpoint" => fixture.fetch("endpoint"), "observed_body" => fixture.fetch("expected_body"), "result" => "passed", "boundary" => fixture.fetch("boundary")}))
  ensure
    begin
      Process.kill("TERM", pid)
      Process.wait(pid)
    rescue Errno::ESRCH, Errno::ECHILD
      nil
    end
  end
end

abort "runtime evidence was not written" unless File.file?(evidence_path)
puts "Dora Playwright runtime execution test passed (approved isolated browser proof recorded fresh technical-health evidence)."
