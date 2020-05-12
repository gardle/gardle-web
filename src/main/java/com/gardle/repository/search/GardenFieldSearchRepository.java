package com.gardle.repository.search;

import com.gardle.domain.GardenField;
import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Provides additional search functionality with hibernate search (apache lucene)
 * Should be inherited by the corresponding repository (GardenFieldRepository)
 */
public interface GardenFieldSearchRepository {
    @NotNull
    Page<GardenField> filter(final Pageable pageable, @NotNull final GardenFieldFilterCriteria gardenFieldFilterCriteria);

    @NotNull
    List<GardenField> autocompleteSearch(@Nullable final String partialSearchString);
}
