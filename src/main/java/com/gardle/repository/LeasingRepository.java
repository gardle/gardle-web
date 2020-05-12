package com.gardle.repository;

import com.gardle.domain.GardenField;
import com.gardle.domain.Leasing;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.service.dto.leasing.LeasingDateRangeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data  repository for the Leasing entity.
 */
@Repository
public interface LeasingRepository extends JpaRepository<Leasing, Long>, JpaSpecificationExecutor<Leasing> {

    @Query("select l from Leasing l where (:gf = l.gardenField) " +
        "and not ((l.from <= :from and l.to <= :from) or (l.from >= :to and l.to >= :to)) "
        + "and (l.status = 'RESERVED' or (l.status = 'OPEN' and l.user = :user))")
    List<Leasing> findAllOverlapping(@Param("gf") GardenField gardenField,
                                     @Param("user") User user,
                                     @Param("from") Instant from, @Param("to") Instant to);

    @Query("Select NEW com.gardle.service.dto.leasing.LeasingDateRangeDTO(l.from, l.to) from Leasing l " +
        " where l.gardenField = :gf " +
        " and (coalesce(:status, NULL) is null or l.status = :status) " +
        " and (coalesce(:from, NULL) is null or l.from >= :from) " +
        " and (coalesce(:to, NULL) is null or l.to <= :to)")
    List<LeasingDateRangeDTO> findLeasedDateRangesByFromAndTo(@Param("gf") GardenField gardenField,
                                                              @Param("status") LeasingStatus status,
                                                              @Param("from") Instant from,
                                                              @Param("to") Instant to);

    @Query("Select l from Leasing l where l.gardenField = :gf " +
        " and (coalesce(:statusList, NULL) is null or l.status IN (:statusList)) " +
        " and (coalesce(:from, NULL) is null or l.from >= :from) " +
        " and (coalesce(:to, NULL) is null or l.to <= :to)" +
        " and (coalesce(:past, NULL) is null or l.to < :past) " +
        " and (coalesce(:ongoing, NULL) is null or :ongoing BETWEEN l.from AND l.to) " +
        " and (coalesce(:future, NULL) is null or l.from > :future)")
    Page<Leasing> findAllByGardenFieldAndStatusAndFromAndTo(Pageable pageable, @Param("gf") GardenField gardenField,
                                                            @Param("statusList") List<LeasingStatus> leasingStatusList,
                                                            @Param("from") Instant from,
                                                            @Param("to") Instant to,
                                                            @Param("past") Instant past,
                                                            @Param("ongoing") Instant ongoing,
                                                            @Param("future") Instant future);

    @Query("Select l from Leasing l where l.user = :user " +
        " and (coalesce(:statusList, NULL) is null or l.status IN (:statusList)) " +
        " and (coalesce(:from, NULL) is null or l.from >= :from) " +
        " and (coalesce(:to, NULL) is null or l.to <= :to) " +
        " and (coalesce(:past, NULL) is null or l.to < :past) " +
        " and (coalesce(:ongoing, NULL) is null or :ongoing BETWEEN l.from AND l.to) " +
        " and (coalesce(:future, NULL) is null or l.from > :future)")
    Page<Leasing> findAllByUserAndStatusAndFromAndTo(Pageable pageable, @Param("user") User user,
                                                     @Param("statusList") List<LeasingStatus> leasingStatusList,
                                                     @Param("from") Instant from,
                                                     @Param("to") Instant to,
                                                     @Param("past") Instant past,
                                                     @Param("ongoing") Instant ongoing,
                                                     @Param("future") Instant future);

    @Query("Select l from Leasing l where l.gardenField.owner = :user " +
        " and (coalesce(:statusList, NULL) is null or l.status IN (:statusList)) " +
        " and (coalesce(:from, NULL) is null or l.from >= :from) " +
        " and (coalesce(:to, NULL) is null or l.to <= :to) " +
        " and (coalesce(:past, NULL) is null or l.to < :past) " +
        " and (coalesce(:ongoing, NULL) is null or :ongoing BETWEEN l.from AND l.to) " +
        " and (coalesce(:future, NULL) is null or l.from > :future)")
    Page<Leasing> findAllByOwnerAndStatusAndFromAndTo(Pageable pageable, @Param("user") User user,
                                                      @Param("statusList") List<LeasingStatus> leasingStatusList,
                                                      @Param("from") Instant from,
                                                      @Param("to") Instant to,
                                                      @Param("past") Instant past,
                                                      @Param("ongoing") Instant ongoing,
                                                      @Param("future") Instant future);
}
