package com.residencial.controller;

import com.residencial.dto.SolicitudDTO;
import com.residencial.model.Solicitud;
import com.residencial.service.SolicitudService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/solicitudes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SolicitudController {

    private final SolicitudService solicitudService;

    // POST /api/solicitudes
    @PostMapping
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<SolicitudDTO> crear(
            @RequestBody Solicitud solicitud) {
        return ResponseEntity.ok(
                SolicitudDTO.from(solicitudService.crear(solicitud)));
    }

    // GET /api/solicitudes — admin
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<SolicitudDTO>> listarTodas(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("creadoEn").descending());
        return ResponseEntity.ok(
                solicitudService.listarTodas(pageable)
                        .map(SolicitudDTO::from));
    }

    // GET /api/solicitudes/residente/{id}
    @GetMapping("/residente/{id}")
    @PreAuthorize("hasAnyRole('RESIDENTE','ADMIN')")
    public ResponseEntity<Page<SolicitudDTO>> listarPorResidente(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                solicitudService.listarPorResidente(id, pageable)
                        .map(SolicitudDTO::from));
    }

    // GET /api/solicitudes/sin-asignar
    @GetMapping("/sin-asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SolicitudDTO>> sinAsignar() {
        return ResponseEntity.ok(
                solicitudService.listarSinAsignar().stream()
                        .map(SolicitudDTO::from)
                        .toList());
    }

    // GET /api/solicitudes/{numero}
    @GetMapping("/{numero}")
    @PreAuthorize("hasAnyRole('RESIDENTE','ADMIN','PORTERIA')")
    public ResponseEntity<SolicitudDTO> buscarPorNumero(
            @PathVariable String numero) {
        return ResponseEntity.ok(
                SolicitudDTO.from(solicitudService.buscarPorNumero(numero)));
    }

    // PATCH /api/solicitudes/{id}/asignar
    @PatchMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SolicitudDTO> asignar(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        return ResponseEntity.ok(
                SolicitudDTO.from(
                        solicitudService.asignar(id, body.get("personalId"))));
    }

    // PATCH /api/solicitudes/{id}/estado
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN','PORTERIA')")
    public ResponseEntity<SolicitudDTO> cambiarEstado(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        Solicitud.EstadoSolicitud estado =
                Solicitud.EstadoSolicitud.valueOf(body.get("estado"));
        return ResponseEntity.ok(
                SolicitudDTO.from(
                        solicitudService.cambiarEstado(id, estado)));
    }

    // GET /api/solicitudes/conteo/{estado}
    @GetMapping("/conteo/{estado}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> conteo(
            @PathVariable String estado) {
        long total = solicitudService.contarPorEstado(
                Solicitud.EstadoSolicitud.valueOf(estado));
        return ResponseEntity.ok(Map.of("total", total));
    }
}