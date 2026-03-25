package com.example.service;

import com.example.dto.DashboardStatsDTO;
import com.example.dto.ProcessusCountDTO;
import com.example.dto.ChecklistRecentDTO;
import com.example.entity.OkDemarrage;
import com.example.entity.ReponseCritere;
import com.example.repository.ChecklistRepository;
import com.example.repository.ReponseCritereRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private ChecklistRepository checklistRepository;

    @Autowired
    private ReponseCritereRepository reponseCritereRepository;

    // ── Stats globales ───────────────────────────────────────────────
    public DashboardStatsDTO getGlobalStats() {
        DashboardStatsDTO stats = new DashboardStatsDTO();

        LocalDate now = LocalDate.now();
        LocalDate debutMois         = now.withDayOfMonth(1);
        LocalDate finMois           = now.with(TemporalAdjusters.lastDayOfMonth());
        LocalDate debutMoisPrecedent = debutMois.minusMonths(1);
        LocalDate finMoisPrecedent   = debutMoisPrecedent.with(TemporalAdjusters.lastDayOfMonth());

        // 1. Checklists validées finalement ce mois (VALIDE_FINAL uniquement)
        Long valideesMois      = checklistRepository.countByStatusAndDateBetween(
                OkDemarrage.Status.VALIDE_FINAL, debutMois, finMois);
        Long valideesMoisPrec  = checklistRepository.countByStatusAndDateBetween(
                OkDemarrage.Status.VALIDE_FINAL, debutMoisPrecedent, finMoisPrecedent);

        stats.setChecklistsValidees(valideesMois);
        stats.setEvolutionValidees(calculateEvolutionPercent(valideesMois, valideesMoisPrec));

        // 2. Non-conformités (critère ROUGE) ce mois
        Long ncMois      = checklistRepository.countWithNonConformiteAndDateBetween(debutMois, finMois);
        Long ncMoisPrec  = checklistRepository.countWithNonConformiteAndDateBetween(debutMoisPrecedent, finMoisPrecedent);

        stats.setNonConformites(ncMois);
        stats.setEvolutionNC(calculateEvolutionPercent(ncMois, ncMoisPrec));

        // 3. En attente de validation = SOUMIS + VALIDE_N1 + VALIDE_N2
        Long enAttenteN1         = checklistRepository.countByStatus(OkDemarrage.Status.SOUMIS);
        Long enAttenteN2         = checklistRepository.countByStatus(OkDemarrage.Status.VALIDE_N1);
        Long enAttenteValidation = checklistRepository.countByStatus(OkDemarrage.Status.VALIDE_N2);
        Long enAttente           = enAttenteN1 + enAttenteN2 + enAttenteValidation;

        stats.setEnAttente(enAttente);
        stats.setEnAttenteN1(enAttenteN1);
        stats.setEnAttenteN2(enAttenteN2);
        stats.setEnAttenteValidation(enAttenteValidation);
        stats.setEvolutionAttente("+" + enAttente);

        // 4. Taux de conformité du mois
        Long totalMois = checklistRepository.countByDateBetween(debutMois, finMois);
        Double tauxConformite   = totalMois > 0 ? (valideesMois  * 100.0) / totalMois : 0.0;

        Long totalMoisPrec      = checklistRepository.countByDateBetween(debutMoisPrecedent, finMoisPrecedent);
        Double tauxConfPrec     = totalMoisPrec > 0 ? (valideesMoisPrec * 100.0) / totalMoisPrec : 0.0;

        stats.setTauxConformite(tauxConformite);
        stats.setEvolutionTaux(String.format("%+.1f%%", tauxConformite - tauxConfPrec));
        stats.setTotalChecklists(totalMois);

        return stats;
    }

    // ── Checklists par processus ─────────────────────────────────────
    public List<ProcessusCountDTO> getChecklistCountsByProcessus() {
        List<Object[]> results = checklistRepository.countChecklistsByProcessus();
        return results.stream()
                .map(r -> new ProcessusCountDTO(
                        (Long) r[0],
                        (String) r[1],
                        ((Number) r[2]).longValue()))
                .collect(Collectors.toList());
    }

    // ── Dernières checklists ─────────────────────────────────────────
    public List<ChecklistRecentDTO> getRecentChecklists(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "date"));
        Page<OkDemarrage> page = checklistRepository.findAll(pageable);
        return page.getContent().stream()
                .map(this::convertToChecklistRecentDTO)
                .collect(Collectors.toList());
    }

    // ── Stats par période personnalisée ──────────────────────────────
    public DashboardStatsDTO getStatsByPeriod(String startDateStr, String endDateStr) {
        LocalDate startDate = LocalDate.parse(startDateStr);
        LocalDate endDate   = LocalDate.parse(endDateStr);

        DashboardStatsDTO stats = new DashboardStatsDTO();

        Long validees = checklistRepository.countByStatusAndDateBetween(
                OkDemarrage.Status.VALIDE_FINAL, startDate, endDate);
        stats.setChecklistsValidees(validees);
        stats.setEvolutionValidees("+0%");

        Long nc = checklistRepository.countWithNonConformiteAndDateBetween(startDate, endDate);
        stats.setNonConformites(nc);
        stats.setEvolutionNC("0%");

        Long enAttenteN1         = checklistRepository.countByStatus(OkDemarrage.Status.SOUMIS);
        Long enAttenteN2         = checklistRepository.countByStatus(OkDemarrage.Status.VALIDE_N1);
        Long enAttenteValidation = checklistRepository.countByStatus(OkDemarrage.Status.VALIDE_N2);
        Long enAttente           = enAttenteN1 + enAttenteN2 + enAttenteValidation;

        stats.setEnAttente(enAttente);
        stats.setEnAttenteN1(enAttenteN1);
        stats.setEnAttenteN2(enAttenteN2);
        stats.setEnAttenteValidation(enAttenteValidation);
        stats.setEvolutionAttente("+" + enAttente);

        Long total = checklistRepository.countByDateBetween(startDate, endDate);
        Double taux = total > 0 ? (validees * 100.0) / total : 0.0;
        stats.setTauxConformite(taux);
        stats.setEvolutionTaux("0%");
        stats.setTotalChecklists(total);

        return stats;
    }

    // ── Helpers ──────────────────────────────────────────────────────
    private String calculateEvolutionPercent(Long current, Long previous) {
        if (previous == null || previous == 0) {
            return current > 0 ? "+100%" : "0%";
        }
        double evolution = ((current - previous) * 100.0) / previous;
        return String.format("%+.0f%%", evolution);
    }

    private ChecklistRecentDTO convertToChecklistRecentDTO(OkDemarrage checklist) {
        ChecklistRecentDTO dto = new ChecklistRecentDTO();

        dto.setId(checklist.getId());
        dto.setMachineNom(checklist.getMachine()   != null ? checklist.getMachine().getNom()   : "N/A");
        dto.setMachineCode(checklist.getMachine()  != null ? checklist.getMachine().getNom()   : "N/A");
        dto.setOperateurNom(checklist.getOperateur() != null ? checklist.getOperateur().getNom() : "N/A");
        dto.setDateControle(checklist.getDate());

        // dateCreation = date de la dernière action (finale si validée, rejet sinon, N2, N1...)
        dto.setDateCreation(
            checklist.getDateValidationFinale() != null ? checklist.getDateValidationFinale() :
            checklist.getDateRejet()            != null ? checklist.getDateRejet()            :
            checklist.getDateValidationN2()     != null ? checklist.getDateValidationN2()     :
            checklist.getDateValidationN1()     != null ? checklist.getDateValidationN1()     :
            null
        );

        dto.setStatut(checklist.getStatus() != null ? checklist.getStatus().name() : null);

        List<ReponseCritere> reponses = reponseCritereRepository.findByOkDemarrageId(checklist.getId());
        int total = reponses.size();
        int ok    = (int) reponses.stream().filter(r -> ReponseCritere.Valeur.VERT == r.getValeur()).count();

        dto.setCriteresOk(ok);
        dto.setCriteresTotal(total);
        dto.setHasNonConformite(reponses.stream().anyMatch(r -> ReponseCritere.Valeur.ROUGE == r.getValeur()));

        return dto;
    }
}