package com.residencial.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "reservas")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EspacioComun espacio;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(nullable = false)
    private LocalTime horaInicio;

    @Column(nullable = false)
    private LocalTime horaFin;

    private Integer numeroPersonas;

    private String motivo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "residente_id", nullable = false)
    private Usuario residente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    public enum EspacioComun {
        SALON_COMUNAL, PISCINA, ZONA_BBQ,
        CANCHA_MULTIPLE, CANCHA_TENIS, GIMNASIO
    }

    public enum EstadoReserva {
        CONFIRMADA, CANCELADA, PASADA
    }
}