package com.residencial.service;

import com.residencial.model.Usuario;
import com.residencial.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository    usuarioRepository;
    private final PasswordEncoder      passwordEncoder;
    private final JwtService           jwtService;
    private final AuthenticationManager authenticationManager;

    // ── Login ──
    public String login(String email, String password) {
        // Buscar usuario manualmente para ver qué hash tiene
        usuarioRepository.findByEmail(email).ifPresent(u -> {
        });

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));
        } catch (Exception e) {
            System.out.println("=== ERROR: " + e.getMessage());
            throw e;
        }

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return jwtService.generateToken(
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getId(),
                usuario.getNombre());
    }

    // ── Registro de nuevo residente ──
    public Usuario registrar(Usuario nuevo, String passwordPlano) {
        if (usuarioRepository.existsByEmail(nuevo.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        nuevo.setPassword(passwordEncoder.encode(passwordPlano));
        return usuarioRepository.save(nuevo);
    }
}