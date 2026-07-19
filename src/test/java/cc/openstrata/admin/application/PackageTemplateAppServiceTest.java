package cc.openstrata.admin.application;

import cc.openstrata.admin.application.dto.CreatePackageTemplateRequest;
import cc.openstrata.admin.application.dto.PackageTemplateResponse;
import cc.openstrata.admin.domain.model.PackageTemplate;
import cc.openstrata.admin.infrastructure.persistence.InMemoryPackageTemplateRepository;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackageTemplateAppServiceTest {
    private final PackageTemplateAppService svc =
        new PackageTemplateAppService(new InMemoryPackageTemplateRepository());

    @Test
    void createThenList() {
        PackageTemplate t = svc.create(new CreatePackageTemplateRequest("Starter", "basic",
            List.of("chat", "memory"), "2 vCPU"));
        assertTrue(t.getId().startsWith("pt-"));
        assertEquals(1, svc.list().size());
        assertEquals("Starter", svc.get(t.getId()).getName());
    }

    @Test
    void deleteRemovesTemplate() {
        PackageTemplate t = svc.create(new CreatePackageTemplateRequest("Pro", "advanced",
            List.of("chat"), "8 vCPU"));
        svc.delete(t.getId());
        assertEquals(0, svc.list().size());
    }

    @Test
    void getMissingThrows() {
        assertThrows(IllegalArgumentException.class, () -> svc.get("pt-missing"));
    }
}
