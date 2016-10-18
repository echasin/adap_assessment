package com.innvo.repository;

import com.innvo.domain.Responsedetail;

import org.springframework.data.jpa.repository.*;

import java.util.List;

/**
 * Spring Data JPA repository for the Responsedetail entity.
 */
@SuppressWarnings("unused")
public interface ResponsedetailRepository extends JpaRepository<Responsedetail,Long> {

}
