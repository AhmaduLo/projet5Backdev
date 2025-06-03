package com.openclassrooms.starterjwt.testService;

import com.openclassrooms.starterjwt.exception.BadRequestException;
import com.openclassrooms.starterjwt.exception.NotFoundException;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.SessionRepository;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.SessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionServiceTest {

    @Mock
    private SessionRepository sessionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SessionService sessionService;


    @BeforeEach
    void setUp() {
        // On redéfinit les mocks manuellement (utile si le test est modifié dynamiquement)
        sessionRepository = mock(SessionRepository.class);
        userRepository = mock(UserRepository.class);
        sessionService = new SessionService(sessionRepository, userRepository);
    }

    // Test création d'une session
    @Test
    void testCreateSession() {
        Session session = new Session();
        when(sessionRepository.save(session)).thenReturn(session);

        Session created = sessionService.create(session);

        verify(sessionRepository).save(session);
        assertEquals(session, created);
    }

    // Test suppression d'une session
    @Test
    void testDeleteSession() {
        Long id = 1L;
        doNothing().when(sessionRepository).deleteById(id);

        sessionService.delete(id);

        verify(sessionRepository).deleteById(id);
    }


    // Test récupération de toutes les sessions
    @Test
    void testFindAllSessions() {
        List<Session> sessions = new ArrayList<>();
        sessions.add(new Session());
        when(sessionRepository.findAll()).thenReturn(sessions);

        List<Session> result = sessionService.findAll();

        verify(sessionRepository).findAll();
        assertEquals(sessions, result);
    }

    // Test récupération d'une session par ID - cas trouvée
    @Test
    void testGetByIdFound() {
        Session session = new Session();
        Long id = 1L;
        when(sessionRepository.findById(id)).thenReturn(Optional.of(session));

        Session result = sessionService.getById(id);

        verify(sessionRepository).findById(id);
        assertEquals(session, result);
    }

    // Test récupération d'une session par ID - cas non trouvée
    @Test
    void testGetByIdNotFound() {
        Long id = 1L;
        when(sessionRepository.findById(id)).thenReturn(Optional.empty());

        Session result = sessionService.getById(id);

        verify(sessionRepository).findById(id);
        assertNull(result);
    }

    // Test mise à jour d'une session
    @Test
    void testUpdateSession() {
        Long id = 1L;
        Session sessionToUpdate = new Session();
        sessionToUpdate.setId(99L); // id différent avant update
        Session savedSession = new Session();
        when(sessionRepository.save(any(Session.class))).thenReturn(savedSession);

        Session result = sessionService.update(id, sessionToUpdate);

        // Vérifie que l'id de sessionToUpdate est bien modifié
        assertEquals(id, sessionToUpdate.getId());
        verify(sessionRepository).save(sessionToUpdate);
        assertEquals(savedSession, result);
    }

    // Test participation d'un utilisateur à une session - cas normal
    @Test
    void testParticipateSuccess() {
        Long sessionId = 1L;
        Long userId = 10L;

        Session session = new Session();
        session.setUsers(new ArrayList<>());
        User user = new User();
        user.setId(userId);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        sessionService.participate(sessionId, userId);

        verify(sessionRepository).findById(sessionId);
        verify(userRepository).findById(userId);
        // Vérifie que l'utilisateur a bien été ajouté
        assertTrue(session.getUsers().contains(user));
        verify(sessionRepository).save(session);
    }

    // Test participation - session ou utilisateur introuvable -> NotFoundException
    @Test
    void testParticipateNotFoundException() {
        Long sessionId = 1L;
        Long userId = 10L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sessionService.participate(sessionId, userId));
    }

    // Test participation - utilisateur déjà présent -> BadRequestException
    @Test
    void testParticipateAlreadyParticipate() {
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        Session session = new Session();
        List<User> users = new ArrayList<>();
        users.add(user);
        session.setUsers(users);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class, () -> sessionService.participate(sessionId, userId));
    }

    // Test annulation de participation - cas normal
    @Test
    void testNoLongerParticipateSuccess() {
        Long sessionId = 1L;
        Long userId = 10L;

        User user = new User();
        user.setId(userId);

        List<User> users = new ArrayList<>();
        users.add(user);

        Session session = new Session();
        session.setUsers(users);

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(Session.class))).thenReturn(session);

        sessionService.noLongerParticipate(sessionId, userId);

        verify(sessionRepository).findById(sessionId);
        // Vérifie que l'utilisateur a été retiré
        assertFalse(session.getUsers().stream().anyMatch(u -> u.getId().equals(userId)));
        verify(sessionRepository).save(session);
    }

    // Test annulation de participation - session non trouvée -> NotFoundException
    @Test
    void testNoLongerParticipateNotFoundException() {
        Long sessionId = 1L;
        Long userId = 10L;

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> sessionService.noLongerParticipate(sessionId, userId));
    }


    // Test annulation de participation - utilisateur non participant -> BadRequestException
    @Test
    void testNoLongerParticipateBadRequestException() {
        Long sessionId = 1L;
        Long userId = 10L;

        Session session = new Session();
        session.setUsers(new ArrayList<>()); // pas d'utilisateur

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThrows(BadRequestException.class, () -> sessionService.noLongerParticipate(sessionId, userId));
    }
}
