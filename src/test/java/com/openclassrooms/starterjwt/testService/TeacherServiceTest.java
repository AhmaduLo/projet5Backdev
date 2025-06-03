package com.openclassrooms.starterjwt.testService;


import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import com.openclassrooms.starterjwt.services.TeacherService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TeacherServiceTest {

    @Mock
    private TeacherRepository teacherRepository;

    @InjectMocks
    private TeacherService teacherService;

    @Test
    void testFindAll_ReturnsListOfTeachers() {
        // Arrange – créer des profs fictifs
        Teacher teacher1 = new Teacher();
        teacher1.setId(1L);
        teacher1.setLastName("Dupont");

        Teacher teacher2 = new Teacher();
        teacher2.setId(2L);
        teacher2.setLastName("Martin");

        when(teacherRepository.findAll()).thenReturn(Arrays.asList(teacher1, teacher2));

        // Act – appel de la méthode à tester
        List<Teacher> result = teacherService.findAll();

        // Assert – vérifie le résultat
        assertEquals(2, result.size()); // On attend 2 profs
        assertEquals("Martin", result.get(1).getLastName()); // Vérifie le nom du deuxième
        verify(teacherRepository, times(1)).findAll(); // Vérifie l'appel
    }

    @Test
    void testFindById_TeacherExists() {
        // Arrange
        Long teacherId = 1L;
        Teacher teacher = new Teacher();
        teacher.setId(teacherId);
        teacher.setLastName("Durand");

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.of(teacher));

        // Act
        Teacher result = teacherService.findById(teacherId);

        // Assert
        assertNotNull(result);
        assertEquals("Durand", result.getLastName());
        verify(teacherRepository, times(1)).findById(teacherId);
    }

    @Test
    void testFindById_TeacherDoesNotExist() {
        // Arrange
        Long teacherId = 99L;

        when(teacherRepository.findById(teacherId)).thenReturn(Optional.empty());

        // Act
        Teacher result = teacherService.findById(teacherId);

        // Assert
        assertNull(result); // Aucun prof trouvé
        verify(teacherRepository, times(1)).findById(teacherId);
    }
}
