package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.RegisterRequest;
import edu.dosw.parcial.controller.dtos.response.UserResponse;
import edu.dosw.parcial.core.exception.BusinessException;
import edu.dosw.parcial.core.models.RoleEnum;
import edu.dosw.parcial.persistence.entities.UserEntity;
import edu.dosw.parcial.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request) {
        log.info("Intentando registrar usuario: {}", request.getEmail());

        if (!request.getEmail().contains("edu")) {
            log.warn("Email no institucional: {}", request.getEmail());
            throw new BusinessException("El correo debe ser institucional", HttpStatus.BAD_REQUEST);
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("[REGISTER] Email ya registrado: {}", request.getEmail());
            throw new BusinessException("El correo ya está registrado", HttpStatus.CONFLICT);
        }

        UserEntity user = UserEntity.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(RoleEnum.CLIENTE)
                .build();

        UserEntity saved = userRepository.save(user);
        log.info("Usuario registrado con id: {}", saved.getId());
        return toResponse(saved);
    }

    public UserResponse getUserByEmail(String email) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", HttpStatus.NOT_FOUND));
        return toResponse(user);
    }

    private UserResponse toResponse(UserEntity user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build();
    }
}