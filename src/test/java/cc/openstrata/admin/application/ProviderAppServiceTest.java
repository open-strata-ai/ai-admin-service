package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.admin.application.dto.ProviderView;
import cc.openstrata.admin.application.dto.UpsertProviderRequest;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.domain.port.SecretStorePort;
import cc.openstrata.admin.infrastructure.persistence.ProviderEntity;
import cc.openstrata.admin.infrastructure.persistence.ProviderJpaRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProviderAppServiceTest {

    @Mock ProviderJpaRepository repo;
    @Mock SecretStorePort secrets;
    @Mock AuditAggregationService audit;
    @InjectMocks ProviderAppService service;

    @Test
    void createPersistsAndReturnsView() {
        UpsertProviderRequest req = new UpsertProviderRequest("OpenAI", "openai",
            "https://api.openai.com", "api_key", "ACTIVE", Map.of("region", "us"));

        ProviderView v = service.create(req);

        assertEquals("openai", v.type());
        assertFalse(v.hasSecret());
        verify(repo).save(any());
        verify(audit).record(any(), any(), any(), eq("PROVIDER_CREATED"), any());
    }

    @Test
    void setSecretStoresRefAndNeverInline() {
        ProviderEntity e = new ProviderEntity();
        e.setId("p1");
        e.setName("n");
        e.setType("t");
        when(repo.findById("p1")).thenReturn(Optional.of(e));
        when(secrets.store("p1", "sec")).thenReturn("ref/p1");

        service.setSecret("p1", "sec");

        assertEquals("ref/p1", e.getSecretRef());
        verify(repo).save(e);
        verify(secrets).store("p1", "sec");
    }

    @Test
    void deleteCleansSecretRef() {
        ProviderEntity e = new ProviderEntity();
        e.setId("p1");
        e.setSecretRef("ref");
        when(repo.findById("p1")).thenReturn(Optional.of(e));

        service.delete("p1");

        verify(secrets).delete("ref");
        verify(repo).deleteById("p1");
    }

    @Test
    void getMissingThrows() {
        when(repo.findById("x")).thenReturn(Optional.empty());
        assertThrows(DomainException.class, () -> service.get("x"));
    }
}
