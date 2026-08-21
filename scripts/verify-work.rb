#!/usr/bin/env ruby
# frozen_string_literal: true

ROOT = File.expand_path("..", __dir__)
exec File.join(ROOT, "bin/dora"), "work-verify", File.join(ROOT, ".dora/project.yaml"), *ARGV
