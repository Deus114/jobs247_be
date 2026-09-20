package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.EducationLevel;

@Repository
public interface EducationLevelRepository
		extends JpaRepository<EducationLevel, Long>, JpaSpecificationExecutor<EducationLevel> {

	boolean existsByNameVi(String nameVi);

	boolean existsByNameViAndIdNot(String nameVi, Long id);

	boolean existsByNameEn(String nameEn);

	boolean existsByNameEnAndIdNot(String nameEn, Long id);

	@Query(value = "SELECT * FROM education_levels WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<EducationLevel> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE education_levels SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM education_levels WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
