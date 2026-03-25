package com.example.service;
 
import com.example.dto.ProcessusDTO;
import com.example.dto.ProcessusRequest;
import com.example.entity.Processus;
import com.example.entity.Segment;
import com.example.repository.ProcessusRepository;
import com.example.repository.SegmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
 
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class ProcessusService {
 
    private final ProcessusRepository processusRepository;
    private final SegmentRepository segmentRepository;  
 
    private ProcessusDTO toDTO(Processus p) {
        ProcessusDTO dto = new ProcessusDTO();
        dto.setId(p.getId());
        dto.setNom(p.getNom());
        dto.setDescription(p.getDescription());
        if (p.getSegment() != null) {
            dto.setSegmentId(p.getSegment().getId());
            dto.setSegmentNom(p.getSegment().getNom());
        }
        return dto;
    }

    public List<ProcessusDTO> findAll() {
        return processusRepository.findAllWithSegmentAndMachineCount()
                .stream()
                .map(row -> {
                    Processus p     = (Processus) row[0];
                    long count      = (long) row[1];
                    ProcessusDTO dto = toDTO(p);         // toDTO() normal inchangé
                    dto.setMachineCount((int) count);    // on injecte le count ici
                    return dto;
                })
                .toList();
    }
 
    public ProcessusDTO findById(Long id) {
        Processus p = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
        return toDTO(p);
    }
 
    public List<ProcessusDTO> findBySegment(Long segmentId) {
        return processusRepository.findBySegmentId(segmentId)
                .stream()
                .map(this::toDTO)
                .toList();
    }
 
    public ProcessusDTO create(ProcessusRequest req) {
        if (req.getSegmentId() == null) {
            throw new RuntimeException("Le segment est obligatoire");
        }
        Segment segment = segmentRepository.findById(req.getSegmentId())
                .orElseThrow(() -> new RuntimeException("Segment introuvable"));
        Processus p = new Processus();
        p.setNom(req.getNom());
        p.setDescription(req.getDescription());
        p.setSegment(segment);
        return toDTO(processusRepository.save(p));
    }
 
    public ProcessusDTO update(Long id, ProcessusRequest req) {
        Processus p = processusRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));
        p.setNom(req.getNom());
        p.setDescription(req.getDescription());
        if (req.getSegmentId() != null) {
            Segment segment = segmentRepository.findById(req.getSegmentId())
                    .orElseThrow(() -> new RuntimeException("Segment introuvable"));
            p.setSegment(segment);
        }
        return toDTO(processusRepository.save(p));
    }
    public void delete(Long id) {
        processusRepository.deleteById(id);
    }
}
 