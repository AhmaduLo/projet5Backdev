package com.openclassrooms.starterjwt.integrationController;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.payload.request.LoginRequest;
import com.openclassrooms.starterjwt.payload.request.SignupRequest;
import com.openclassrooms.starterjwt.payload.response.JwtResponse;
import com.openclassrooms.starterjwt.payload.response.MessageResponse;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;

import com.openclassrooms.starterjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Indique que c’est un test d’intégration Spring Boot
@SpringBootTest

// Configure automatiquement MockMvc pour simuler les requêtes HTTP
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc; // Permet d'exécuter des requêtes HTTP sans démarrer le serveur

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    // Données de test réutilisées
    private final String testEmail = "test@test.com";
    private final String testPassword = "password";
    private final String testFirstName = "Test";
    private final String testLastName = "User";


    @BeforeEach
    public void setUp() {
        // Supprime tous les utilisateurs avant chaque test
        userRepository.deleteAll();
    }

    @AfterEach
    public void tearDown() {
        // Supprime tous les utilisateurs après chaque test (nettoyage)
        userRepository.deleteAll();
    }

    @Test
    public void testRegisterUser_Success() throws Exception {
        // Prépare une requête d’inscription
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setEmail(testEmail);
        signupRequest.setPassword(testPassword);
        signupRequest.setFirstName(testFirstName);
        signupRequest.setLastName(testLastName);

        // Effectue une requête POST vers /api/auth/register avec les données JSON
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"" + testEmail + "\",\"password\":\"" + testPassword + "\",\"firstName\":\"" + testFirstName + "\",\"lastName\":\"" + testLastName + "\"}")).andExpect(status().isOk()) // Attend un statut 200 OK
                .andReturn();

        // Vérifie que le message de succès est bien présent
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("User registered successfully!"));

        // Vérifie que l’utilisateur a bien été enregistré dans la base
        User registeredUser = userRepository.findByEmail(testEmail).orElse(null);
        assertNotNull(registeredUser);
        assertEquals(testEmail, registeredUser.getEmail());
        assertEquals(testFirstName, registeredUser.getFirstName());
        assertEquals(testLastName, registeredUser.getLastName());
        assertTrue(passwordEncoder.matches(testPassword, registeredUser.getPassword())); // Vérifie que le mot de passe est bien encodé
    }

    @Test
    public void testRegisterUser_EmailAlreadyTaken() throws Exception {
        // Crée un utilisateur existant dans la base
        User existingUser = new User();
        existingUser.setEmail(testEmail);
        existingUser.setPassword(passwordEncoder.encode(testPassword));
        existingUser.setFirstName(testFirstName);
        existingUser.setLastName(testLastName);
        userRepository.save(existingUser);

        // Tente de s’inscrire avec le même email
        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + testEmail + "\",\"password\":\"" + testPassword + "\",\"firstName\":\"" + testFirstName + "\",\"lastName\":\"" + testLastName + "\"}"))
                .andExpect(status().isBadRequest()) // Doit retourner un statut 400
                .andReturn();

        // Vérifie que le message d’erreur est correct
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("Error: Email is already taken!"));
    }

    @Test
    public void testAuthenticateUser_Success() throws Exception {
        // Crée un utilisateur dans la base
        User user = new User();
        user.setEmail(testEmail);
        user.setPassword(passwordEncoder.encode(testPassword));
        user.setFirstName(testFirstName);
        user.setLastName(testLastName);
        user.setAdmin(false);
        userRepository.save(user);

        // Crée un UserDetailsImpl pour le mock
        UserDetailsImpl userDetails = UserDetailsImpl.builder()
                .id(1L) // ou user.getId() si l'ID est généré
                .username(testEmail)
                .firstName(testFirstName)
                .lastName(testLastName)
                .password(passwordEncoder.encode(testPassword))
                .build();

        // Simule une authentification réussie avec le bon type de principal
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, // principal de type UserDetailsImpl
                null, // credentials
                userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(any())).thenReturn("mocked-jwt-token");

        // Prépare et envoie la requête de login
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + testEmail + "\",\"password\":\"" + testPassword + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        // Vérifie que la réponse contient bien le token et les infos utilisateur
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("mocked-jwt-token"));
        assertTrue(content.contains(testEmail));
        assertTrue(content.contains(testFirstName));
        assertTrue(content.contains(testLastName));
    }

    @Test
    public void testAuthenticateUser_InvalidCredentials() throws Exception {
        // Simule une erreur d'authentification
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // Envoie une requête avec de mauvais identifiants
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"wrong@email.com\",\"password\":\"wrongpassword\"}"))
                .andExpect(status().isUnauthorized()) // Doit retourner 401 Unauthorized
                .andReturn();
    }
}
