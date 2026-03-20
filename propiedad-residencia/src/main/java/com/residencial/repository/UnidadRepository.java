package com.residencial.repository;

import com.residencial.model.Unidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnidadRepository extends JpaRepository<Unidad, Long> {

    Optional<Unidad> findByNumeroAndTorre(String numero, String torre);

    List<Unidad> findByTorre(String torre);

    List<Unidad> findByEstadoCuota(Unidad.EstadoCuota estadoCuota);
}