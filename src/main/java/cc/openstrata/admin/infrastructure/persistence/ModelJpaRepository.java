package cc.openstrata.admin.infrastructure.persistence;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Spring Data JPA repository for {@link ModelEntity} (PR-C). */
public interface ModelJpaRepository extends JpaRepository<ModelEntity, String> {

    @Query("select m from ModelEntity m where m.providerId = :providerId")
    List<ModelEntity> findByProviderId(@Param("providerId") String providerId);
}
