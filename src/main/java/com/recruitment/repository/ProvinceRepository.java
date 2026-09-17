package com.recruitment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.recruitment.entity.Province;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long>, JpaSpecificationExecutor<Province> {

	boolean existsByName(String name);

	boolean existsByNameAndIdNot(String name, Long id);

	@Query(value = "SELECT * FROM provinces WHERE id = :id AND deleted_at = 1", nativeQuery = true)
	Optional<Province> findSoftDeletedById(@Param("id") Long id);

	@Modifying
	@Query(value = "UPDATE provinces SET deleted_at = 0, deleted_by = NULL WHERE id = :id AND deleted_at = 1",
			nativeQuery = true)
	int restoreById(@Param("id") Long id);

	@Modifying
	@Query(value = "DELETE FROM provinces WHERE id = :id", nativeQuery = true)
	void hardDeleteById(@Param("id") Long id);
}
