package com.openclassrooms.starterjwt.controllers;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
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
import static org.mockito.Mockito.verify;

// Active l'utilisation de Mockito avec JUnit 5
@ExtendWith(MockitoExtension.class)
public class TeacherControllerTest {

    @Mock
    private TeacherService teacherService;

    @Mock
    private TeacherMapper teacherMapper;

    @InjectMocks
    private TeacherController teacherController;

    private Teacher teacher1;
    private Teacher teacher2;
    private TeacherDto teacherDto1;
    private TeacherDto teacherDto2;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test avant chaque test
        teacher1 = new Teacher();
        teacher1.setId(1L);
        teacher1.setFirstName("John");
        teacher1.setLastName("Doe");

        teacher2 = new Teacher();
        teacher2.setId(2L);
        teacher2.setFirstName("Jane");
        teacher2.setLastName("Smith");

        teacherDto1 = new TeacherDto();
        teacherDto1.setId(1L);
        teacherDto1.setFirstName("John");
        teacherDto1.setLastName("Doe");

        teacherDto2 = new TeacherDto();
        teacherDto2.setId(2L);
        teacherDto2.setFirstName("Jane");
        teacherDto2.setLastName("Smith");
    }

    @Test
    void findById_WhenTeacherExists_ShouldReturnTeacherDto() {
        // Arrange (Préparation) Le service renvoie un enseignant, et le mapper un DTO
        given(teacherService.findById(1L)).willReturn(teacher1);
        given(teacherMapper.toDto(teacher1)).willReturn(teacherDto1);

        // Act : Appel de la méthode du contrôleur
        ResponseEntity<?> response = teacherController.findById("1");

        // Assert : Vérification du code HTTP et du contenu retourné
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(teacherDto1);
        verify(teacherService).findById(1L);
        verify(teacherMapper).toDto(teacher1);
    }

    @Test
    void findById_WhenTeacherDoesNotExist_ShouldReturnNotFound() {
        //Arrange : Le service retourne null pour un ID inexistant
        given(teacherService.findById(anyLong())).willReturn(null);

        // Act
        ResponseEntity<?> response = teacherController.findById("1");

        // Assert : On attend un 404 NOT FOUND
        assertThat(response.getStatusCodeValue()).isEqualTo(404);
        verify(teacherService).findById(1L);
    }

    @Test
    void findById_WhenInvalidIdFormat_ShouldReturnBadRequest() {
        // Act : Appel avec un ID invalide (non numérique)
        ResponseEntity<?> response = teacherController.findById("invalid");

        // Assert : On attend un 400 BAD REQUEST
        assertThat(response.getStatusCodeValue()).isEqualTo(400);
    }

    @Test
    void findAll_WhenTeachersExist_ShouldReturnTeacherDtoList() {
        // Arrange : Le service retourne deux enseignants, le mapper deux DTOs
        List<Teacher> teachers = Arrays.asList(teacher1, teacher2);
        List<TeacherDto> teacherDtos = Arrays.asList(teacherDto1, teacherDto2);

        given(teacherService.findAll()).willReturn(teachers);
        given(teacherMapper.toDto(teachers)).willReturn(teacherDtos);

        // Act : Appel du contrôleur
        ResponseEntity<?> response = teacherController.findAll();

        // Assert : 200 OK + vérification du contenu
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(teacherDtos);
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(teachers);
    }

    @Test
    void findAll_WhenNoTeachers_ShouldReturnEmptyList() {
        // Arrange : Le service retourne une liste vide
        List<Teacher> emptyList = Arrays.asList();
        List<TeacherDto> emptyDtoList = Arrays.asList();

        given(teacherService.findAll()).willReturn(emptyList);
        given(teacherMapper.toDto(emptyList)).willReturn(emptyDtoList);

        // Act
        ResponseEntity<?> response = teacherController.findAll();

        // Assert : 200 OK avec une liste vide
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(emptyDtoList);
        verify(teacherService).findAll();
        verify(teacherMapper).toDto(emptyList);
    }
}
