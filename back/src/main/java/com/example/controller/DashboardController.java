package com.example.controller;

import com.example.dto.DashboardStatsDTO;
import com.example.dto.ProcessusCountDTO;
import com.example.dto.ChecklistRecentDTO;
import com.example.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN','PPO','OPERATEUR','CHEF_LIGNE','AGENT_QUALITE','RESPONSABLE_PRODUCTION','TECHNICIEN')")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getGlobalStats());
    }

    @GetMapping("/processus-counts")
    @PreAuthorize("hasAnyRole('ADMIN','PPO','CHEF_LIGNE','AGENT_QUALITE','RESPONSABLE_PRODUCTION','TECHNICIEN')")
    public ResponseEntity<List<ProcessusCountDTO>> getProcessusCounts() {
        return ResponseEntity.ok(dashboardService.getChecklistCountsByProcessus());
    }

    @GetMapping("/recent-checklists")
    @PreAuthorize("hasAnyRole('ADMIN','PPO','OPERATEUR','CHEF_LIGNE','AGENT_QUALITE','RESPONSABLE_PRODUCTION','TECHNICIEN')")
    public ResponseEntity<List<ChecklistRecentDTO>> getRecentChecklists(
            @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentChecklists(limit));
    }

    @GetMapping("/stats/period")
    @PreAuthorize("hasAnyRole('ADMIN','PPO','AGENT_QUALITE','RESPONSABLE_PRODUCTION')")
    public ResponseEntity<DashboardStatsDTO> getStatsByPeriod(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate) {
        return ResponseEntity.ok(dashboardService.getStatsByPeriod(startDate, endDate));
    }
}