package com.sidequest.sidequest.repository;

import com.sidequest.sidequest.model.QuestApplication;
import com.sidequest.sidequest.model.QuestApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface QuestApplicationRepository extends JpaRepository<QuestApplication, Long> {

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.quest.id = :questId")
    List<QuestApplication> findByQuestId(Long questId);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.applicant.id = :applicantId")
    List<QuestApplication> findByApplicantId(Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.quest.id = :questId and qa.applicant.id = :applicantId")
    Optional<QuestApplication> findByQuestIdAndApplicantId(Long questId, Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.quest.id = :questId and qa.applicant.id = :applicantId and qa.status = :status")
    Optional<QuestApplication> findByQuestIdAndApplicantIdAndStatus(Long questId, Long applicantId, QuestApplicationStatus status);

    boolean existsByQuestIdAndApplicantId(Long questId, Long applicantId);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.id = :id and qa.quest.id = :questId")
    Optional<QuestApplication> findByIdAndQuestId(Long id, Long questId);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.id = :id")
    Optional<QuestApplication> findByIdDetailed(Long id);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant where qa.quest.id = :questId and qa.status = :status")
    List<QuestApplication> findByQuestIdAndStatus(Long questId, QuestApplicationStatus status);

    @Query("select qa from QuestApplication qa join fetch qa.quest join fetch qa.applicant order by qa.createdAt desc")
    List<QuestApplication> findAllDetailed();

    long countByQuestIdAndStatus(Long questId, QuestApplicationStatus status);

    boolean existsByQuestId(Long questId);

    void deleteByQuestId(Long questId);

    long countByApplicantId(Long applicantId);
}
