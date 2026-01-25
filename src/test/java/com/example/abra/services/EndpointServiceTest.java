package com.example.abra.services;

import com.example.abra.models.EndpointModel;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EndpointServiceTest {

    @Mock
    private EndpointModelService endpointModelService;

    @InjectMocks
    private EndpointService service;

    @Test
    void selectEndpoint_noEndpoints_returnsNull() {
        when(endpointModelService.findByVariantId("v1")).thenReturn(Collections.emptyList());

        EndpointModel result = service.selectEndpoint("v1");

        assertNull(result);
    }

    @Test
    void selectEndpoint_filtersInactiveOrDead() {
        EndpointModel active = new EndpointModel(); active.setActive(true); active.setAlive(true);
        EndpointModel inactive = new EndpointModel(); inactive.setActive(false); inactive.setAlive(true);
        EndpointModel dead = new EndpointModel(); dead.setActive(true); dead.setAlive(false);

        when(endpointModelService.findByVariantId("v1")).thenReturn(Arrays.asList(active, inactive, dead));

        EndpointModel result = service.selectEndpoint("v1");

        assertNotNull(result);
        assertSame(active, result);
    }

    @Test
    void selectEndpoint_roundRobinSelection() {
        EndpointModel e1 = new EndpointModel(); e1.setActive(true); e1.setAlive(true);
        EndpointModel e2 = new EndpointModel(); e2.setActive(true); e2.setAlive(true);

        when(endpointModelService.findByVariantId("v1")).thenReturn(Arrays.asList(e1, e2));

        // First call -> e1 (index 0)
        EndpointModel r1 = service.selectEndpoint("v1");
        assertSame(e1, r1);

        // Second call -> e2 (index 1)
        EndpointModel r2 = service.selectEndpoint("v1");
        assertSame(e2, r2);

        // Third call -> e1 (index 0 again)
        EndpointModel r3 = service.selectEndpoint("v1");
        assertSame(e1, r3);
    }
}
