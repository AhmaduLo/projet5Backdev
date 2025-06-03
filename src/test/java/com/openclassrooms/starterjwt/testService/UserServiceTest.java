package com.openclassrooms.starterjwt.testService;

import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.repository.UserRepository;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Active le support de Mockito avec JUnit 5
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void testFindById_UserExists() {
        // Arrange – Création d'un utilisateur simulé avec un ID et un email
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");

        // On indique au mock ce qu'il doit retourner quand findById est appelé
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        // Act – Appel de la méthode findById sur le service
        User result = userService.findById(userId);

        // Assert – vérification du résultat
        assertNotNull(result); // On s'attend à un résultat non nul
        assertEquals("test@example.com", result.getEmail()); // Vérifie l'email
        verify(userRepository, times(1)).findById(userId); // Vérifie que findById a été appelé une fois
    }

    @Test
    void testFindById_UserDoesNotExist() {
        // Arrange On indique que le repository ne trouve pas l'utilisateur
        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act  Appel de la méthode
        User result = userService.findById(userId);

        // Assert On s'attend à un résultat null
        assertNull(result); // Si l'utilisateur n'existe pas, on doit obtenir null
        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testDelete_UserIsDeleted() {
        // Arrange ID d'utilisateur à supprimer
        Long userId = 3L;

        // Act  Appel de la méthode delete
        userService.delete(userId);

        // Assert Vérifie que deleteById a bien été appelé une fois avec le bon ID
        verify(userRepository, times(1)).deleteById(userId); // Vérifie que deleteById a été appelé une fois
    }
}
