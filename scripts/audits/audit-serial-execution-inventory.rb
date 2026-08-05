#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("../..", __dir__)
require File.join(ROOT, "dora/lib/dora/serial_plan_integrity")

master_path = ARGV.fetch(0)
inventory_path = ARGV.fetch(1)
Dora::SerialPlanIntegrity.validate!(master_path, inventory_path, project_root: ROOT)
puts "Serial execution inventory audit passed through Dora core."
