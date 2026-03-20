package com.residencial.dto;

import com.residencial.model.Solicitud;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SolicitudDTO {

    private Long   id;
    private String numero;
    private String titulo;
    private String descripcion;
    private String tipo;
    private String prioridad;
    private String estado;
    private String ubicacion;

    // Solo datos básicos del residente — sin relaciones anidadas
    private Long   residenteId;
    private String residenteNombre;
    private String residenteEmail;

    private Long   asignadoId;
    private String asignadoNombre;

    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
    private LocalDateTime resueltaEn;

    // Constructor desde entidad
    public static SolicitudDTO from(Solicitud s) {
        SolicitudDTO dto = new SolicitudDTO();
        dto.setId(s.getId());
        dto.setNumero(s.getNumero());
        dto.setTitulo(s.getTitulo());
        dto.setDescripcion(s.getDescripcion());
        dto.setTipo(capitalize(s.getTipo().name()));
        dto.setPrioridad(s.getPrioridad().name());
        dto.setEstado(s.getEstado().name());
        dto.setUbicacion(s.getUbicacion());
        dto.setCreadoEn(s.getCreadoEn());
        dto.setActualizadoEn(s.getActualizadoEn());
        dto.setResueltaEn(s.getResueltaEn());

        if (s.getResidente() != null) {
            dto.setResidenteId(s.getResidente().getId());
            dto.setResidenteNombre(s.getResidente().getNombre());
            dto.setResidenteEmail(s.getResidente().getEmail());
        }
        if (s.getAsignadoA() != null) {
            dto.setAsignadoId(s.getAsignadoA().getId());
            dto.setAsignadoNombre(s.getAsignadoA().getNombre());
        }
        return dto;
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}