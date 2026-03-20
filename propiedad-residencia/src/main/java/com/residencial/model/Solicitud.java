package com.residencial.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Solicitud creada por un residente.
 * Puede ser de mantenimiento, queja, reserva, visita, pago u otro.
 */
@Entity
@Table(name = "solicitudes")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Solicitud {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String numero;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoSolicitud tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Prioridad prioridad = Prioridad.MEDIA;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado = EstadoSolicitud.PENDIENTE;

    private String ubicacion;

    // ── Relaciones ──
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "residente_id")
    private Usuario residente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asignado_a_id")
    private Usuario asignadoA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @OneToMany(mappedBy = "solicitud", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ComentarioSolicitud> comentarios = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "solicitud_archivos", joinColumns = @JoinColumn(name = "solicitud_id"))
    @Column(name = "url_archivo")
    @Builder.Default
    private List<String> archivos = new ArrayList<>();

    // ── Auditoría ──
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    @UpdateTimestamp
    private LocalDateTime actualizadoEn;

    private LocalDateTime resueltaEn;
    private LocalDateTime asignadaEn;

    // ── Enums ──
    public enum TipoSolicitud {
        MANTENIMIENTO, QUEJA, RESERVA, VISITA, PAGO, OTRO
    }

    public enum Prioridad {
        BAJA, MEDIA, ALTA
    }

    public enum EstadoSolicitud {
        SIN_ASIGNAR, PENDIENTE, EN_PROCESO, RESUELTO, CANCELADO
    }
}
