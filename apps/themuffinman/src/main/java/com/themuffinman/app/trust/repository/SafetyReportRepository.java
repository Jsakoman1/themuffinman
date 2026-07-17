package com.themuffinman.app.trust.repository;

import com.themuffinman.app.trust.model.SafetyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> {
    @Query("select report from SafetyReport report join fetch report.reporter where report.reporter.id = :reporterId order by report.createdAt desc, report.id desc")
    List<SafetyReport> findByReporterId(Long reporterId);
}
