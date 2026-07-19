package cc.openstrata.admin.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository for {@link PackageTemplateEntity} (PA-04). */
public interface PackageTemplateJpaRepository extends JpaRepository<PackageTemplateEntity, String> {
}
