package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditEntry;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.domain.model.PackageTier;
import cc.openstrata.admin.domain.model.TenantGovernance;
import cc.openstrata.admin.domain.model.TenantId;
import cc.openstrata.admin.domain.port.ControlPlaneClient;
import cc.openstrata.admin.infrastructure.persistence.AuditRecorder;
import cc.openstrata.admin.infrastructure.persistence.TenantGovernanceRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GovernanceAuthorityServiceTest {

    @Mock ControlPlaneClient client;
    @Mock TenantGovernanceRepository repository;
    @Mock AuditRecorder auditRecorder;
    @InjectMocks GovernanceAuthorityService service;

    @Test
    void createTenantWritesDomainAuthorityAndMirrors() {
        when(auditRecorder.record(any(), any(), any(), any(), any()))
            .thenReturn(AuditEntry.of("a", AuditScope.PLATFORM, "t1", "x", Map.of()));
        TenantGovernance g = service.createTenant(new TenantId("t1"), PackageTier.STANDARD);
        verify(client).createTenant(new TenantId("t1"), PackageTier.STANDARD);
        verify(repository).save(any());
        assertEquals(PackageTier.STANDARD, g.packageTier());
    }

    @Test
    void loadMissingTenantThrows() {
        when(repository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(DomainException.class, () -> service.load(new TenantId("missing")));
    }
}
