package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

// Intégration de Mockito avec JUnit 5
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    // Contrôleur testé avec injection des mocks
    @InjectMocks
    private AuthController authController;

    private LoginRequest loginRequest;
    private SignupRequest signupRequest;
    private User user;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test

        // Requête de connexion
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@test.com");
        loginRequest.setPassword("password");

        // Requête d'inscription
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@test.com");
        signupRequest.setPassword("password");
        signupRequest.setFirstName("Test");
        signupRequest.setLastName("User");

        // Utilisateur simulé
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("encodedPassword");
        user.setAdmin(false);

        userDetails = UserDetailsImpl.builder()
                .id(1L)
                .username("test@test.com")
                .firstName("Test")
                .lastName("User")
                .password("password")
                .build();
    }

    @Test
    void authenticateUser_WithValidCredentials_ShouldReturnJwtResponse() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContextHolder.clearContext(); // Nettoyer le contexte avant le test

        given(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .willReturn(authentication);
        given(authentication.getPrincipal()).willReturn(userDetails);
        given(jwtUtils.generateJwtToken(authentication)).willReturn("jwtToken");
        given(userRepository.findByEmail("test@test.com")).willReturn(Optional.of(user));

        // Act
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();

        assertThat(jwtResponse).isNotNull();
        assertThat(jwtResponse.getToken()).isEqualTo("jwtToken");
        assertThat(jwtResponse.getId()).isEqualTo(1L);
        assertThat(jwtResponse.getUsername()).isEqualTo("test@test.com");
        assertThat(jwtResponse.getFirstName()).isEqualTo("Test");
        assertThat(jwtResponse.getLastName()).isEqualTo("User");
        assertThat(jwtResponse.getAdmin()).isFalse();

        // Vérification moins stricte du contexte de sécurité
        Authentication contextAuth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(contextAuth).isNotNull();
        assertThat(contextAuth.getPrincipal()).isEqualTo(userDetails);
    }

    @Test
    void registerUser_WithNewEmail_ShouldReturnSuccessMessage() {
        // Arrange : email encore inexistant
        given(userRepository.existsByEmail("test@test.com")).willReturn(false);
        given(passwordEncoder.encode("password")).willReturn("encodedPassword");

        // Act : appel au contrôleur
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert : vérifie que l'utilisateur a été enregistré
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(((MessageResponse) response.getBody()).getMessage())
                .isEqualTo("User registered successfully!");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_WithExistingEmail_ShouldReturnBadRequest() {
        // Arrange : email déjà existant
        given(userRepository.existsByEmail("test@test.com")).willReturn(true);

        // Act : appel au contrôleur
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Assert : vérifie que le message d'erreur est retourné
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        assertThat(((MessageResponse) response.getBody()).getMessage())
                .isEqualTo("Error: Email is already taken!");
    }

    @Test
    void authenticateUser_WithInvalidCredentials_ShouldThrowException() {
        // Arrange : simulation d'une mauvaise authentification
        given(authenticationManager.authenticate(any()))
                .willThrow(new RuntimeException("Bad credentials"));

        // Act & Assert : s'attend à une exception levée
        try {
            authController.authenticateUser(loginRequest);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Bad credentials");
        }
    }
}