# frozen_string_literal: true
require "time"
module Dora; class ApprovalRecord
def self.validate!(r,operation:,scope:,now: Time.now.utc)
raise ArgumentError,"invalid approval" unless r.is_a?(Hash)&&r["kind"]=="dora_approval_record"&&r["version"].to_i==1
%w[id actor operation scope expires_at evidence rollback].each{|k| raise ArgumentError,"missing #{k}" unless r[k].is_a?(String)&&!r[k].empty?}
raise ArgumentError,"scope mismatch" unless r["operation"]==operation&&r["scope"]==scope
raise ArgumentError,"rollback evidence is required" unless r["rollback"].strip.length > 0
raise ArgumentError,"expired approval" unless Time.parse(r["expires_at"])>now
r.freeze
end; end; end
