package com.themuffinman.app.workmarket.repository;

import com.themuffinman.app.workmarket.model.QuestApplication;
import com.themuffinman.app.workmarket.model.QuestApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestApplicationRepository extends JpaRepository<QuestApplication, Long> {

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.quest.id = :questId")
    List<QuestApplication> findForQuestApplicationManagement(Long questId);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.applicant.id = :applicantId")
    List<QuestApplication> findForApplicantDashboard(Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.quest.id = :questId and qa.applicant.id = :applicantId")
    Optional<QuestApplication> findForViewerApplication(Long questId, Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.quest.id = :questId and qa.applicant.id = :applicantId and qa.status = :status")
    Optional<QuestApplication> findForViewerApplicationWithStatus(Long questId, Long applicantId, QuestApplicationStatus status);

    boolean existsByQuestIdAndApplicantId(Long questId, Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.id = :id and qa.quest.id = :questId")
    Optional<QuestApplication> findForQuestApplicationDetail(Long id, Long questId);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.id = :id")
    Optional<QuestApplication> findForApplicationDetail(Long id);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant where qa.quest.id = :questId and qa.status = :status")
    List<QuestApplication> findForQuestApplicationsByStatus(Long questId, QuestApplicationStatus status);

    @Query("select qa from QuestApplication qa join fetch qa.quest q join fetch q.creator join fetch qa.applicant order by qa.createdAt desc")
    List<QuestApplication> findForAdminApplicationList();

    long countByQuestIdAndStatus(Long questId, QuestApplicationStatus status);

    boolean existsByQuestId(Long questId);

    void deleteByQuestId(Long questId);

    long countByApplicantId(Long applicantId);

    default List<QuestApplication> findByQuestId(Long questId) {
        return findForQuestApplicationManagement(questId);
    }

    default List<QuestApplication> findByApplicantId(Long applicantId) {
        return findForApplicantDashboard(applicantId);
    }

    default Optional<QuestApplication> findByQuestIdAndApplicantId(Long questId, Long applicantId) {
        return findForViewerApplication(questId, applicantId);
    }

    default Optional<QuestApplication> findByQuestIdAndApplicantIdAndStatus(
            Long questId,
            Long applicantId,
            QuestApplicationStatus status
    ) {
        return findForViewerApplicationWithStatus(questId, applicantId, status);
    }

    default Optional<QuestApplication> findByIdAndQuestId(Long id, Long questId) {
        return findForQuestApplicationDetail(id, questId);
    }

    default Optional<QuestApplication> findByIdDetailed(Long id) {
        return findForApplicationDetail(id);
    }

    default List<QuestApplication> findByQuestIdAndStatus(Long questId, QuestApplicationStatus status) {
        return findForQuestApplicationsByStatus(questId, status);
    }

    default List<QuestApplication> findAllDetailed() {
        return findForAdminApplicationList();
    }
}
