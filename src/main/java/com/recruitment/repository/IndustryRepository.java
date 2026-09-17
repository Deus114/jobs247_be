package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.Industry;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, Long>, JpaSpecificationExecutor<Industry> {

	boolean existsByIndustryGroupId(Long industryGroupId);

	long countByIndustryGroupId(Long industryGroupId);

	@Query(value = "SELECT COUNT(*) FROM industries WHERE industry_group_id = :groupId", nativeQuery = true)
	long countAllByIndustryGroupId(@Param("groupId") Long groupId);

	@Query("""
			SELECT i FROM Industry i
			JOIN FETCH i.industryGroup
			WHERE i.id = :id
			""")
	Optional<Industry> findByIdWithGroup(@Param("id") Long id);

	@Query(value = "SELECT * FROM industries WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<Industry> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE industries SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM industries WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
