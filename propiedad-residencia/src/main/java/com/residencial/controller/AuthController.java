package com.residencial.controller;

import com.residencial.model.Usuario;
import com.residencial.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestBody Map<String, String> body) {

        String token = authService.login(
                body.get("email"),
                body.get("password"));

        return ResponseEntity.ok(Map.of("token", token));
    }

    // POST /api/auth/registro
    @PostMapping("/registro")
    public ResponseEntity<Usuario> registro(
            @RequestBody Map<String, String> body) {

        Usuario nuevo = Usuario.builder()
                .nombre(body.get("nombre"))
                .email(body.get("email"))
                .telefono(body.get("telefono"))
                .rol(Usuario.Rol.valueOf(body.get("rol")))
                .build();

        return ResponseEntity.ok(
                authService.registrar(nuevo, body.get("password")));
    }

}