package com.recruitment.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.recruitment.entity.BusinessConfig;

public interface BusinessConfigRepository extends JpaRepository<BusinessConfig, Long> {
}
