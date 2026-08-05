#!/usr/bin/env ruby
require "tmpdir"
require "fileutils"
require "yaml"
require_relative "../lib/dora/project_upgrade"
Dir.mktmpdir("dora-upgrade") do |root|
  FileUtils.mkdir_p(File.join(root,"dora")); File.write(File.join(root,"dora","old"),"old")
  FileUtils.mkdir_p(File.join(root,".dora")); File.write(File.join(root,".dora/bootstrap-source.yaml"),YAML.dump({"kind"=>"dora_bootstrap_record","version"=>1,"source"=>{"ref"=>"a"*40},"package_path"=>"dora"}))
  source=File.join(root,"source"); FileUtils.mkdir_p(File.join(source,"bin")); FileUtils.mkdir_p(File.join(source,"lib/dora")); File.write(File.join(source,"bin/dora"),"x")
  checksum=Dora::BootstrapSource.send(:checksum_for,source); desc=File.join(root,"source.yaml"); File.write(desc,YAML.dump({"kind"=>"dora_bootstrap_source","version"=>1,"source"=>{"path"=>source,"ref"=>"b"*40,"checksum"=>checksum}}))
  approval={"kind"=>"dora_approval_record","version"=>1,"id"=>"a","actor"=>"u","operation"=>"upgrade_apply","scope"=>root,"expires_at"=>"2030-01-01T00:00:00Z","evidence"=>"e","rollback"=>"b"}
  result=Dora::ProjectUpgrade.apply!(project_root:root,source_descriptor_path:desc,approval:approval); abort unless result["applied"]&&File.file?(File.join(root,"dora/bin/dora"))&&Dir.exist?(File.join(root,result["backup"]))
end
puts "Dora project upgrade apply test passed."
