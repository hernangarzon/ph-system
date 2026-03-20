package com.residencial.controller;

import com.residencial.model.Usuario;
import com.residencial.model.Unidad;
import com.residencial.repository.UsuarioRepository;
import com.residencial.repository.UnidadRepository;
import com.residencial.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioRepository  usuarioRepository;
    private final UnidadRepository   unidadRepository;
    private final AuthService        authService;
    private final PasswordEncoder    passwordEncoder;

    // GET /api/usuarios — admin lista todos
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioRepository.findAll());
    }

    // GET /api/usuarios/residentes — solo residentes
    @GetMapping("/residentes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listarResidentes() {
        return ResponseEntity.ok(
                usuarioRepository.findAll().stream()
                        .filter(u -> u.getRol() == Usuario.Rol.RESIDENTE)
                        .toList()
        );
    }

    // GET /api/usuarios/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESIDENTE')")
    public ResponseEntity<Usuario> buscarPorId(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/usuarios — admin crea residente
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> crear(@RequestBody Map<String, String> body) {
        Usuario nuevo = Usuario.builder()
                .nombre(body.get("nombre"))
                .email(body.get("email"))
                .telefono(body.get("telefono"))
                .rol(Usuario.Rol.valueOf(
                        body.getOrDefault("rol", "RESIDENTE")))
                .build();

        // Asignar unidad si viene en el body
        if (body.get("unidadId") != null) {
            unidadRepository.findById(Long.parseLong(body.get("unidadId")))
                    .ifPresent(nuevo::setUnidad);
        }

        return ResponseEntity.ok(
                authService.registrar(nuevo, body.get("password")));
    }

    // PUT /api/usuarios/{id} — admin actualiza residente
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> actualizar(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {

        return usuarioRepository.findById(id).map(usuario -> {
            if (body.get("nombre")    != null) usuario.setNombre(body.get("nombre"));
            if (body.get("telefono")  != null) usuario.setTelefono(body.get("telefono"));
            if (body.get("password")  != null)
                usuario.setPassword(passwordEncoder.encode(body.get("password")));
            if (body.get("unidadId")  != null)
                unidadRepository.findById(Long.parseLong(body.get("unidadId")))
                        .ifPresent(usuario::setUnidad);
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }

    // PATCH /api/usuarios/{id}/activar — activar/desactivar
    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Usuario> toggleActivo(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body) {

        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setActivo(body.get("activo"));
            return ResponseEntity.ok(usuarioRepository.save(usuario));
        }).orElse(ResponseEntity.notFound().build());
    }

    // GET /api/usuarios/personal — personal de mantenimiento y portería
    @GetMapping("/personal")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Usuario>> listarPersonal() {
        return ResponseEntity.ok(
                usuarioRepository.findAll().stream()
                        .filter(u -> u.getRol() == Usuario.Rol.PORTERIA)
                        .toList()
        );
    }
}