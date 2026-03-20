package com.residencial.repository;

import com.residencial.model.RegistroAcceso;
import com.residencial.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RegistroAccesoRepository extends JpaRepository<RegistroAcceso, Long> {

    Page<RegistroAcceso> findByOrderByFechaHoraDesc(Pageable pageable);

    List<RegistroAcceso> findByFechaHoraBetweenOrderByFechaHoraDesc(
            LocalDateTime inicio, LocalDateTime fin);

    long countByTipoAndFechaHoraBetween(
            RegistroAcceso.TipoAcceso tipo,
            LocalDateTime inicio,
            LocalDateTime fin);

    List<RegistroAcceso> findByPortero(Usuario portero);
}