package com.example.controller;

import com.example.dto.MachineDTO;
import com.example.dto.MachineRequest;
import com.example.entity.Machine;
import com.example.repository.MachineRepository;
import com.example.service.MachineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/machines")
@RequiredArgsConstructor
public class MachineController {

    private final MachineService machineService;
    private final MachineRepository machineRepository;

    @GetMapping
    public ResponseEntity<List<MachineDTO>> getAll() {
        return ResponseEntity.ok(machineService.findAll());
    }

    @GetMapping("/processus/{id}")
    public ResponseEntity<List<MachineDTO>> getByProcessus(@PathVariable("id") Long id) {
        return ResponseEntity.ok(machineService.findByProcessus(id));
    }

    @PostMapping
    public ResponseEntity<MachineDTO> create(@RequestBody MachineRequest req) {
        return ResponseEntity.ok(machineService.create(req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MachineDTO> update(@PathVariable("id") Long id,
                                              @RequestBody MachineRequest req) {
        return ResponseEntity.ok(machineService.update(id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        Machine machine = machineService.findById(id);
        if (machine.getProcessus() != null) {
            throw new RuntimeException("Machine liée à un processus");
        }
        machineService.delete(id);
        return ResponseEntity.noContent().build();
    }
}