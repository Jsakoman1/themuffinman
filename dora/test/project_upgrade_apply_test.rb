#!/usr/bin/env ruby
require "tmpdir"
require "fileutils"
require "yaml"
require_relative "../lib/dora/project_upgrade"
Dir.mktmpdir("dora-upgrade") do |root|
  FileUtils.mkdir_p(File.join(root,"dora")); File.write(File.join(root,"dora","old"),"old")
  FileUtils.mkdir_p(File.join(root,".dora")); File.write(File.join(root,".dora/bootstrap-source.yaml"),YAML.dump({"kind"=>"dora_bootstrap_record","version"=>1,"source"=>{"ref"=>"a"*40},"package_path"=>"dora"}))
  source=File.join(root,"source"); FileUtils.mkdir_p(File.join(source,"bin")); FileUtils.mkdir_p(File.join(source,"lib/dora")); FileUtils.mkdir_p(File.join(source,".git")); FileUtils.mkdir_p(File.join(source,".idea")); File.write(File.join(source,"bin/dora"),"x"); File.write(File.join(source,".git/config"),"private"); File.write(File.join(source,".idea/workspace.xml"),"local")
  checksum=Dora::BootstrapSource.send(:checksum_for,source); desc=File.join(root,"source.yaml"); File.write(desc,YAML.dump({"kind"=>"dora_bootstrap_source","version"=>1,"source"=>{"path"=>source,"ref"=>"b"*40,"checksum"=>checksum}}))
  approval={"kind"=>"dora_approval_record","version"=>1,"id"=>"a","actor"=>"u","operation"=>"upgrade_apply","scope"=>root,"expires_at"=>"2030-01-01T00:00:00Z","evidence"=>"e","rollback"=>"b"}
  result=Dora::ProjectUpgrade.apply!(project_root:root,source_descriptor_path:desc,approval:approval); abort unless result["applied"]&&File.file?(File.join(root,"dora/bin/dora"))&&!File.exist?(File.join(root,"dora/.git"))&&!File.exist?(File.join(root,"dora/.idea"))&&Dir.exist?(File.join(root,result["backup"]))&&File.file?(File.join(root,"#{result["backup"]}.bootstrap-source.yaml"))
  pin=YAML.load_file(File.join(root,".dora/bootstrap-source.yaml")); abort unless pin.dig("source","ref")=="b"*40&&pin.dig("source","checksum")==checksum&&!pin.dig("source").key?("path")&&result.dig("target","ref")=="b"*40
  rollback=Dora::ProjectUpgrade.rollback!(project_root:root,backup:result["backup"],approval:approval.merge("operation"=>"upgrade_rollback")); restored=YAML.load_file(File.join(root,".dora/bootstrap-source.yaml")); abort unless rollback["restored_source_pin"]&&restored.dig("source","ref")=="a"*40
end
puts "Dora project upgrade apply test passed."
