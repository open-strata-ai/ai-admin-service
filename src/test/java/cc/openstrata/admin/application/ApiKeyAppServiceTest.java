package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.admin.application.dto.ApiKeyGenerated;
import cc.openstrata.admin.application.dto.CreateApiKeyRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.infrastructure.persistence.ApiKeyEntity;
import cc.openstrata.admin.infrastructure.persistence.ApiKeyJpaRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ApiKeyAppServiceTest {

    @Mock ApiKeyJpaRepository repo;
    @Mock AuditAggregationService audit;
    @InjectMocks ApiKeyAppService service;

    @Test
    void generateReturnsPlaintextAndStoresHash() {
        CreateApiKeyRequest req = new CreateApiKeyRequest("ci", "t1", "a@b.com",
            List.of("read"), null);

        ApiKeyGenerated g = service.create(req);

        assertTrue(g.plaintext().startsWith("sk-"));
        assertEquals(g.plaintext().substring(0, 8), g.key().prefix());
        verify(repo).save(argThat((ApiKeyEntity e) ->
            e.getKeyHash() != null && !e.getKeyHash().equals(g.plaintext())));
    }

    @Test
    void revokeSetsStatus() {
        ApiKeyEntity e = new ApiKeyEntity();
        e.setId("k1");
        e.setName("n");
        e.setPrefix("sk-xxxx");
        e.setKeyHash("h");
        e.setStatus("ACTIVE");
        when(repo.findById("k1")).thenReturn(Optional.of(e));

        service.revoke("k1");

        assertEquals("REVOKED", e.getStatus());
        verify(repo).save(e);
    }

    @Test
    void revokeMissingThrows() {
        when(repo.findById("x")).thenReturn(Optional.empty());
        assertThrows(DomainException.class, () -> service.revoke("x"));
    }
}
