package com.example.service;

import com.example.dto.BatchCritereRequest;
import com.example.dto.CritereDTO;
import com.example.dto.CritereRequest;
import com.example.entity.Critere;
import com.example.entity.Processus;
import com.example.repository.CritereRepository;
import com.example.repository.ProcessusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class CritereService {

    private final CritereRepository critereRepository;
    private final ProcessusRepository processusRepository;

    private CritereDTO toDTO(Critere c) {
        CritereDTO dto = new CritereDTO();
        dto.setId(c.getId());
        dto.setNom(c.getNom());
        dto.setDescription(c.getDescription());
        dto.setNomAr(c.getNomAr());
        dto.setDescriptionAr(c.getDescriptionAr());
        dto.setType(c.getType());
        dto.setCouleur(c.getCouleur());
        dto.setMoyenVerification(c.getMoyenVerification());
        dto.setCategorie(c.getCategorie());
        dto.setImage(c.getImage());
        if (c.getProcessus() != null) {
            dto.setProcessusId(c.getProcessus().getId());
            dto.setProcessusNom(c.getProcessus().getNom());
        }
        return dto;
    }

    private void applyRequest(Critere c, CritereRequest req) {
        if (req.getNom()         != null) c.setNom(req.getNom().trim());
        if (req.getDescription() != null) c.setDescription(req.getDescription());
        c.setNomAr(req.getNomAr());
        c.setDescriptionAr(req.getDescriptionAr());
        if (req.getType()              != null) c.setType(req.getType());
        if (req.getCouleur()           != null) c.setCouleur(req.getCouleur());
        if (req.getMoyenVerification() != null) c.setMoyenVerification(req.getMoyenVerification());
        if (req.getCategorie()         != null) c.setCategorie(req.getCategorie());
        if (req.getImage()             != null) c.setImage(req.getImage());
    }

    public List<CritereDTO> findAll() {
        return critereRepository.findAllWithProcessus().stream().map(this::toDTO).toList();
    }

    public List<CritereDTO> findByProcessus(Long processusId) {
        return critereRepository.findByProcessusId(processusId).stream().map(this::toDTO).toList();
    }

    public CritereDTO create(CritereRequest req) {
        Processus p = processusRepository.findById(req.getProcessusId())
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
        Critere c = new Critere();
        applyRequest(c, req);
        c.setProcessus(p);
        return toDTO(critereRepository.save(c));
    }

    public CritereDTO update(Long id, CritereRequest req) {
        Critere c = critereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Critère introuvable"));
        applyRequest(c, req);
        if (req.getProcessusId() != null) {
            Processus p = processusRepository.findById(req.getProcessusId())
                    .orElseThrow(() -> new RuntimeException("Processus introuvable"));
            c.setProcessus(p);
        }
        return toDTO(critereRepository.save(c));
    }

    public void delete(Long id) {
        critereRepository.deleteById(id);
    }

    /** Suppression en lot — utilisé par POST /batch-delete */
    @Transactional
    public int deleteAll(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return 0;
        List<Critere> toDelete = critereRepository.findAllById(ids);
        critereRepository.deleteAll(toDelete);
        return toDelete.size();
    }

    @Transactional
    public String ajouterBatch(BatchCritereRequest req) {
        Processus processus = processusRepository.findById(req.getProcessusId())
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
        List<CritereRequest> lignes = req.getCriteres();
        if (lignes == null || lignes.isEmpty()) throw new RuntimeException("Aucun critère fourni.");
        int count = 0;
        for (CritereRequest cr : lignes) {
            if (cr.getNom() == null || cr.getNom().trim().length() < 2) continue;
            Critere critere = new Critere();
            applyRequest(critere, cr);
            critere.setProcessus(processus);
            critereRepository.save(critere);
            count++;
        }
        if (count == 0) throw new RuntimeException("Aucun critère valide.");
        return count + " critère(s) ajouté(s) avec succès.";
    }
}