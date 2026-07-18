package com.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.openstrata.admin.domain.model.AuditEntry;
import com.openstrata.admin.domain.model.AuditScope;
import com.openstrata.admin.infrastructure.persistence.AuditEntryEntity;
import com.openstrata.admin.infrastructure.persistence.AuditLogRepository;
import com.openstrata.admin.infrastructure.persistence.AuditRecorder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditAggregationServiceTest {

    @Mock AuditLogRepository auditLogRepository;
    @Mock AuditRecorder auditRecorder;
    @InjectMocks AuditAggregationService service;

    @Test
    void recordDelegatesToRecorder() {
        when(auditRecorder.record(any(), any(), any(), any(), any()))
            .thenReturn(AuditEntry.of("a", AuditScope.PLATFORM, "t1", "x", Map.of()));
        assertNotNull(service.record("a", AuditScope.PLATFORM, "t1", "x", Map.of()));
    }

    @Test
    void aggregateCrossServiceCombinesLocal() {
        AuditEntryEntity e = new AuditEntryEntity();
        e.setActor("a");
        e.setScope(AuditScope.PLATFORM);
        e.setTenantId("t1");
        e.setAction("x");
        e.setCreatedAt(java.time.Instant.now());
        when(auditLogRepository.findByTenantId("t1")).thenReturn(List.of(e));
        AuditAggregationService.AggregatedAudit agg = service.aggregateCrossService("t1");
        assertEquals(1, agg.localCount());
        assertEquals("LOCAL_ONLY", agg.status());
    }
}
