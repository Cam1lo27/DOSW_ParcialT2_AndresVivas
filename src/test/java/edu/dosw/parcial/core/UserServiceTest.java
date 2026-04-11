package edu.dosw.parcial.core;

import edu.dosw.parcial.controller.dtos.request.RegisterRequest;
import edu.dosw.parcial.core.exception.BusinessException;
import edu.dosw.parcial.core.models.RoleEnum;
import edu.dosw.parcial.core.services.UserService;
import edu.dosw.parcial.persistence.entities.UserEntity;
import edu.dosw.parcial.persistence.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest();
        validRequest.setFullName("Andres Vivas");
        validRequest.setEmail("andres.vivas-b@mail.escuelaing.edu.co");
        validRequest.setPassword("123456");
    }

    @Test
    void register_exitoso() {
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(userRepository.save(any())).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        var response = userService.register(validRequest);

        assertNotNull(response);
        assertEquals("andres@unal.edu.co", response.getEmail());
        assertEquals("CLIENTE", response.getRole());
        verify(userRepository).save(any());
    }

    @Test
    void register_emailNoInstitucional_lanzaExcepcion() {
        validRequest.setEmail("andres@gmail.com");

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register(validRequest));

        assertEquals("El correo debe ser institucional", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_emailYaExiste_lanzaExcepcion() {
        when(userRepository.existsByEmail(any())).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> userService.register(validRequest));

        assertEquals("El correo ya está registrado", ex.getMessage());
    }

    @Test
    void getUserByEmail_usuarioNoExiste_lanzaExcepcion() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> userService.getUserByEmail("noexiste@unal.edu.co"));
    }
}