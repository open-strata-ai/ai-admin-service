package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.model.AuditEntry;
import cc.openstrata.admin.domain.model.AuditScope;
import cc.openstrata.admin.domain.model.EntitlementSet;
import cc.openstrata.admin.config.OpenstrataProperties;
import cc.openstrata.admin.infrastructure.persistence.AuditRecorder;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WhitelistManifestServiceTest {

    @Mock OpenstrataProperties props;
    @Mock OpenstrataProperties.Features features;
    @Mock OpenstrataProperties.WhitelistManifest wm;
    @Mock AuditAggregationService audit;
    @InjectMocks WhitelistManifestService service;

    @BeforeEach
    void setup() {
        when(props.getFeatures()).thenReturn(features);
        when(features.getWhitelistManifest()).thenReturn(wm);
        when(audit.record(any(), any(), any(), any(), any()))
            .thenReturn(AuditEntry.of("s", AuditScope.PLATFORM, "t1", "x", Map.of()));
    }

    @Test
    void billingWithoutMultitenancyViolates() {
        assertTrue(service.validate(new EntitlementSet(java.util.Set.of("billing"))).violations()
            .stream().anyMatch(v -> "ENTITLEMENT_DEP_VIOLATION".equals(v.code())));
    }

    @Test
    void rejectStrategyDeniesGuideSelectionBeyondWhitelist() {
        when(wm.getConflictStrategy()).thenReturn("REJECT");
        assertThrows(DomainException.class,
            () -> service.resolveConflict("t1",
                new EntitlementSet(java.util.Set.of("auth")), List.of("billing")));
    }

    @Test
    void escalateStrategyProceeds() {
        when(wm.getConflictStrategy()).thenReturn("ESCALATE");
        WhitelistManifestService.Evaluation e = service.resolveConflict("t1",
            new EntitlementSet(java.util.Set.of("auth")), List.of("billing"));
        assertTrue(e.accepted());
        assertTrue(e.escalated());
    }

    @Test
    void noConflictAccepted() {
        when(wm.getConflictStrategy()).thenReturn("REJECT");
        assertDoesNotThrow(() -> service.resolveConflict("t1",
            new EntitlementSet(java.util.Set.of("auth")), List.of("auth")));
    }
}
