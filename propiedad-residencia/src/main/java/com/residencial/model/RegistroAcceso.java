package com.residencial.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_acceso")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class RegistroAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoAcceso tipo;

    @Column(nullable = false)
    private String nombreVisitante;

    private String documento;

    private String placaVehiculo;

    @Column(nullable = false)
    private String destinoApto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portero_id")
    private Usuario portero;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    private boolean autorizado = true;

    private String observaciones;

    public enum TipoAcceso {
        ENTRADA, SALIDA, DELIVERY, TAXI
    }
}