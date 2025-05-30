package com.openclassrooms.starterjwt.integrationController;

import com.openclassrooms.starterjwt.controllers.TeacherController;
import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.mapper.TeacherMapper;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "admin@admin.com", roles = {"ADMIN"})
public class TeacherControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private TeacherMapper teacherMapper;

    @Autowired
    private TeacherController teacherController;

    private Teacher teacher;
    private TeacherDto teacherDto;

    @BeforeEach
    public void setup() {
        // Initialisation manuelle de MockMvc avec le contrôleur ciblé
        mockMvc = MockMvcBuilders.standaloneSetup(teacherController).build();

        // Création d'un enseignant fictif
        teacher = new Teacher();
        teacher.setId(1L);
        teacher.setFirstName("John");
        teacher.setLastName("Doe");

        // Création d'un DTO correspondant à l'enseignant fictif
        teacherDto = new TeacherDto();
        teacherDto.setId(1L);
        teacherDto.setFirstName("John");
        teacherDto.setLastName("Doe");
    }

    @Test
    public void shouldGetTeacherById() throws Exception {
        // Simulation du service et du mapper pour un enseignant trouvé
        when(teacherService.findById(anyLong())).thenReturn(teacher);
        when(teacherMapper.toDto(teacher)).thenReturn(teacherDto);

        // Envoi d'une requête GET pour récupérer un enseignant par son ID
        mockMvc.perform(get("/api/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    public void shouldReturnNotFoundWhenTeacherNotExists() throws Exception {
        // Simulation d'un enseignant introuvable
        when(teacherService.findById(anyLong())).thenReturn(null);

        // Vérifie qu'une requête GET renvoie 404 si l'enseignant n'existe pas
        mockMvc.perform(get("/api/teacher/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnBadRequestWhenInvalidId() throws Exception {
        // Simulation d'une requête GET avec un ID invalide (non numérique)
        mockMvc.perform(get("/api/teacher/invalid")
                        .contentType(MediaType.APPLICATION_JSON))
                // Statut attendu : 400 Bad Request
                .andExpect(status().isBadRequest());
    }

    @Test
    public void shouldGetAllTeachers() throws Exception {
        // Simulation d'un retour de liste d'enseignants
        List<Teacher> teachers = Arrays.asList(teacher);
        List<TeacherDto> teacherDtos = Arrays.asList(teacherDto);

        when(teacherService.findAll()).thenReturn(teachers);
        when(teacherMapper.toDto(teachers)).thenReturn(teacherDtos);

        // Vérifie qu'une requête GET sur tous les enseignants renvoie les bons résultats
        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    // Simulation d’un utilisateur avec un rôle USER
    @WithMockUser(username = "user@user.com", roles = {"USER"})
    public void shouldAllowAccessForUserRole() throws Exception {
        // Vérifie que les utilisateurs non-admin peuvent accéder à la liste des enseignants
        when(teacherService.findAll()).thenReturn(Arrays.asList(teacher));
        when(teacherMapper.toDto(Arrays.asList(teacher))).thenReturn(Arrays.asList(teacherDto));

        mockMvc.perform(get("/api/teacher")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
