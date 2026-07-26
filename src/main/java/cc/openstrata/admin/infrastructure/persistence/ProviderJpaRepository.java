package cc.openstrata.admin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link ProviderEntity} (PR-C). */
public interface ProviderJpaRepository extends JpaRepository<ProviderEntity, String> {
}
