package cc.openstrata.admin.web;

import cc.openstrata.admin.application.UserAppService;
import cc.openstrata.admin.application.dto.UpsertUserRequest;
import cc.openstrata.admin.application.dto.UserView;
import cc.openstrata.admin.config.TenantContext;
import cc.openstrata.admin.domain.DomainException;
import cc.openstrata.admin.web.ErrorCode;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Platform user directory REST surface (PR-C). Roles may include `viewer`. */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminUserController {

    private final UserAppService userApp;

    public AdminUserController(UserAppService userApp) {
        this.userApp = userApp;
    }

    @GetMapping("/users")
    public List<UserView> list() {
        return userApp.list();
    }

    @GetMapping("/users/{id}")
    public UserView get(@PathVariable String id) {
        return userApp.get(id);
    }

    @PostMapping("/users")
    public ResponseEntity<UserView> create(@Valid @RequestBody UpsertUserRequest req) {
        requirePlatformAdmin();
        return ResponseEntity.status(201).body(userApp.create(req));
    }

    @PutMapping("/users/{id}")
    public UserView update(@PathVariable String id,
                           @Valid @RequestBody UpsertUserRequest req) {
        requirePlatformAdmin();
        return userApp.update(id, req);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        requirePlatformAdmin();
        userApp.delete(id);
        return ResponseEntity.noContent().build();
    }

    private void requirePlatformAdmin() {
        if (!TenantContext.isPlatformAdmin()) {
            throw new DomainException(ErrorCode.FORBIDDEN,
                "platform-admin role required for user writes");
        }
    }
}
