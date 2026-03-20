package com.residencial.repository;

import com.residencial.model.Reserva;
import com.residencial.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long> {

    List<Reserva> findByResidenteOrderByFechaDesc(Usuario residente);

    List<Reserva> findByFechaAndEspacio(
            LocalDate fecha, Reserva.EspacioComun espacio);

    // Verificar si un horario ya está ocupado
    @Query("SELECT COUNT(r) > 0 FROM Reserva r " +
            "WHERE r.espacio = :espacio " +
            "AND r.fecha = :fecha " +
            "AND r.estado = 'CONFIRMADA' " +
            "AND r.horaInicio < :horaFin " +
            "AND r.horaFin > :horaInicio")
    boolean existeSolapamiento(
            @Param("espacio")    Reserva.EspacioComun espacio,
            @Param("fecha")      LocalDate fecha,
            @Param("horaInicio") java.time.LocalTime horaInicio,
            @Param("horaFin")    java.time.LocalTime horaFin);
}