package com.example.service;

import com.example.dto.ChecklistDTO;
import com.example.dto.ChecklistRequest;
import com.example.entity.*;
import com.example.repository.*;
import lombok.RequiredArgsConstructor;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.example.service.NotificationService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@SuppressWarnings("null")
public class ChecklistService {

    private final OkDemarrageRepository okDemarrageRepository;
    private final MachineRepository machineRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final CritereRepository critereRepository;
    private final SiteRepository siteRepository;
    private final ProcessusRepository processusRepository;
    private final RestTemplate restTemplate;
    private final NotificationService notificationService;

    // URL du micro-service Python (configurable dans application.properties)
    @Value("${ocr.service.url:http://localhost:8000/ocr}")
    private String ocrServiceUrl;

    // ── toDTO ────────────────────────────────────────────────────────
    private ChecklistDTO toDTO(OkDemarrage okd) {
        ChecklistDTO dto = new ChecklistDTO();
        dto.setId(okd.getId());
        dto.setDate(okd.getDate());
        dto.setStatus(okd.getStatus());
        dto.setSession(okd.getSession());

        dto.setDateValidationN1(okd.getDateValidationN1());
        if (okd.getValideN1Par() != null) {
            dto.setValideN1Par(okd.getValideN1Par().getNom());
            dto.setValideN1ParMatricule(okd.getValideN1Par().getMatricule());
        }

        dto.setDateValidationN2(okd.getDateValidationN2());
        if (okd.getValideN2Par() != null) {
            dto.setValideN2Par(okd.getValideN2Par().getNom());
            dto.setValideN2ParMatricule(okd.getValideN2Par().getMatricule());
        }

        dto.setDateValidationFinale(okd.getDateValidationFinale());
        if (okd.getValideParFinal() != null) {
            dto.setValideParFinal(okd.getValideParFinal().getNom());
            dto.setValideParFinalMatricule(okd.getValideParFinal().getMatricule());
        }

        dto.setMotifRejet(okd.getMotifRejet());
        dto.setDateRejet(okd.getDateRejet());
        if (okd.getRejetePar() != null) {
            dto.setRejetePar(okd.getRejetePar().getNom());
        }

        if (okd.getMachine() != null) {
            dto.setMachineId(okd.getMachine().getId());
            dto.setMachineNom(okd.getMachine().getNom());
            if (okd.getMachine().getProcessus() != null) {
                dto.setProcessusId(okd.getMachine().getProcessus().getId());
                dto.setProcessusNom(okd.getMachine().getProcessus().getNom());
            }
        }
        if (okd.getOperateur() != null) {
            dto.setOperateurId(okd.getOperateur().getId());
            dto.setOperateurNom(okd.getOperateur().getNom());
            dto.setOperateurMatricule(okd.getOperateur().getMatricule());
        }
        if (okd.getSite() != null) {
            dto.setSiteId(okd.getSite().getId());
            Processus proc = okd.getMachine().getProcessus();
            if (proc.getSegment() != null) {
                Segment segment = proc.getSegment();
                dto.setSegmentId(segment.getId());
                if (segment.getPlant() != null) {
                    dto.setPlantId(segment.getPlant().getId());
                }
            }        
        }
        if (okd.getReponses() != null) {
            dto.setReponses(okd.getReponses().stream().map(r -> {
                ChecklistDTO.ReponseDTO rd = new ChecklistDTO.ReponseDTO();
                rd.setId(r.getId());
                rd.setValeur(r.getValeur().name());
                rd.setCommentaire(r.getCommentaire());
                if (r.getCritere() != null) {
                    rd.setCritereId(r.getCritere().getId());
                    rd.setCritereNom(r.getCritere().getNom());
                }
                return rd;
            }).toList());
        }
        return dto;
    }

    // ── Lectures ─────────────────────────────────────────────────────
    public List<ChecklistDTO> findAll() {
        return okDemarrageRepository.findAllWithDetails().stream().map(this::toDTO).toList();
    }

    public ChecklistDTO findById(Long id) {
        return toDTO(okDemarrageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Checklist introuvable")));
    }

    // ── Soumettre ────────────────────────────────────────────────────
    @Transactional
    public ChecklistDTO soumettre(ChecklistRequest req) {
        if (req.getReponses() != null) {
            req.getReponses().forEach(r -> {
                if ("ROUGE".equals(r.getValeur().name())
                        && (r.getCommentaire() == null || r.getCommentaire().isBlank())) {
                    throw new RuntimeException("Commentaire obligatoire pour un critère ROUGE.");
                }
            });
        }
        Machine machine = machineRepository.findById(req.getMachineId())
                .orElseThrow(() -> new RuntimeException("Machine introuvable"));
        Utilisateur operateur = utilisateurRepository.findById(req.getOperateurId())
                .orElseThrow(() -> new RuntimeException("Opérateur introuvable"));
        Site site = siteRepository.findById(req.getSiteId())
                .orElseThrow(() -> new RuntimeException("Site introuvable"));

        OkDemarrage okd = new OkDemarrage();
        okd.setDate(req.getDate());
        okd.setSession(req.getSession() != null ? req.getSession() : OkDemarrage.Session.M);
        okd.setMachine(machine);
        okd.setOperateur(operateur);
        okd.setSite(site);
        okd.setStatus(OkDemarrage.Status.SOUMIS);

        List<ReponseCritere> reponses = req.getReponses().stream().map(r -> {
            Critere critere = critereRepository.findById(r.getCritereId())
                    .orElseThrow(() -> new RuntimeException("Critère introuvable"));
            ReponseCritere rep = new ReponseCritere();
            rep.setValeur(r.getValeur());
            rep.setCommentaire(r.getCommentaire());
            rep.setCritere(critere);
            rep.setOkDemarrage(okd);
            return rep;
        }).toList();

        okd.setReponses(reponses);
        OkDemarrage saved = okDemarrageRepository.save(okd);

        // ── Notifier les validateurs ─────────────────────────────────────
        try {
            String operateurNom  = operateur.getNom();
            String machineNom    = machine.getNom();
            String processusNom  = machine.getProcessus() != null
                                   ? machine.getProcessus().getNom() : "—";
            notificationService.notifierNouvelleChecklist(
                saved.getId(), operateurNom, machineNom, processusNom);
        } catch (Exception ignored) {
            // Ne jamais bloquer la soumission si la notif échoue
        }

        return toDTO(saved);
    }

    // ── Validation N1 ────────────────────────────────────────────────
    @Transactional
    public ChecklistDTO validerN1(Long id, String matricule) {
        OkDemarrage okd = okDemarrageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Checklist introuvable"));
        if (okd.getStatus() != OkDemarrage.Status.SOUMIS)
            throw new RuntimeException("Validation N1 impossible : statut actuel = " + okd.getStatus());
        okd.setValideN1Par(utilisateurRepository.findByMatricule(matricule).orElse(null));
        okd.setDateValidationN1(LocalDateTime.now());
        okd.setStatus(OkDemarrage.Status.VALIDE_N1);
        return toDTO(okDemarrageRepository.save(okd));
    }

    // ── Validation N2 ────────────────────────────────────────────────
    @Transactional
    public ChecklistDTO validerN2(Long id, String matricule) {
        OkDemarrage okd = okDemarrageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Checklist introuvable"));
        if (okd.getStatus() != OkDemarrage.Status.VALIDE_N1)
            throw new RuntimeException("Validation N2 impossible : N1 doit valider en premier.");
        okd.setValideN2Par(utilisateurRepository.findByMatricule(matricule).orElse(null));
        okd.setDateValidationN2(LocalDateTime.now());
        okd.setStatus(OkDemarrage.Status.VALIDE_N2);
        return toDTO(okDemarrageRepository.save(okd));
    }

    // ── Validation finale ────────────────────────────────────────────
    @Transactional
    public ChecklistDTO validerFinal(Long id, String matricule) {
        OkDemarrage okd = okDemarrageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Checklist introuvable"));
        if (okd.getStatus() != OkDemarrage.Status.VALIDE_N2)
            throw new RuntimeException("Validation finale impossible : N1 et N2 doivent être complétés.");
        okd.setValideParFinal(utilisateurRepository.findByMatricule(matricule).orElse(null));
        okd.setDateValidationFinale(LocalDateTime.now());
        okd.setStatus(OkDemarrage.Status.VALIDE_FINAL);
        return toDTO(okDemarrageRepository.save(okd));
    }

    // ── Rejet ────────────────────────────────────────────────────────
    @Transactional
    public ChecklistDTO rejeter(Long id, String motif, String matricule) {
        OkDemarrage okd = okDemarrageRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Checklist introuvable"));
        if (okd.getStatus() == OkDemarrage.Status.VALIDE_FINAL || okd.getStatus() == OkDemarrage.Status.REJETE)
            throw new RuntimeException("Impossible de rejeter : la checklist est déjà " + okd.getStatus());
        if (motif == null || motif.isBlank())
            throw new RuntimeException("Le motif de rejet est obligatoire.");
        okd.setStatus(OkDemarrage.Status.REJETE);
        okd.setMotifRejet(motif);
        okd.setRejetePar(utilisateurRepository.findByMatricule(matricule).orElse(null));
        okd.setDateRejet(LocalDateTime.now());
        return toDTO(okDemarrageRepository.save(okd));
    }

    // ── Import PDF ───────────────────────────────────────────────────
    @Transactional
    public String importerCriteresDepuisPdf(MultipartFile file, Long processusId) throws Exception {

        Processus processus = processusRepository.findById(processusId)
                .orElseThrow(() -> new RuntimeException("Processus introuvable"));

        // 1. Essayer PDFBox (PDF natif texte)
        String text = null;
        try (PDDocument document = PDDocument.load(file.getInputStream())) {
            text = new PDFTextStripper().getText(document);
        }

        // 2. PDF scanné → appel micro-service Python OCR
        if (text == null || text.trim().length() < 20) {
            text = appellerServiceOCR(file);
        }

        if (text == null || text.isBlank()) {
            throw new RuntimeException("Impossible d'extraire le texte du PDF.");
        }

        // 3. Parser et sauvegarder les critères
        String[] lignes = text.split("\n");
        int count = 0;

        for (String ligne : lignes) {
            ligne = ligne.trim();
            if (ligne.length() < 10)  continue;
            if (ligne.length() > 200) continue;
            if (ligne.matches("^\\d+$"))                         continue;
            if (ligne.matches("(?i)^page\\s*\\d.*"))             continue;
            if (ligne.matches("(?i)^(date|visa|signature|résultat|oui|non|observations?|total|critère|leoni|ok.?d[eé]marrage).*")) continue;
            if (ligne.contains("CamScanner"))                    continue;
            if (ligne.matches(".*\\d{2}/\\d{2}/\\d{4}.*"))      continue;

            Critere critere = new Critere();
            critere.setNom(nettoyer(ligne));
            critere.setType(detecterType(ligne));
            critere.setProcessus(processus);
            critereRepository.save(critere);
            count++;
        }

        if (count == 0) {
            throw new RuntimeException(
                "Aucun critère détecté. Les lignes extraites ne correspondent pas au format attendu.");
        }

        return count + " critère(s) importé(s) depuis le PDF.";
    }

    // ── Appel HTTP vers le micro-service Python ───────────────────────
    @SuppressWarnings("unchecked")
    private String appellerServiceOCR(MultipartFile file) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() { return file.getOriginalFilename(); }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<Map> response = restTemplate.postForEntity(ocrServiceUrl, requestEntity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return (String) response.getBody().get("text");
            }
            throw new RuntimeException("Le service OCR a retourné une réponse invalide.");

        } catch (Exception e) {
            throw new RuntimeException(
                "Service OCR inaccessible. Vérifiez que le service Python est démarré sur le port 8000. Détail : "
                + e.getMessage());
        }
    }

    // ── Nettoyage d'une ligne ─────────────────────────────────────────
    private String nettoyer(String ligne) {
        return ligne
            .replaceAll("^\\s*[-•►▶▸✓✔☑□■●○◦]+\\s*", "")
            .replaceAll("^\\d+[.)\\-]\\s*", "")
            .replaceAll("\\s{2,}", " ")
            .trim();
    }

    // ── Classification par mots-clés ──────────────────────────────────
    private Critere.TypeCritere detecterType(String ligne) {
        String lower = ligne.toLowerCase();

        String[] motsClesSecurite = {
            "sécurité","securite","epi","casque","gants","lunette","5s",
            "incendie","urgence","risque","danger","accident","protection",
            "consigne","extinction","évacuation","evacuation","habilitation"
        };
        String[] motsClesQualite = {
            "qualité","qualite","contrôle","controle","vérification","verification",
            "conformité","conformite","tolérance","tolerance","mesure","calibre",
            "étalonnage","etalonnage","non-conformité","rebut","retouche",
            "traçabilité","tracabilite","inspection","audit","échantillon"
        };

        int scoreSecurite = 0, scoreQualite = 0;
        for (String kw : motsClesSecurite) if (lower.contains(kw)) scoreSecurite++;
        for (String kw : motsClesQualite)  if (lower.contains(kw)) scoreQualite++;

        if (scoreSecurite > scoreQualite && scoreSecurite > 0) return Critere.TypeCritere.SECURITE;
        if (scoreQualite  > scoreSecurite && scoreQualite > 0) return Critere.TypeCritere.QUALITE;
        return Critere.TypeCritere.TECHNIQUE;
    }
}