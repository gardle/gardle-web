package com.gardle.repository.search.impl;

import com.gardle.domain.GardenField;
import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import com.gardle.domain.criteria.LocationFilterCriteria;
import com.gardle.repository.search.GardenFieldSearchRepository;
import lombok.RequiredArgsConstructor;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.BooleanJunction;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.hibernate.search.query.dsl.Unit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Nullable;
import javax.persistence.EntityManager;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class GardenFieldSearchRepositoryImpl implements GardenFieldSearchRepository {
    private final EntityManager entityManager;
    private final static Logger LOGGER = LoggerFactory.getLogger(GardenFieldSearchRepositoryImpl.class);

    @Transactional
    @Override
    @NotNull
    public List<GardenField> autocompleteSearch(@NotNull String partialSearchString) {
        FullTextEntityManager fullTextEntityManager
            = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
            .buildQueryBuilder()
            .forEntity(GardenField.class)
            .get();
        BooleanJunction<?> junction = queryBuilder.bool();

        createNameAndCityQuery(queryBuilder, junction, partialSearchString);
        createDeletedNotNullQuery(queryBuilder, junction);

        FullTextQuery fullTextQuery = fullTextEntityManager
            .createFullTextQuery(junction.createQuery(), GardenField.class)
            .setMaxResults(5);

        return fullTextQuery.getResultList();
    }

    @Override
    @NotNull
    @Transactional
    public Page<GardenField> filter(Pageable pageable, @NotNull GardenFieldFilterCriteria gardenFieldFilterCriteria) {
        FullTextEntityManager fullTextEntityManager
            = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager.getSearchFactory()
            .buildQueryBuilder().forEntity(GardenField.class).get();

        Query combinedQuery = this.buildCombinedQuery(queryBuilder, gardenFieldFilterCriteria);
        LOGGER.debug("calculated filter query: " + combinedQuery);

        FullTextQuery fullTextQuery = fullTextEntityManager
            .createFullTextQuery(combinedQuery, GardenField.class);
        List<GardenField> gardenFields = fullTextQuery.getResultList();
        //Remove all gardenfields that have leasings in search timeframe.
        if (gardenFieldFilterCriteria.getLeasingTimeFrom() != null && gardenFieldFilterCriteria.getLeasingTimeTo() != null) {
            gardenFields = gardenFields.stream().filter(gardenField -> isGardenFieldBookableBetween(gardenField, gardenFieldFilterCriteria.getLeasingTimeFrom(), gardenFieldFilterCriteria.getLeasingTimeTo())).collect(Collectors.toList());
        }
        return this.convertListToPage(pageable, gardenFields);
    }

    @NotNull
    private Query buildCombinedQuery(@NotNull final QueryBuilder queryBuilder,
                                     @NotNull final GardenFieldFilterCriteria gardenFieldFilterCriteria) {
        BooleanJunction<?> junction = queryBuilder.bool();

        createMinPriceQuery(queryBuilder, junction, gardenFieldFilterCriteria.getMinPricePerM2());
        createMaxPriceQuery(queryBuilder, junction, gardenFieldFilterCriteria.getMaxPricePerM2());
        createMinSizeQuery(queryBuilder, junction, gardenFieldFilterCriteria.getSizeInM2LowerBound());
        createMaxSizeQuery(queryBuilder, junction, gardenFieldFilterCriteria.getSizeInM2UpperBound());
        createRoofedQuery(queryBuilder, junction, gardenFieldFilterCriteria.getRoofed());
        createSpatialQuery(queryBuilder, junction, gardenFieldFilterCriteria.getLocationFilterCriteria());
        createNameAndCityAndDescriptionQuery(queryBuilder, junction, gardenFieldFilterCriteria.getKeywords());
        createWaterQuery(queryBuilder, junction, gardenFieldFilterCriteria.getWater());
        createElectricityQuery(queryBuilder, junction, gardenFieldFilterCriteria.getElectricity());
        createHighQuery(queryBuilder, junction, gardenFieldFilterCriteria.getHigh());
        createGlassHouseQuery(queryBuilder, junction, gardenFieldFilterCriteria.getGlassHouse());
        createDeletedNotNullQuery(queryBuilder, junction);

        return junction.createQuery();
    }

    private void createDeletedNotNullQuery(QueryBuilder queryBuilder, BooleanJunction<?> junction) {
        Query notDeletedQuery = queryBuilder
            .keyword()
            .onFields("deleted")
            .matching(null)
            .createQuery();
        junction.must(notDeletedQuery);

    }

    @NotNull
    private Page<GardenField> convertListToPage(@NotNull Pageable pageable, @Nullable List<GardenField> gardenFields) {
        if (gardenFields == null || gardenFields.isEmpty()) {
            return Page.empty(pageable);
        }
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), gardenFields.size());
        return new PageImpl<>(gardenFields.subList(start, end), pageable, gardenFields.size());
    }

    private void createMinPriceQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction,
                                     Double minPrice) {
        if (minPrice != null) {
            Query priceQuery = queryBuilder
                .range()
                .onField("pricePerMonth")
                .above(minPrice)
                .createQuery();
            junction.must(priceQuery);
        }
    }

    private void createMaxPriceQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction,
                                     Double maxPrice) {
        if (maxPrice != null) {
            Query priceQuery = queryBuilder
                .range()
                .onField("pricePerMonth")
                .below(maxPrice)
                .createQuery();
            junction.must(priceQuery);
        }
    }

    private void createMinSizeQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction,
                                    Double minSize) {
        if (minSize != null) {
            Query priceQuery = queryBuilder
                .range()
                .onField("sizeInM2")
                .above(minSize)
                .createQuery();
            junction.must(priceQuery);
        }
    }

    private void createMaxSizeQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction,
                                    Double maxSize) {
        if (maxSize != null) {
            Query sizeQuery = queryBuilder
                .range()
                .onField("sizeInM2")
                .below(maxSize)
                .createQuery();
            junction.must(sizeQuery);
        }
    }

    private void createRoofedQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction, Boolean roofed) {
        if (roofed != null) {
            Query roofedQuery = queryBuilder
                .keyword()
                .onFields("roofed")
                .matching(roofed)
                .createQuery();
            junction.must(roofedQuery);
        }
    }

    private void createSpatialQuery(@NotNull QueryBuilder queryBuilder, BooleanJunction<?> junction, LocationFilterCriteria locationFilterCriteria) {
        if (locationFilterCriteria != null && locationFilterCriteria.checkNonNull()) {
            Query locationQuery = queryBuilder
                .spatial()
                .within(locationFilterCriteria.getRadiusInKM(), Unit.KM)
                .ofLatitude(locationFilterCriteria.getLatitude())
                .andLongitude(locationFilterCriteria.getLongitude())
                .createQuery();
            junction.must(locationQuery);
        }
    }

    private void createNameAndCityAndDescriptionQuery(@NotNull final QueryBuilder queryBuilder,
                                                      @NotNull final BooleanJunction<?> junction,
                                                      @Nullable final String searchString) {
        if (searchString != null) {
            Query descriptionQuery = queryBuilder
                .keyword()
                .fuzzy()
                .withPrefixLength(2)
                .withEditDistanceUpTo(2)
                .onFields("name", "city", "description")
                .matching(searchString)
                .createQuery();
            junction.must(descriptionQuery);
        }
    }

    private void createNameAndCityQuery(@NotNull final QueryBuilder queryBuilder,
                                        @NotNull final BooleanJunction<?> junction,
                                        @Nullable final String searchString) {
        if (searchString != null) {
            Query descriptionQuery = queryBuilder
                .keyword()
                .fuzzy()
                .withPrefixLength(3)
                .withEditDistanceUpTo(2)
                .onFields("name", "city")
                .matching(searchString)
                .createQuery();
            junction.must(descriptionQuery);
        }
    }

    private void createWaterQuery(QueryBuilder queryBuilder, BooleanJunction<?> junction, Boolean water) {
        if (water != null) {
            Query waterQuery = queryBuilder
                .keyword()
                .onFields("water")
                .matching(water)
                .createQuery();
            junction.must(waterQuery);
        }
    }

    private void createElectricityQuery(QueryBuilder queryBuilder, BooleanJunction<?> junction, Boolean electricity) {
        if (electricity != null) {
            Query electricityQuery = queryBuilder
                .keyword()
                .onFields("electricity")
                .matching(electricity)
                .createQuery();
            junction.must(electricityQuery);
        }
    }

    private void createHighQuery(QueryBuilder queryBuilder, BooleanJunction<?> junction, Boolean high) {
        if (high != null) {
            Query highQuery = queryBuilder
                .keyword()
                .onFields("high")
                .matching(high)
                .createQuery();
            junction.must(highQuery);
        }
    }

    private void createGlassHouseQuery(QueryBuilder queryBuilder, BooleanJunction<?> junction, Boolean glassHouse) {
        if (glassHouse != null) {
            Query glassHouseQuery = queryBuilder
                .keyword()
                .onFields("glassHouse")
                .matching(glassHouse)
                .createQuery();
            junction.must(glassHouseQuery);
        }
    }

    private boolean isGardenFieldBookableBetween(GardenField gardenField, Instant from, Instant to) {
        javax.persistence.Query query = entityManager.createQuery("SELECT l.id FROM Leasing l " +
            "WHERE l.gardenField.id = :gardenFieldId " +
            "AND l.status = 'RESERVED' AND ((:from <= l.from AND l.from <= :to ) " +
            "OR ( :from <= l.to  AND l.to <= :to) OR (l.from <= :from AND :to <= l.to ))")
            .setParameter("gardenFieldId", gardenField.getId())
            .setParameter("from", from)
            .setParameter("to", to)
            .setMaxResults(1);
        return !query.getResultStream().findAny().isPresent();
    }
}
