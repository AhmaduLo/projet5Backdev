package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.mapper.UserMapper;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;


import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test avant chaque test
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");

        testUserDto = new UserDto();
        testUserDto.setId(1L);
        testUserDto.setEmail("test@test.com");
        testUserDto.setFirstName("Test");
        testUserDto.setLastName("User");
    }

    @Test
    void findById_WhenUserExists_ShouldReturnUserDto() {
        // Arrange (Préparation)
        given(userService.findById(1L)).willReturn(testUser);
        given(userMapper.toDto(testUser)).willReturn(testUserDto);

        // Act (Exécution)
        ResponseEntity<?> response = userController.findById("1");

        // Assert (Vérification)
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(testUserDto);
        verify(userService).findById(1L);
        verify(userMapper).toDto(testUser);
    }

    @Test
    void findById_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        given(userService.findById(anyLong())).willReturn(null);

        // Act
        ResponseEntity<?> response = userController.findById("1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).findById(1L);
        verify(userMapper, never()).toDto((User) any());
    }

    @Test
    void findById_WhenInvalidIdFormat_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<?> response = userController.findById("invalid");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).findById(anyLong());
        verify(userMapper, never()).toDto((User) any());
    }

    @Test
    void delete_WhenUserExistsAndIsOwner_ShouldDeleteAndReturnOk() {
        // Arrange
        given(userService.findById(1L)).willReturn(testUser);

        // Configuration du contexte de sécurité
        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("test@test.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<?> response = userController.save("1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(userService).findById(1L);
        verify(userService).delete(1L);
    }

    @Test
    void delete_WhenUserDoesNotExist_ShouldReturnNotFound() {
        // Arrange
        given(userService.findById(anyLong())).willReturn(null);

        // Act
        ResponseEntity<?> response = userController.save("1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(userService).findById(1L);
        verify(userService, never()).delete(anyLong());
    }

    @Test
    void delete_WhenUserExistsButNotOwner_ShouldReturnUnauthorized() {
        // Arrange
        given(userService.findById(1L)).willReturn(testUser);

        // Configuration du contexte de sécurité avec un autre utilisateur
        UserDetails userDetails = mock(UserDetails.class);
        given(userDetails.getUsername()).willReturn("other@user.com");

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null);
        SecurityContext securityContext = mock(SecurityContext.class);
        given(securityContext.getAuthentication()).willReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Act
        ResponseEntity<?> response = userController.save("1");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(userService).findById(1L);
        verify(userService, never()).delete(anyLong());
    }

    @Test
    void delete_WhenInvalidIdFormat_ShouldReturnBadRequest() {
        // Act
        ResponseEntity<?> response = userController.save("invalid");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).findById(anyLong());
        verify(userService, never()).delete(anyLong());
    }
}