package com.residencial.repository;

import com.residencial.model.Solicitud;
import com.residencial.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, Long> {

    Optional<Solicitud> findByNumero(String numero);

    // Todas las solicitudes de un residente, paginadas
    Page<Solicitud> findByResidenteOrderByCreadoEnDesc(Usuario residente, Pageable pageable);

    // Solicitudes por estado
    Page<Solicitud> findByEstadoOrderByCreadoEnDesc(
            Solicitud.EstadoSolicitud estado, Pageable pageable);

    // Solicitudes asignadas a un técnico
    List<Solicitud> findByAsignadoAAndEstadoNot(
            Usuario asignadoA, Solicitud.EstadoSolicitud estado);

    // Conteo por estado (para el dashboard del admin)
    long countByEstado(Solicitud.EstadoSolicitud estado);

    // Solicitudes sin asignar
    @Query("SELECT s FROM Solicitud s WHERE s.asignadoA IS NULL " +
            "AND s.estado != 'CANCELADO' AND s.estado != 'RESUELTO' " +
            "ORDER BY s.creadoEn ASC")
    List<Solicitud> findSinAsignar();
}