package com.residencial.controller;

import com.residencial.model.Solicitud;
import com.residencial.model.RegistroAcceso;
import com.residencial.repository.SolicitudRepository;
import com.residencial.repository.UsuarioRepository;
import com.residencial.repository.ReservaRepository;
import com.residencial.repository.RegistroAccesoRepository;
import com.residencial.repository.UnidadRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ReporteController {

    private final SolicitudRepository      solicitudRepository;
    private final UsuarioRepository        usuarioRepository;
    private final ReservaRepository        reservaRepository;
    private final RegistroAccesoRepository registroRepository;
    private final UnidadRepository         unidadRepository;

    // GET /api/reportes/dashboard — KPIs principales del admin
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> dashboard() {

        // Solicitudes por estado
        long sinAsignar = solicitudRepository
                .countByEstado(Solicitud.EstadoSolicitud.SIN_ASIGNAR);
        long pendientes = solicitudRepository
                .countByEstado(Solicitud.EstadoSolicitud.PENDIENTE);
        long enProceso  = solicitudRepository
                .countByEstado(Solicitud.EstadoSolicitud.EN_PROCESO);
        long resueltas  = solicitudRepository
                .countByEstado(Solicitud.EstadoSolicitud.RESUELTO);
        long abiertas   = sinAsignar + pendientes + enProceso;

        // Resueltas hoy
        LocalDateTime inicioDia = LocalDate.now().atStartOfDay();
        LocalDateTime finDia    = inicioDia.plusDays(1);

        // Residentes y unidades
        long totalResidentes = usuarioRepository.findAll().stream()
                .filter(u -> u.getRol().name().equals("RESIDENTE"))
                .count();

        long cuotasPendientes = unidadRepository
                .findByEstadoCuota(com.residencial.model.Unidad.EstadoCuota.PENDIENTE)
                .size();

        long totalUnidades = unidadRepository.count();

        double pctCuotasAlDia = totalUnidades > 0
                ? Math.round(((totalUnidades - cuotasPendientes)
                / (double) totalUnidades) * 100)
                : 0;

        // Accesos hoy
        long entradasHoy = registroRepository.countByTipoAndFechaHoraBetween(
                RegistroAcceso.TipoAcceso.ENTRADA, inicioDia, finDia);
        long salidasHoy  = registroRepository.countByTipoAndFechaHoraBetween(
                RegistroAcceso.TipoAcceso.SALIDA,  inicioDia, finDia);

        return ResponseEntity.ok(Map.of(
                "solicitudes", Map.of(
                        "abiertas",    abiertas,
                        "sinAsignar",  sinAsignar,
                        "pendientes",  pendientes,
                        "enProceso",   enProceso,
                        "resueltas",   resueltas
                ),
                "residentes", Map.of(
                        "total",           totalResidentes,
                        "cuotasPendientes", cuotasPendientes,
                        "pctCuotasAlDia",  pctCuotasAlDia
                ),
                "accesos", Map.of(
                        "entradasHoy", entradasHoy,
                        "salidasHoy",  salidasHoy,
                        "dentroAhora", Math.max(0, entradasHoy - salidasHoy)
                )
        ));
    }

    // GET /api/reportes/solicitudes-por-tipo — para gráfica donut
    @GetMapping("/solicitudes-por-tipo")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> solicitudesPorTipo() {
        Map<String, Long> resultado = new java.util.LinkedHashMap<>();
        for (Solicitud.TipoSolicitud tipo : Solicitud.TipoSolicitud.values()) {
            long count = solicitudRepository.findAll().stream()
                    .filter(s -> s.getTipo() == tipo)
                    .count();
            resultado.put(tipo.name(), count);
        }
        return ResponseEntity.ok(resultado);
    }
}