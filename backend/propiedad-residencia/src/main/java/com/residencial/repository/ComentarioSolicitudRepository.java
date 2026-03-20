package com.residencial.repository;

import com.residencial.model.ComentarioSolicitud;
import com.residencial.model.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComentarioSolicitudRepository extends JpaRepository<ComentarioSolicitud, Long> {

    List<ComentarioSolicitud> findBySolicitudOrderByCreadoEnAsc(Solicitud solicitud);
}