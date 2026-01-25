package com.example.abra.services;

import com.example.abra.models.UserModel;
import com.example.abra.repositories.UserModelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserModelServiceTest {

    @Mock
    private UserModelRepository repository;

    @InjectMocks
    private UserModelService service;

    @Test
    void loadUserByUsername_found_returnsUserDetails() {
        UserModel user = UserModel.builder()
                .login("admin")
                .password("encodedPass")
                .build();

        when(repository.findByLogin("admin")).thenReturn(Optional.of(user));

        UserDetails result = service.loadUserByUsername("admin");

        assertNotNull(result);
        assertEquals("admin", result.getUsername());
        assertEquals("encodedPass", result.getPassword());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_notFound_throwsException() {
        when(repository.findByLogin("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> 
            service.loadUserByUsername("unknown"));
    }
}
