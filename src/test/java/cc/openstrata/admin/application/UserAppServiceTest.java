package cc.openstrata.admin.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import cc.openstrata.admin.application.dto.UpsertUserRequest;
import cc.openstrata.admin.application.dto.UserView;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserEntity;
import cc.openstrata.admin.infrastructure.persistence.PlatformUserJpaRepository;
import java.util.List;
import java.util.Optional;
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
}
