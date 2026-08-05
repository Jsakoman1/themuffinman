#!/usr/bin/env ruby
require "yaml"
require_relative "../lib/dora/approval_record"
f=YAML.load_file(File.expand_path("fixtures/guidance-correctness-project.yaml",__dir__))
base={"kind"=>"dora_approval_record","version"=>1,"id"=>"x","actor"=>"consumer","evidence"=>"fixture","rollback"=>"backup"}
[[f.fetch("approval"), "expired approval accepted"], [f.fetch("wrong_scope_approval"), "wrong scoped approval accepted"]].each do |approval, message|
  begin; Dora::ApprovalRecord.validate!(base.merge(approval),operation:"upgrade",scope:"project"); abort message; rescue ArgumentError; end
end
puts "Dora independent guidance correctness consumer test passed (expired and wrongly scoped approvals are rejected without completion inference)."
