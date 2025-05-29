package com.openclassrooms.starterjwt.integrationController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclassrooms.starterjwt.controllers.SessionController;
import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin@admin.com", roles = {"ADMIN"}) // Simule un utilisateur admin connecté
public class SessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SessionService sessionService;

    @MockBean
    private SessionMapper sessionMapper;

    @Autowired
    private SessionController sessionController;

    @Autowired
    private ObjectMapper objectMapper;

    private Session session;
    private SessionDto sessionDto;
    private final Date fixedDate = new Date(1700000000000L); // Date fixe pour les tests

    @BeforeEach
    public void setup() {
        // Configure MockMvc avec le contrôleur réel
        mockMvc = MockMvcBuilders.standaloneSetup(sessionController).build();

        // Initialisation d’une session fictive
        session = new Session();
        session.setId(1L);
        session.setName("Test Session");
        session.setDescription("Test Description");
        session.setDate(new Date());

        // DTO correspondant à la session fictive
        sessionDto = new SessionDto();
        sessionDto.setId(1L);
        sessionDto.setName("Test Session");
        sessionDto.setDescription("Test Description");
        sessionDto.setDate(new Date());
    }


    // Test : récupération d'une session existante par son ID
    @Test
    public void shouldGetSessionById() throws Exception {
        when(sessionService.getById(anyLong())).thenReturn(session);
        when(sessionMapper.toDto(any(Session.class))).thenReturn(sessionDto);

        mockMvc.perform(get("/api/session/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))// Vérifie l'ID
                .andExpect(jsonPath("$.name").value("Test Session"));// Vérifie le nom

        verify(sessionService, times(1)).getById(1L);
        verify(sessionMapper, times(1)).toDto(session);
    }

    // Test : session inexistante => doit retourner 404
    @Test
    public void shouldReturnNotFoundWhenSessionNotExists() throws Exception {
        when(sessionService.getById(anyLong())).thenReturn(null);

        mockMvc.perform(get("/api/session/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(sessionService, times(1)).getById(1L);
    }

    // Test : ID invalide (ex. chaîne non convertible en Long)
    @Test
    public void shouldReturnBadRequestWhenInvalidId() throws Exception {
        mockMvc.perform(get("/api/session/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // Test : récupération de toutes les sessions
    @Test
    public void shouldGetAllSessions() throws Exception {
        List<Session> sessions = Arrays.asList(session);
        List<SessionDto> sessionDtos = Arrays.asList(sessionDto);

        when(sessionService.findAll()).thenReturn(sessions);
        when(sessionMapper.toDto(sessions)).thenReturn(sessionDtos);

        mockMvc.perform(get("/api/session")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Test Session"));

        verify(sessionService, times(1)).findAll();
        verify(sessionMapper, times(1)).toDto(sessions);
    }

    // Test : création d'une nouvelle session
    @Test
    public void shouldCreateSession() throws Exception {
        // Données en entrée simulées
        SessionDto inputDto = new SessionDto();
        inputDto.setName("Test Session");
        inputDto.setDescription("Test Description");
        inputDto.setDate(new Date()); // Champ requis
        inputDto.setTeacher_id(1L);   // Champ requis

        // Session attendue après mapping et sauvegarde
        Session expectedSession = new Session();
        expectedSession.setId(1L);
        expectedSession.setName(inputDto.getName());
        expectedSession.setDescription(inputDto.getDescription());

        // DTO de sortie après création
        SessionDto expectedDto = new SessionDto();
        expectedDto.setId(1L);
        expectedDto.setName(inputDto.getName());
        expectedDto.setDescription(inputDto.getDescription());
        expectedDto.setDate(inputDto.getDate());
        expectedDto.setTeacher_id(inputDto.getTeacher_id());

        when(sessionMapper.toEntity(any(SessionDto.class))).thenReturn(expectedSession);
        when(sessionService.create(any(Session.class))).thenReturn(expectedSession);
        when(sessionMapper.toDto(any(Session.class))).thenReturn(expectedDto);

        // Exécution et vérification
        mockMvc.perform(post("/api/session")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(inputDto.getName()))
                .andExpect(jsonPath("$.description").value(inputDto.getDescription()))
                .andExpect(jsonPath("$.teacher_id").value(inputDto.getTeacher_id()));
    }

    // Test : mise à jour d'une session
    @Test
    public void shouldUpdateSession() throws Exception {
        // Préparer les données de test COMPLÈTES
        SessionDto updateDto = new SessionDto();
        updateDto.setName("Updated Session");
        updateDto.setDescription("Updated Description");
        updateDto.setDate(new Date()); // Champ requis
        updateDto.setTeacher_id(1L);   // Champ requis

        //Configurer les mocks
        Session updatedSession = new Session();
        updatedSession.setId(1L);
        updatedSession.setName(updateDto.getName());
        updatedSession.setDescription(updateDto.getDescription());

        SessionDto updatedDto = new SessionDto();
        updatedDto.setId(1L);
        updatedDto.setName(updateDto.getName());
        updatedDto.setDescription(updateDto.getDescription());
        updatedDto.setDate(updateDto.getDate());
        updatedDto.setTeacher_id(updateDto.getTeacher_id());

        when(sessionMapper.toEntity(any(SessionDto.class))).thenReturn(updatedSession);
        when(sessionService.update(anyLong(), any(Session.class))).thenReturn(updatedSession);
        when(sessionMapper.toDto(any(Session.class))).thenReturn(updatedDto);

        // Exécuter et vérifier
        mockMvc.perform(put("/api/session/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Updated Session")) // Vérifier la nouvelle valeur
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.teacher_id").value(1L));

        verify(sessionService, times(1)).update(1L, updatedSession);
        verify(sessionMapper, times(1)).toDto(updatedSession);
    }

    // Test : suppression d'une session inexistante
    @Test
    public void shouldReturnNotFoundWhenDeleteNonExistingSession() throws Exception {
        when(sessionService.getById(anyLong())).thenReturn(null);

        mockMvc.perform(delete("/api/session/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(sessionService, never()).delete(anyLong());
    }

    // Test : participation à une session
    @Test
    public void shouldParticipateToSession() throws Exception {
        doNothing().when(sessionService).participate(anyLong(), anyLong());

        mockMvc.perform(post("/api/session/1/participate/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).participate(1L, 2L);
    }

    // Test : annulation de la participation à une session
    @Test
    public void shouldNoLongerParticipateToSession() throws Exception {
        doNothing().when(sessionService).noLongerParticipate(anyLong(), anyLong());

        mockMvc.perform(delete("/api/session/1/participate/2")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(sessionService, times(1)).noLongerParticipate(1L, 2L);
    }

}
