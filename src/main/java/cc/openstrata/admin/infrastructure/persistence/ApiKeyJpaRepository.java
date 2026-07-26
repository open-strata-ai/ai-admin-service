package cc.openstrata.admin.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for {@link ApiKeyEntity} (PR-C). */
public interface ApiKeyJpaRepository extends JpaRepository<ApiKeyEntity, String> {

    @Query("select k from ApiKeyEntity k where k.tenantId = :tenantId")
    List<ApiKeyEntity> findByTenantId(@Param("tenantId") String tenantId);
}
