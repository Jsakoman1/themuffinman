#!/usr/bin/env ruby
# frozen_string_literal: true

root = File.expand_path("..", __dir__)
operator = File.read(File.join(root, "docs/operator-guide.md"))
agent = File.read(File.join(root, "docs/agent-first-application-guide.md"))

%w[spring-vue-postgres-buildable Flyway docker\ compose actuator/health authentication backups vertical-slice\ proposal independent_postgres_starter_consumer_test.rb independent_vertical_slice_consumer_test.rb].each do |phrase|
  abort "operator guide is missing #{phrase}" unless operator.include?(phrase)
end
%w[spring-vue-postgres-buildable dora_confirmed_capability_context data-safety workflow permission technical vertical-slice\ generator readiness\ gate not\ source\ code authentication retention backup].each do |phrase|
  abort "agent guide is missing #{phrase}" unless agent.include?(phrase)
end
abort "guidance promises automatic product implementation" if [operator, agent].join("\n").match?(/Dora (automatically )?(creates|implements) product/i)

puts "Dora web-app foundation guidance test passed (current route, deferred decisions, proposal boundary, and independent proof commands are documented)."
