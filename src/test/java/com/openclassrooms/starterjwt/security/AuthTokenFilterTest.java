package com.openclassrooms.starterjwt.security;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.openclassrooms.starterjwt.security.jwt.AuthTokenFilter;
import com.openclassrooms.starterjwt.security.jwt.JwtUtils;
import com.openclassrooms.starterjwt.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
public class AuthTokenFilterTest {

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private AuthTokenFilter authTokenFilter;

    private final String validToken = "valid.jwt.token";
    private final String username = "testuser";
    private final String invalidToken = "invalid.jwt.token";
    private final String authHeader = "Bearer " + validToken;

    @BeforeEach
    void setUp() {
        // On réinitialise le contexte de sécurité entre chaque test
        SecurityContextHolder.clearContext();
    }

    @Test
    void parseJwt_ShouldReturnNull_WhenNoAuthorizationHeader() {
        // Cas : Aucun header Authorization dans la requête
        when(request.getHeader("Authorization")).thenReturn(null);

        // On s'attend à ce que la méthode parseJwt retourne null
        assertNull(authTokenFilter.parseJwt(request));
    }

    @Test
    void parseJwt_ShouldReturnNull_WhenAuthorizationHeaderDoesNotStartWithBearer() {
        // Cas : Header Authorization mal formé (ne commence pas par "Bearer ")
        when(request.getHeader("Authorization")).thenReturn("Basic credentials");

        // La méthode doit retourner null car ce n'est pas un token Bearer
        assertNull(authTokenFilter.parseJwt(request));
    }

    @Test
    void parseJwt_ShouldReturnToken_WhenValidAuthorizationHeader() {
        // Cas : Header Authorization correct avec "Bearer <token>"
        when(request.getHeader("Authorization")).thenReturn(authHeader);

        // Le token JWT est correctement extrait
        assertEquals(validToken, authTokenFilter.parseJwt(request));
    }

    @Test
    void doFilterInternal_ShouldSetAuthentication_WhenValidToken() throws ServletException, IOException {
        // Cas : Le token JWT est valide, l'utilisateur est correctement authentifié
        // Création d'un utilisateur fictif avec Spring Security
        UserDetails userDetails = User.withUsername(username).password("password").roles("USER").build();


        // Mock du header, validation du token et récupération du nom d'utilisateur
        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(jwtUtils.validateJwtToken(validToken)).thenReturn(true);
        when(jwtUtils.getUserNameFromJwtToken(validToken)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);

        // Exécution du filtre (simulateur de requête)
        authTokenFilter.doFilterInternal(request, response, filterChain);

        // Vérification : le filtre continue bien la chaîne (doFilter est appelé)
        // Vérification : une authentification est bien présente dans le contexte de sécurité
        // Vérifie que le nom d'utilisateur dans le contexte correspond à celui du token
        verify(filterChain).doFilter(request, response);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(username, SecurityContextHolder.getContext().getAuthentication().getName());
    }
}
