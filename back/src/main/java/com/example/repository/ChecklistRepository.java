package com.example.repository;

import com.example.entity.OkDemarrage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ChecklistRepository extends JpaRepository<OkDemarrage, Long> {

    // ── Comptages par statut + période ───────────────────────────────

    @Query("SELECT COUNT(o) FROM OkDemarrage o WHERE o.status = :status AND o.date BETWEEN :startDate AND :endDate")
    Long countByStatusAndDateBetween(
            @Param("status") OkDemarrage.Status status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(o) FROM OkDemarrage o WHERE o.status = :status")
    Long countByStatus(@Param("status") OkDemarrage.Status status);

    @Query("SELECT COUNT(o) FROM OkDemarrage o WHERE o.date BETWEEN :startDate AND :endDate")
    Long countByDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ── En attente de validation ─────────────────────────────────────
    // = toutes les checklists qui ne sont ni validées finalement, ni rejetées
    // (SOUMIS + VALIDE_N1 + VALIDE_N2)
    @Query("SELECT COUNT(o) FROM OkDemarrage o WHERE o.status IN " +
           "('SOUMIS', 'VALIDE_N1', 'VALIDE_N2')")
    Long countEnAttenteDeValidation();

    // ── Non-conformités (critère ROUGE) ──────────────────────────────
    @Query("SELECT COUNT(DISTINCT o) FROM OkDemarrage o JOIN o.reponses r " +
           "WHERE r.valeur = com.example.entity.ReponseCritere.Valeur.ROUGE " +
           "AND o.date BETWEEN :startDate AND :endDate")
    Long countWithNonConformiteAndDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // ── Répartition par processus ─────────────────────────────────────
    @Query("""
            SELECT c.machine.processus.id, c.machine.processus.nom, COUNT(c)
            FROM OkDemarrage c
            WHERE c.machine.processus IS NOT NULL
            GROUP BY c.machine.processus.id, c.machine.processus.nom
            ORDER BY COUNT(c) DESC
            """)
    List<Object[]> countChecklistsByProcessus();

    // ── Recherches directes ───────────────────────────────────────────
    @Override
    Page<OkDemarrage> findAll(Pageable pageable);

    List<OkDemarrage> findByMachineId(Long machineId);
    List<OkDemarrage> findByOperateurId(Long operateurId);
    List<OkDemarrage> findByDate(LocalDate date);
    List<OkDemarrage> findByStatus(OkDemarrage.Status status);

    // ── Checklists en attente pour un rôle donné ──────────────────────
    // Pratique pour filtrer côté frontend par rôle
    @Query("SELECT o FROM OkDemarrage o WHERE o.status = 'SOUMIS'")
    List<OkDemarrage> findEnAttenteN1();

    @Query("SELECT o FROM OkDemarrage o WHERE o.status = 'VALIDE_N1'")
    List<OkDemarrage> findEnAttenteN2();

    @Query("SELECT o FROM OkDemarrage o WHERE o.status = 'VALIDE_N2'")
    List<OkDemarrage> findEnAttenteValidationFinale();
}