package cc.openstrata.admin.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.admin.domain.model.PackageTemplate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JpaPackageTemplateRepositoryTest {

    private PackageTemplateJpaRepository jpa;
    private JpaPackageTemplateRepository repo;

    @BeforeEach
    void setUp() {
        jpa = mock(PackageTemplateJpaRepository.class);
        repo = new JpaPackageTemplateRepository(jpa);
    }

    @Test
    void saveSerializesComponentsAsJson() {
        PackageTemplate t = new PackageTemplate("pt-starter", "Starter", "basic",
            List.of("chat", "memory"), "2 vCPU");

        repo.save(t);

        verify(jpa).save(argThat(e ->
            "pt-starter".equals(e.getId())
                && "Starter".equals(e.getName())
                && "basic".equals(e.getTier())
                && e.getComponents().contains("chat")
                && e.getComponents().contains("memory")
                && "2 vCPU".equals(e.getQuotaPolicy())));
    }

    @Test
    void findByIdDeserializesComponents() {
        PackageTemplateEntity e = new PackageTemplateEntity();
        e.setId("pt-pro");
        e.setName("Pro");
        e.setTier("advanced");
        e.setComponents("[\"chat\",\"eval\"]");
        e.setQuotaPolicy("8 vCPU");

        when(jpa.findById("pt-pro")).thenReturn(Optional.of(e));

        Optional<PackageTemplate> result = repo.findById("pt-pro");
        assertTrue(result.isPresent());
        assertEquals("Pro", result.get().getName());
        assertEquals(List.of("chat", "eval"), result.get().getComponents());
        assertEquals("8 vCPU", result.get().getQuotaPolicy());
    }

    @Test
    void findByIdEmptyWhenMissing() {
        when(jpa.findById("pt-missing")).thenReturn(Optional.empty());
        assertTrue(repo.findById("pt-missing").isEmpty());
    }

    @Test
    void findAllMapsAllRows() {
        PackageTemplateEntity e = new PackageTemplateEntity();
        e.setId("pt-1");
        e.setName("One");
        e.setTier("basic");
        e.setComponents("[]");
        when(jpa.findAll()).thenReturn(List.of(e));

        List<PackageTemplate> all = repo.findAll();
        assertEquals(1, all.size());
        assertEquals("pt-1", all.get(0).getId());
        assertTrue(all.get(0).getComponents().isEmpty());
    }

    @Test
    void deleteDelegatesToJpa() {
        repo.delete("pt-1");
        verify(jpa).deleteById("pt-1");
    }
}
