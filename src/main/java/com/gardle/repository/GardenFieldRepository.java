package com.gardle.repository;

import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.repository.search.GardenFieldSearchRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GardenFieldRepository extends JpaRepository<GardenField, Long>, GardenFieldSearchRepository {

    Page<GardenField> findAllByOwner(Pageable pageable, User user);

    @Query("SELECT coalesce(min(g.sizeInM2),0) from GardenField g ")
    Double getMinSize();

    @Query("SELECT coalesce(max(g.sizeInM2),100) from GardenField g ")
    Double getMaxSize();

    @Query("SELECT min(g.pricePerM2 * g.sizeInM2 * 30) from GardenField g")
    Double getMinPriceForPerMonth();

    @Query("SELECT max(g.pricePerM2 * g.sizeInM2 * 30) from GardenField g")
    Double getMaxPriceForSizePerMonth();
}
