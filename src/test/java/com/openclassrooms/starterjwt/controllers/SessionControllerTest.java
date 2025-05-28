package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.mapper.SessionMapper;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

// Permet l'utilisation de Mockito avec JUnit 5
@ExtendWith(MockitoExtension.class)
class SessionControllerTest {

    @Mock
    private SessionService sessionService;// Service mocké

    @Mock
    private SessionMapper sessionMapper;// Mapper mocké

    @InjectMocks
    private SessionController sessionController;// Contrôleur testé avec mocks injectés

    private Session session;
    private SessionDto sessionDto;

    @BeforeEach
    void setUp() {
        // Création d'une session fictive pour les tests
        session = new Session();
        session.setId(1L);
        session.setName("Test Session");
        session.setDescription("Test Description");

        sessionDto = new SessionDto();
        sessionDto.setId(1L);
        sessionDto.setName("Test Session");
        sessionDto.setDescription("Test Description");
    }

    // ---------- Tests du endpoint findById ----------
    @Test
    void findById_WhenSessionExists_ShouldReturnSessionDto() {
        // Simule un retour valide du service et du mapper
        given(sessionService.getById(1L)).willReturn(session);
        given(sessionMapper.toDto(session)).willReturn(sessionDto);

        // Appel du contrôleur
        ResponseEntity<?> response = sessionController.findById("1");

        // Vérifie que la réponse est correcte
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(sessionDto);
    }

    @Test
    void findById_WhenSessionNotExists_ShouldReturnNotFound() {
        // Simule une session non trouvée
        given(sessionService.getById(anyLong())).willReturn(null);

        ResponseEntity<?> response = sessionController.findById("1");
        // Vérifie que la réponse est 404
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
    }

    @Test
    void findById_WhenInvalidId_ShouldReturnBadRequest() {
        // Teste un identifiant invalide (non numérique)
        ResponseEntity<?> response = sessionController.findById("invalid");

        // Vérifie que la réponse est 400
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ---------- Tests du endpoint findAll ----------
    @Test
    void findAll_WhenSessionsExist_ShouldReturnSessionDtoList() {
        // Simule une liste de sessions
        List<Session> sessions = Arrays.asList(session);
        List<SessionDto> sessionDtos = Arrays.asList(sessionDto);

        given(sessionService.findAll()).willReturn(sessions);
        given(sessionMapper.toDto(sessions)).willReturn(sessionDtos);

        ResponseEntity<?> response = sessionController.findAll();

        // Vérifie que la réponse contient bien la liste
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(sessionDtos);
    }

    // ---------- Tests du endpoint create ----------
    @Test
    void create_WithValidSessionDto_ShouldReturnCreatedSession() {
        given(sessionMapper.toEntity(sessionDto)).willReturn(session);
        given(sessionService.create(session)).willReturn(session);
        given(sessionMapper.toDto(session)).willReturn(sessionDto);

        ResponseEntity<?> response = sessionController.create(sessionDto);

        // Vérifie que la session est bien créée
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(sessionDto);
    }

    // ---------- Tests du endpoint update ----------
    @Test
    void update_WithValidIdAndSessionDto_ShouldReturnUpdatedSession() {
        // Simule le processus de mise à jour
        given(sessionMapper.toEntity(sessionDto)).willReturn(session);
        given(sessionService.update(1L, session)).willReturn(session);
        given(sessionMapper.toDto(session)).willReturn(sessionDto);

        ResponseEntity<?> response = sessionController.update("1", sessionDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(sessionDto);
    }

    @Test
    void update_WithInvalidId_ShouldReturnBadRequest() {
        // Teste un identifiant invalide lors d'une mise à jour
        ResponseEntity<?> response = sessionController.update("invalid", sessionDto);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ---------- Tests du endpoint save (delete logique) ----------
    @Test
    void delete_WhenSessionExists_ShouldReturnOk() {
        // Simule une session existante à supprimer
        given(sessionService.getById(1L)).willReturn(session);

        ResponseEntity<?> response = sessionController.save("1");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(sessionService).delete(1L);
    }

    @Test
    void delete_WhenSessionNotExists_ShouldReturnNotFound() {
        // Simule l'absence de session
        given(sessionService.getById(anyLong())).willReturn(null);

        ResponseEntity<?> response = sessionController.save("1");

        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        verify(sessionService, never()).delete(anyLong());
    }

    @Test
    void delete_WithInvalidId_ShouldReturnBadRequest() {
        ResponseEntity<?> response = sessionController.save("invalid");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ---------- Tests du endpoint participate ----------
    @Test
    void participate_WithValidIds_ShouldReturnOk() {
        // Teste la participation d'un utilisateur à une session
        ResponseEntity<?> response = sessionController.participate("1", "2");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(sessionService).participate(1L, 2L);
    }

    @Test
    void participate_WithInvalidIds_ShouldReturnBadRequest() {
        ResponseEntity<?> response = sessionController.participate("invalid", "2");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    // ---------- Tests du endpoint noLongerParticipate ----------
    @Test
    void noLongerParticipate_WithValidIds_ShouldReturnOk() {
        // Teste la désinscription d'un utilisateur
        ResponseEntity<?> response = sessionController.noLongerParticipate("1", "2");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(sessionService).noLongerParticipate(1L, 2L);
    }

    @Test
    void noLongerParticipate_WithInvalidIds_ShouldReturnBadRequest() {
        // Teste avec un ID utilisateur invalide
        ResponseEntity<?> response = sessionController.noLongerParticipate("1", "invalid");

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }
}