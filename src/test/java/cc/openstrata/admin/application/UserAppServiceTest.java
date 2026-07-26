package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.admin.application.dto.UpsertUserRequest;
import cc.openstrata.admin.application.dto.UserView;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserEntity;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAppServiceTest {

    @Mock PlatformUserJpaRepository repo;
    @Mock AuditAggregationService audit;
    @InjectMocks UserAppService service;

    @BeforeEach
    void setUp() {
        // Production always establishes a tenant context via AuthInterceptor.
        TenantContext.set(new TenantContext.Tenant("local", Set.of("platform-admin"), true));
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void viewerRoleAllowed() {
        UpsertUserRequest req = new UpsertUserRequest("a@b.com", "A", "t1",
            List.of("viewer"), "ACTIVE");

        UserView v = service.create(req);

        assertEquals(List.of("viewer"), v.roles());
        verify(repo).save(any());
    }

    @Test
    void unknownRoleRejected() {
        UpsertUserRequest req = new UpsertUserRequest("a@b.com", "A", "t1",
            List.of("superuser"), "ACTIVE");

        assertThrows(DomainException.class, () -> service.create(req));
    }

    @Test
    void deleteDelegates() {
        PlatformUserEntity e = new PlatformUserEntity();
        e.setId("u1");
        e.setEmail("a@b.com");
        when(repo.findById("u1")).thenReturn(Optional.of(e));

        service.delete("u1");

        verify(repo).deleteById("u1");
    }

    @Test
    void listReturnsAllForPlatformAdmin() {
        PlatformUserEntity a = new PlatformUserEntity();
        a.setId("u1");
        a.setTenantId("t1");
        PlatformUserEntity b = new PlatformUserEntity();
        b.setId("u2");
        b.setTenantId("t2");
        when(repo.findAll()).thenReturn(List.of(a, b));

        List<UserView> views = service.list();

        assertEquals(2, views.size());
        verify(repo).findAll();
    }

    @Test
    void listScopedToTenantFiltersCrossTenantRows() {
        TenantContext.set(new TenantContext.Tenant("t2", Set.of("tenant-admin"), false));
        PlatformUserEntity mine = new PlatformUserEntity();
        mine.setId("u2");
        mine.setTenantId("t2");
        PlatformUserEntity other = new PlatformUserEntity();
        other.setId("u1");
        other.setTenantId("t1");
        when(repo.findByTenantId("t2")).thenReturn(List.of(mine));

        List<UserView> views = service.list();

        assertEquals(1, views.size());
        assertEquals("u2", views.get(0).id());
        verify(repo).findByTenantId("t2");
    }

    @Test
    void getCrossTenantThrowsForbidden() {
        TenantContext.set(new TenantContext.Tenant("t2", Set.of("tenant-admin"), false));
        PlatformUserEntity e = new PlatformUserEntity();
        e.setId("u1");
        e.setTenantId("t1");
        when(repo.findById("u1")).thenReturn(Optional.of(e));

        assertThrows(DomainException.class, () -> service.get("u1"));
    }
}
