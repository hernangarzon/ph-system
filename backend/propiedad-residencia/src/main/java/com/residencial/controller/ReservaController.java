package com.residencial.controller;

import com.residencial.model.Reserva;
import com.residencial.model.Usuario;
import com.residencial.repository.ReservaRepository;
import com.residencial.repository.UsuarioRepository;
import com.residencial.repository.UnidadRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReservaController {

    private final ReservaRepository  reservaRepository;
    private final UsuarioRepository  usuarioRepository;
    private final UnidadRepository   unidadRepository;

    // GET /api/reservas — admin ve todas
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Reserva>> listarTodas() {
        return ResponseEntity.ok(reservaRepository.findAll());
    }

    // GET /api/reservas/mis-reservas — residente ve las suyas
    @GetMapping("/mis-reservas")
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<List<Reserva>> misReservas(Authentication auth) {
        Usuario residente = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return ResponseEntity.ok(
                reservaRepository.findByResidenteOrderByFechaDesc(residente));
    }

    // GET /api/reservas/disponibilidad?espacio=SALON_COMUNAL&fecha=2025-03-21
    @GetMapping("/disponibilidad")
    @PreAuthorize("hasAnyRole('RESIDENTE','ADMIN')")
    public ResponseEntity<List<Reserva>> disponibilidad(
            @RequestParam String espacio,
            @RequestParam String fecha) {

        return ResponseEntity.ok(
                reservaRepository.findByFechaAndEspacio(
                        LocalDate.parse(fecha),
                        Reserva.EspacioComun.valueOf(espacio)));
    }

    // POST /api/reservas — residente crea reserva
    @PostMapping
    @PreAuthorize("hasRole('RESIDENTE')")
    public ResponseEntity<?> crear(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        Usuario residente = usuarioRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        LocalDate fecha      = LocalDate.parse(body.get("fecha"));
        LocalTime horaInicio = LocalTime.parse(body.get("horaInicio"));
        LocalTime horaFin    = LocalTime.parse(body.get("horaFin"));
        Reserva.EspacioComun espacio =
                Reserva.EspacioComun.valueOf(body.get("espacio"));

        // Verificar solapamiento
        boolean solapado = reservaRepository.existeSolapamiento(
                espacio, fecha, horaInicio, horaFin);

        if (solapado) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error",
                            "El horario ya está reservado para ese espacio"));
        }

        Reserva reserva = Reserva.builder()
                .espacio(espacio)
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(horaFin)
                .numeroPersonas(body.get("numeroPersonas") != null
                        ? Integer.parseInt(body.get("numeroPersonas")) : null)
                .motivo(body.get("motivo"))
                .residente(residente)
                .estado(Reserva.EstadoReserva.CONFIRMADA)
                .build();

        // Asignar unidad si existe
        if (residente.getUnidad() != null) {
            reserva.setUnidad(residente.getUnidad());
        }

        return ResponseEntity.ok(reservaRepository.save(reserva));
    }

    // PATCH /api/reservas/{id}/cancelar
    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('RESIDENTE','ADMIN')")
    public ResponseEntity<Reserva> cancelar(
            @PathVariable Long id,
            Authentication auth) {

        return reservaRepository.findById(id).map(reserva -> {
            reserva.setEstado(Reserva.EstadoReserva.CANCELADA);
            return ResponseEntity.ok(reservaRepository.save(reserva));
        }).orElse(ResponseEntity.notFound().build());
    }
}