#!/usr/bin/env ruby
# frozen_string_literal: true
require_relative "./local_tooling_batch_audits"
LocalToolingBatchAudits.run("style_token_usage")
