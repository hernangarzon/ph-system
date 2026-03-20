package com.residencial.service;

import com.residencial.model.*;
import com.residencial.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository   usuarioRepository;

    // ── Crear solicitud ──
    @Transactional
    public Solicitud crear(Solicitud solicitud) {
        solicitud.setEstado(Solicitud.EstadoSolicitud.PENDIENTE);

        // Número temporal — el trigger de Supabase lo actualizará
        // pero por si falla, generamos uno desde Java
        solicitud.setNumero("TEMP-" + System.currentTimeMillis());

        Solicitud guardada = solicitudRepository.save(solicitud);

        // Actualizar con el número real usando el ID generado
        String numero = "SOL-" +
                java.time.Year.now().getValue() + "-" +
                String.format("%04d", guardada.getId());
        guardada.setNumero(numero);

        return solicitudRepository.save(guardada);
    }

    // ── Listar por residente (paginado) ──
    @Transactional(readOnly = true)
    public Page<Solicitud> listarPorResidente(Long residenteId, Pageable pageable) {
        Usuario residente = usuarioRepository.findById(residenteId)
                .orElseThrow(() -> new RuntimeException("Residente no encontrado"));
        return solicitudRepository
                .findByResidenteOrderByCreadoEnDesc(residente, pageable);
    }

    // ── Listar todas (admin) ──
    @Transactional(readOnly = true)
    public Page<Solicitud> listarTodas(Pageable pageable) {
        return solicitudRepository.findAll(pageable);
    }

    // ── Listar sin asignar (admin) ──
    @Transactional(readOnly = true)
    public List<Solicitud> listarSinAsignar() {
        return solicitudRepository.findSinAsignar();
    }

    // ── Obtener por número ──
    @Transactional(readOnly = true)
    public Solicitud buscarPorNumero(String numero) {
        return solicitudRepository.findByNumero(numero)
                .orElseThrow(() -> new RuntimeException(
                        "Solicitud no encontrada: " + numero));
    }

    // ── Asignar a personal ──
    @Transactional
    public Solicitud asignar(Long solicitudId, Long personalId) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        Usuario personal = usuarioRepository.findById(personalId)
                .orElseThrow(() -> new RuntimeException("Personal no encontrado"));

        solicitud.setAsignadoA(personal);
        solicitud.setAsignadaEn(LocalDateTime.now());

        if (solicitud.getEstado() == Solicitud.EstadoSolicitud.PENDIENTE ||
                solicitud.getEstado() == Solicitud.EstadoSolicitud.SIN_ASIGNAR) {
            solicitud.setEstado(Solicitud.EstadoSolicitud.EN_PROCESO);
        }

        return solicitudRepository.save(solicitud);
    }

    // ── Cambiar estado ──
    @Transactional
    public Solicitud cambiarEstado(Long solicitudId,
                                   Solicitud.EstadoSolicitud nuevoEstado) {
        Solicitud solicitud = solicitudRepository.findById(solicitudId)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        solicitud.setEstado(nuevoEstado);

        if (nuevoEstado == Solicitud.EstadoSolicitud.RESUELTO) {
            solicitud.setResueltaEn(LocalDateTime.now());
        }

        return solicitudRepository.save(solicitud);
    }

    // ── Conteo por estado (dashboard) ──
    public long contarPorEstado(Solicitud.EstadoSolicitud estado) {
        return solicitudRepository.countByEstado(estado);
    }
}