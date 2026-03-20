package com.residencial.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Representa un apartamento / unidad residencial dentro del conjunto.
 */
@Entity
@Table(name = "unidades")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Unidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String numero;          // "301"

    @Column(nullable = false)
    private String torre;           // "Torre A"

    @Column(nullable = false)
    private Integer piso;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPropietario tipo;

    @Column(nullable = false)
    private Boolean ocupada = true;

    private LocalDate fechaIngreso;

    // ── Estado cuota ──
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCuota estadoCuota = EstadoCuota.AL_DIA;

    // ── Auditoría ──
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime creadoEn;

    // ── Relaciones ──
    @OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY)
    private List<Usuario> residentes;

    @OneToMany(mappedBy = "unidad", fetch = FetchType.LAZY)
    private List<Solicitud> solicitudes;

    // ── Enums ──
    public enum TipoPropietario { PROPIETARIO, ARRENDATARIO }
    public enum EstadoCuota     { AL_DIA, PENDIENTE, MOROSO }

    // ── Helper ──
    public String getCodigoCompleto() {
        return numero + " – " + torre;
    }
}
