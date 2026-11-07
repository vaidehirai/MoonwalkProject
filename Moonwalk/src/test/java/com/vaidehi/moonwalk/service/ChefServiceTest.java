package com.vaidehi.moonwalk.service;

import com.vaidehi.moonwalk.entity.Chef;
import com.vaidehi.moonwalk.repository.ChefRepository;
import com.vaidehi.moonwalk.response.APIResponse;
import org.springframework.dao.DataAccessResourceFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChefServiceTest {

    @Mock
    private ChefRepository chefRepository;

    @InjectMocks
    private ChefService chefService;

    @BeforeEach
    void setUp() {
        OrderService.chefQueues = new ArrayList<>();
    }

    @Test
    void insertChef_WhenCalled_SavesChefAndCreatesQueue() {
        Chef chef = new Chef();
        chef.setChefName("Ravi");

        ResponseEntity<APIResponse> response = chefService.insertChef(chef);

        verify(chefRepository).save(chef);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Chef inserted successfully", response.getBody().getMsg());
        assertEquals(1, OrderService.chefQueues.size());
        assertTrue(OrderService.chefQueues.get(0).isEmpty());
    }

    @Test
    void insertChef_WhenRepositoryFails_ReturnsInternalServerErrorWithoutCreatingQueue() {
        Chef chef = new Chef();
        chef.setChefName("Ravi");
        doThrow(new DataAccessResourceFailureException("database unavailable"))
                .when(chefRepository).save(chef);

        ResponseEntity<APIResponse> response = chefService.insertChef(chef);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to insert chef", response.getBody().getMsg());
        assertTrue(OrderService.chefQueues.isEmpty());
    }
}
