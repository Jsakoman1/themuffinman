#!/usr/bin/env ruby
# frozen_string_literal: true

exec("dora/bin/dora", "plugin-run", ".dora/plugins.yaml", "http-contract-linker")
