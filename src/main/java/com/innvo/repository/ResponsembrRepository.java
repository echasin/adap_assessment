package com.innvo.repository;

import com.innvo.domain.Responsembr;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Responsembr entity.
 */
@SuppressWarnings("unused")
public interface ResponsembrRepository extends JpaRepository<Responsembr,Long> {

}
