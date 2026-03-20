package com.residencial.controller;

import com.residencial.model.RegistroAcceso;
import com.residencial.model.Usuario;
import com.residencial.repository.RegistroAccesoRepository;
import com.residencial.repository.UsuarioRepository;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/accesos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class RegistroAccesoController {

    private final RegistroAccesoRepository registroRepository;
    private final UsuarioRepository        usuarioRepository;

    // GET /api/accesos — admin y portería ven todos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','PORTERIA')")
    public ResponseEntity<Page<RegistroAcceso>> listar(
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(
                registroRepository.findByOrderByFechaHoraDesc(pageable));
    }

    // GET /api/accesos/hoy — registros del día actual
    @GetMapping("/hoy")
    @PreAuthorize("hasAnyRole('ADMIN','PORTERIA')")
    public ResponseEntity<?> hoy() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        LocalDateTime fin    = inicio.plusDays(1);

        var registros = registroRepository
                .findByFechaHoraBetweenOrderByFechaHoraDesc(inicio, fin);

        // Conteos del día
        long entradas   = registroRepository.countByTipoAndFechaHoraBetween(
                RegistroAcceso.TipoAcceso.ENTRADA,  inicio, fin);
        long salidas    = registroRepository.countByTipoAndFechaHoraBetween(
                RegistroAcceso.TipoAcceso.SALIDA,   inicio, fin);
        long domicilios = registroRepository.countByTipoAndFechaHoraBetween(
                RegistroAcceso.TipoAcceso.DELIVERY, inicio, fin);

        return ResponseEntity.ok(Map.of(
                "registros",   registros,
                "entradas",    entradas,
                "salidas",     salidas,
                "domicilios",  domicilios,
                "dentro",      Math.max(0, entradas - salidas)
        ));
    }

    // POST /api/accesos — portería registra acceso
    @PostMapping
    @PreAuthorize("hasAnyRole('PORTERIA','ADMIN')")
    public ResponseEntity<RegistroAcceso> registrar(
            @RequestBody Map<String, String> body,
            Authentication auth) {

        Usuario portero = usuarioRepository.findByEmail(auth.getName())
                .orElse(null);

        RegistroAcceso registro = RegistroAcceso.builder()
                .tipo(RegistroAcceso.TipoAcceso.valueOf(body.get("tipo")))
                .nombreVisitante(body.get("nombreVisitante"))
                .documento(body.get("documento"))
                .placaVehiculo(body.get("placaVehiculo"))
                .destinoApto(body.get("destinoApto"))
                .portero(portero)
                .fechaHora(LocalDateTime.now())
                .autorizado(true)
                .observaciones(body.get("observaciones"))
                .build();

        return ResponseEntity.ok(registroRepository.save(registro));
    }
}