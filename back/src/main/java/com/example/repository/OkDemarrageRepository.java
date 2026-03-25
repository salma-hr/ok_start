package com.example.repository;

import com.example.entity.OkDemarrage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OkDemarrageRepository extends JpaRepository<OkDemarrage, Long> {

    // ── findByIdWithDetails ───────────────────────────────────────
    // ✅ FIX : suppression de "LEFT JOIN FETCH s.plant p LEFT JOIN FETCH p.segment"
    //          Site n'a plus de @ManyToOne plant — Plant/Segment passent
    //          par machine → processus → segment → plant
    @Query("""
        SELECT okd FROM OkDemarrage okd
        LEFT JOIN FETCH okd.machine m
        LEFT JOIN FETCH m.processus proc
        LEFT JOIN FETCH proc.segment seg
        LEFT JOIN FETCH seg.plant pl
        LEFT JOIN FETCH okd.operateur
        LEFT JOIN FETCH okd.site
        LEFT JOIN FETCH okd.reponses r
        LEFT JOIN FETCH r.critere
        LEFT JOIN FETCH okd.valideN1Par
        LEFT JOIN FETCH okd.valideN2Par
        LEFT JOIN FETCH okd.valideParFinal
        LEFT JOIN FETCH okd.rejetePar
        WHERE okd.id = :id
        """)
    Optional<OkDemarrage> findByIdWithDetails(@Param("id") Long id);

    // ── findAllWithDetails ────────────────────────────────────────
    // Même correction pour le findAll
    @Query("""
        SELECT DISTINCT okd FROM OkDemarrage okd
        LEFT JOIN FETCH okd.machine m
        LEFT JOIN FETCH m.processus proc
        LEFT JOIN FETCH proc.segment seg
        LEFT JOIN FETCH seg.plant pl
        LEFT JOIN FETCH okd.operateur
        LEFT JOIN FETCH okd.site
        LEFT JOIN FETCH okd.reponses r
        LEFT JOIN FETCH r.critere
        LEFT JOIN FETCH okd.valideN1Par
        LEFT JOIN FETCH okd.valideN2Par
        LEFT JOIN FETCH okd.valideParFinal
        LEFT JOIN FETCH okd.rejetePar
        ORDER BY okd.date DESC
        """)
    List<OkDemarrage> findAllWithDetails();
}