package com.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openstrata.admin.domain.DomainException;
import com.openstrata.admin.domain.model.ProvisioningPlan;
import com.openstrata.admin.domain.model.ProvisioningStatus;
import com.openstrata.admin.domain.model.TenantId;
import com.openstrata.admin.infrastructure.persistence.ProvisioningPlanEntity;
import com.openstrata.admin.infrastructure.persistence.ProvisioningPlanRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrchestrationConsensusServiceTest {

    @Mock ProvisioningPlanRepository repository;
    @InjectMocks OrchestrationConsensusService service;

    @Test
    void startCreatesPendingPlan() {
        when(repository.findActiveByTenant("t1")).thenReturn(List.of());
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        ProvisioningPlan p = service.start(new TenantId("t1"), "{}");
        assertEquals(ProvisioningStatus.PENDING, p.status());
        assertTrue(service.canTransition(ProvisioningStatus.PENDING, ProvisioningStatus.APPLYING));
    }

    @Test
    void transitionPendingToApplying() {
        ProvisioningPlanEntity e = entity("p1", ProvisioningStatus.PENDING);
        when(repository.findById("p1")).thenReturn(Optional.of(e));
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(ProvisioningStatus.APPLYING, service.transition("p1", ProvisioningStatus.APPLYING).status());
    }

    @Test
    void illegalTransitionRejected() {
        ProvisioningPlanEntity e = entity("p1", ProvisioningStatus.DONE);
        when(repository.findById("p1")).thenReturn(Optional.of(e));
        assertThrows(DomainException.class,
            () -> service.transition("p1", ProvisioningStatus.APPLYING));
    }

    @Test
    void timedOutApplyingPlanFails() {
        ProvisioningPlanEntity e = entity("p1", ProvisioningStatus.APPLYING);
        e.setCreatedAt(Instant.now().minus(java.time.Duration.ofHours(1)));
        when(repository.findAll()).thenReturn(List.of(e));
        when(repository.saveAll(any())).thenAnswer(i -> i.getArgument(0));
        assertEquals(1, service.failTimedOut(Instant.now()));
        assertEquals(ProvisioningStatus.FAILED, e.getStatus());
    }

    @Test
    void terminalCannotTransition() {
        assertFalse(service.canTransition(ProvisioningStatus.DONE, ProvisioningStatus.APPLYING));
        assertFalse(service.canTransition(ProvisioningStatus.FAILED, ProvisioningStatus.DONE));
    }

    private ProvisioningPlanEntity entity(String id, ProvisioningStatus status) {
        ProvisioningPlanEntity e = new ProvisioningPlanEntity();
        e.setPlanId(id);
        e.setTenantId("t1");
        e.setManifest("{}");
        e.setStatus(status);
        e.setCreatedAt(Instant.now());
        return e;
    }
}
