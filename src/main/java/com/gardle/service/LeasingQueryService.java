package com.gardle.service;

import com.gardle.domain.Leasing;
import com.gardle.domain.Leasing_;
import com.gardle.domain.Message_;
import com.gardle.repository.LeasingRepository;
import com.gardle.service.dto.LeasingCriteria;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.service.mapper.LeasingMapper;
import io.github.jhipster.service.QueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.JoinType;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for executing complex queries for {@link Leasing} entities in the database.
 * The main input is a {@link LeasingCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link List} of {@link LeasingDTO} or a {@link Page} of {@link LeasingDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class LeasingQueryService extends QueryService<Leasing> {

    private final Logger log = LoggerFactory.getLogger(LeasingQueryService.class);

    private final LeasingRepository leasingRepository;

    private final LeasingMapper leasingMapper;

    public LeasingQueryService(LeasingRepository leasingRepository, LeasingMapper leasingMapper) {
        this.leasingRepository = leasingRepository;
        this.leasingMapper = leasingMapper;
    }

    /**
     * Return a {@link List} of {@link LeasingDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public List<LeasingDTO> findByCriteria(LeasingCriteria criteria) {
        log.debug("find by criteria : {}", criteria);
        final Specification<Leasing> specification = createSpecification(criteria);
        return (leasingRepository.findAll(specification)).stream()
            .map(leasingMapper::toDto).collect(Collectors.toList());
    }

    /**
     * Return a {@link Page} of {@link LeasingDTO} which matches the criteria from the database.
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page     The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<LeasingDTO> findByCriteria(LeasingCriteria criteria, Pageable page) {
        log.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Leasing> specification = createSpecification(criteria);
        return leasingRepository.findAll(specification, page)
            .map(leasingMapper::toDto);
    }

    /**
     * Function to convert ConsumerCriteria to a {@link Specification}
     *
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    private Specification<Leasing> createSpecification(LeasingCriteria criteria) {
        Specification<Leasing> specification = Specification.where(null);
        if (criteria != null) {
            if (criteria.getId() != null) {
                specification = specification.and(buildSpecification(criteria.getId(), Leasing_.id));
            }
            if (criteria.getFrom() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getFrom(), Leasing_.from));
            }
            if (criteria.getTo() != null) {
                specification = specification.and(buildRangeSpecification(criteria.getTo(), Leasing_.to));
            }
            if (criteria.getStatus() != null) {
                specification = specification.and(buildSpecification(criteria.getStatus(), Leasing_.status));
            }
            if (criteria.getMessageId() != null) {
                specification = specification.and(buildSpecification(criteria.getMessageId(),
                    root -> root.join(Leasing_.messages, JoinType.LEFT).get(Message_.id)));
            }
        }
        return specification;
    }
}
