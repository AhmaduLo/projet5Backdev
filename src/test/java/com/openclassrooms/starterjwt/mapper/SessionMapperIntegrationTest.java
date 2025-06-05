package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Arrays;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SessionMapperIntegrationTest {

    @Autowired
    private SessionMapper sessionMapper;

    @MockBean
    private TeacherService teacherService;

    @MockBean
    private UserService userService;

    private Teacher teacher;
    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        teacher = new Teacher();
        teacher.setId(1L);

        user1 = new User();
        user1.setId(10L);

        user2 = new User();
        user2.setId(20L);

        // Mock des services
        when(teacherService.findById(1L)).thenReturn(teacher);
        when(userService.findById(10L)).thenReturn(user1);
        when(userService.findById(20L)).thenReturn(user2);
    }

    @Test
    public void testToEntity_shouldMapDtoToEntity() {
        // Création d’un DTO avec id de teacher et liste d’id d’utilisateurs
        SessionDto dto = new SessionDto();
        dto.setDescription("session desc");
        dto.setTeacher_id(1L);
        dto.setUsers(Arrays.asList(10L, 20L));

        // Appel à la méthode toEntity du mapper
        Session entity = sessionMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getDescription()).isEqualTo("session desc");
        assertThat(entity.getTeacher()).isEqualTo(teacher);
        assertThat(entity.getUsers()).containsExactlyInAnyOrder(user1, user2);
    }

    @Test
    public void testToDto_shouldMapEntityToDto() {
        // Création d’une entité Session
        Session session = new Session();
        session.setDescription("session desc");
        session.setTeacher(teacher);
        session.setUsers(Arrays.asList(user1, user2));

        // Conversion vers DTO
        SessionDto dto = sessionMapper.toDto(session);

        assertThat(dto).isNotNull();
        assertThat(dto.getDescription()).isEqualTo("session desc");
        assertThat(dto.getTeacher_id()).isEqualTo(teacher.getId());
        assertThat(dto.getUsers()).containsExactlyInAnyOrder(10L, 20L);
    }
}

