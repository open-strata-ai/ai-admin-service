package cc.openstrata.admin.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for {@link PlatformUserEntity} (PR-C). */
public interface PlatformUserJpaRepository extends JpaRepository<PlatformUserEntity, String> {

    @Query("select u from PlatformUserEntity u where u.tenantId = :tenantId")
    List<PlatformUserEntity> findByTenantId(@Param("tenantId") String tenantId);
}
