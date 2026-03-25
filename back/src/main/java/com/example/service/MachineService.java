package com.example.service;

import com.example.dto.MachineDTO;
import com.example.dto.MachineRequest;
import com.example.entity.Machine;
import com.example.entity.Plant;
import com.example.entity.Processus;
import com.example.entity.Segment;
import com.example.entity.Site;
import com.example.repository.MachineRepository;
import com.example.repository.PlantRepository;
import com.example.repository.ProcessusRepository;
import com.example.repository.SegmentRepository;
import com.example.repository.SiteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class MachineService {

    private final MachineRepository machineRepository;
    private final ProcessusRepository processusRepository;
    private final SegmentRepository segmentRepository;
    private final PlantRepository plantRepository;
    private final SiteRepository siteRepository;

    private MachineDTO toDTO(Machine m) {
        MachineDTO dto = new MachineDTO();
        dto.setId(m.getId());
        dto.setNom(m.getNom());
        dto.setDescription(m.getDescription());
        if (m.getProcessus() != null) {
            dto.setProcessusId(m.getProcessus().getId());
            dto.setProcessusNom(m.getProcessus().getNom());
        }
        if (m.getSegment() != null) {
            dto.setSegmentId(m.getSegment().getId());
            dto.setSegmentNom(m.getSegment().getNom());
        }
        if (m.getPlant() != null) {
            dto.setPlantId(m.getPlant().getId());
            dto.setPlantNom(m.getPlant().getNom());
        }
        if (m.getSite() != null) {
            dto.setSiteId(m.getSite().getId());
            dto.setSiteNom(m.getSite().getNom());
        }
        return dto;
    }

    public List<MachineDTO> findAll() {
        return machineRepository.findAllWithProcessus()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<MachineDTO> findByProcessus(Long processusId) {
        return machineRepository.findByProcessusId(processusId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Transactional
    public MachineDTO create(MachineRequest req) {
        Processus p = processusRepository.findById(req.getProcessusId())
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
        Machine m = new Machine();
        m.setNom(req.getNom());
        m.setDescription(req.getDescription());
        m.setProcessus(p);
        if (req.getSegmentId() != null) {
            Segment seg = segmentRepository.findById(req.getSegmentId()).orElse(null);
            m.setSegment(seg);
        }
        if (req.getPlantId() != null) {
            Plant pl = plantRepository.findById(req.getPlantId()).orElse(null);
            m.setPlant(pl);
        }
        if (req.getSiteId() != null) {
            Site si = siteRepository.findById(req.getSiteId()).orElse(null);
            m.setSite(si);
        }
        return toDTO(machineRepository.save(m));
    }

    @Transactional
    public MachineDTO update(Long id, MachineRequest req) {
        Machine m = machineRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Machine introuvable"));
        m.setNom(req.getNom());
        m.setDescription(req.getDescription());
        if (req.getProcessusId() != null) {
            Processus p = processusRepository.findById(req.getProcessusId())
                    .orElseThrow(() -> new RuntimeException("Processus introuvable"));
            m.setProcessus(p);
        }
        // Mettre à jour segment/plant/site (null = effacer la liaison)
        m.setSegment(req.getSegmentId() != null
                ? segmentRepository.findById(req.getSegmentId()).orElse(null) : null);
        m.setPlant(req.getPlantId() != null
                ? plantRepository.findById(req.getPlantId()).orElse(null) : null);
        m.setSite(req.getSiteId() != null
                ? siteRepository.findById(req.getSiteId()).orElse(null) : null);
        return toDTO(machineRepository.save(m));
    }

    @Transactional
    public void delete(Long id) {
        machineRepository.deleteById(id);
    }

    public Machine findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }
}