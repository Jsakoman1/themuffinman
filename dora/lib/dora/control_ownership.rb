# frozen_string_literal: true

module Dora
  class ControlOwnership
    def self.classify!(document, subjects)
      fail!("control ownership document must be a mapping") unless document.is_a?(Hash)
      rules = Array(document["rules"])
      fail!("control ownership document has no rules") if rules.empty?
      allowed_owners = Array(document["allowed_owners"] || document.dig("classification_contract", "allowed_owners"))
      fail!("control ownership document has no allowed owners") if allowed_owners.empty?
      ids = rules.map { |rule| rule["id"] }
      fail!("control ownership rule ids must be unique and non-empty") if ids.any? { |id| id.to_s.strip.empty? } || ids.uniq.length != ids.length

      rules.each do |rule|
        fail!("control ownership rule #{rule["id"]} has invalid owner") unless allowed_owners.include?(rule["owner"])
        fail!("control ownership rule #{rule["id"]} has no subjects or patterns") if Array(rule["subjects"]).empty? && Array(rule["patterns"]).empty?
      end

      Array(subjects).map do |subject|
        matches = rules.select { |rule| matches?(rule, subject) }
        fail!("unclassified control subject: #{subject}") if matches.empty?
        selected = matches.first
        {"subject" => subject, "owner" => selected.fetch("owner"), "rule" => selected.fetch("id"), "match_count" => matches.length}
      end
    end

    def self.matches?(rule, subject)
      Array(rule["subjects"]).include?(subject) || Array(rule["patterns"]).any? { |pattern| File.fnmatch?(pattern, subject, File::FNM_PATHNAME | File::FNM_EXTGLOB) }
    end
    private_class_method :matches?

    def self.fail!(message)
      raise ArgumentError, message
    end
    private_class_method :fail!
  end
end
