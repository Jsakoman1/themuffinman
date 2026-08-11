#!/usr/bin/env ruby
# frozen_string_literal: true

exec("bin/dora", "plugin-run", ".dora/plugins.yaml", "architecture-integrity")
