#!/usr/bin/env ruby
# frozen_string_literal: true

require_relative "./local_tooling_extended_tools"

LocalToolingExtendedTools.run("cleanup-generated-history", ARGV)
