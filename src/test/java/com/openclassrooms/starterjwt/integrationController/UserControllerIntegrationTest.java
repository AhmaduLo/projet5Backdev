package com.openclassrooms.starterjwt.integrationController;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    private final Long userId = 1L; // ID de test
    private final String userEmail = "test@test.com"; // Email de test
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    public void setUp() {
        // Initialisation de l'utilisateur
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail(userEmail);
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");

        // Initialisation du DTO
        testUserDto = new UserDto();
        testUserDto.setId(userId);
        testUserDto.setEmail(userEmail);
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
    }

    @Test
    public void testFindById_Success() throws Exception {
        // Mock du service et du mapper pour renvoyer un utilisateur
        when(userService.findById(userId)).thenReturn(testUser);
        when(userMapper.toDto(testUser)).thenReturn(testUserDto);

        // Simule un utilisateur authentifié
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userEmail);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Exécution de la requête GET
        MvcResult result = mockMvc.perform(get("/api/user/{id}", userId).header("Authorization", "Bearer mocked-jwt-token")).andExpect(status().isOk()).andReturn();

    }

    @Test
    public void testFindById_NotFound() throws Exception {
        // Simule une recherche d'utilisateur inexistant
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("any@email.com");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, null));

        // Contexte de sécurité simulé
        when(userService.findById(userId)).thenReturn(null); // Simuler un utilisateur non trouvé

        // Envoie une requête GET qui devrait échouer avec un 404
        mockMvc.perform(get("/api/user/{id}", userId).header("Authorization", "Bearer mocked-token")).andExpect(status().isNotFound());
    }

    @Test
    public void testFindById_InvalidId() throws Exception {
        // Simule un contexte de sécurité
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("any@email.com");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, null));

        // Essai de requête GET avec un ID invalide (non numérique)
        mockMvc.perform(get("/api/user/{id}", "not-a-number").header("Authorization", "Bearer mocked-token")).andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete_Success() throws Exception {
        // Simulation d'un utilisateur existant
        when(userService.findById(userId)).thenReturn(testUser);
        doNothing().when(userService).delete(userId);

        // Utilisateur correctement authentifié
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(userEmail);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Requête DELETE réussie
        mockMvc.perform(delete("/api/user/{id}", userId)).andExpect(status().isOk());

        // Vérifie que le service a bien été invoqué une fois
        verify(userService, times(1)).delete(userId);
    }

    @Test
    public void testDelete_NotFound() throws Exception {
        // Simule un utilisateur non trouvé
        when(userService.findById(userId)).thenReturn(null); // Simuler un utilisateur non trouvé

        // Contexte d'un utilisateur authentifié
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("any@email.com"); // Email quelconque
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, null));

        // Requête DELETE échouée avec 404
        mockMvc.perform(delete("/api/user/{id}", userId).header("Authorization", "Bearer mocked-token")) // Token mocké
                .andExpect(status().isNotFound()); // Vérifier le statut 404
    }

    @Test
    public void testDelete_Unauthorized() throws Exception {
        // Simule que l'utilisateur existe
        when(userService.findById(userId)).thenReturn(testUser);

        // Authentification d’un utilisateur différent
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("other@email.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Doit retourner 401 car utilisateur non autorisé
        mockMvc.perform(delete("/api/user/{id}", userId)).andExpect(status().isUnauthorized());
    }

    @Test
    public void testDelete_InvalidId() throws Exception {
        // Utilisateur fictif authentifié
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("any@email.com");
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, null));

        // Requête DELETE avec un ID invalide (chaîne non numérique)
        mockMvc.perform(delete("/api/user/{id}", "invalid").header("Authorization", "Bearer mocked-token")) // Header d'authentification
                .andExpect(status().isBadRequest()); // Doit retourner 400
    }
}