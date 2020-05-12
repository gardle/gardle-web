package com.gardle.service.dto;

import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.web.rest.LeasingController;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.InstantFilter;
import io.github.jhipster.service.filter.LongFilter;
import lombok.Data;

import java.io.Serializable;

/**
 * Criteria class for the {@link com.gardle.domain.Leasing} entity. This class is used
 * in {@link LeasingController} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /leasings?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Data
public class LeasingCriteria implements Serializable, Criteria {
    /**
     * Class for filtering LeasingStatus
     */
    public static class LeasingStatusFilter extends Filter<LeasingStatus> {

        public LeasingStatusFilter() {
        }

        public LeasingStatusFilter(LeasingStatusFilter filter) {
            super(filter);
        }

        @Override
        public LeasingStatusFilter copy() {
            return new LeasingStatusFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter from;

    private InstantFilter to;

    private LeasingStatusFilter status;

    private LongFilter paymentId;

    private LongFilter messageId;

    private LongFilter userId;

    private LongFilter gardenFieldId;

    public LeasingCriteria() {
    }

    public LeasingCriteria(LeasingCriteria other) {
        this.id = other.id == null ? null : other.id.copy();
        this.from = other.from == null ? null : other.from.copy();
        this.to = other.to == null ? null : other.to.copy();
        this.status = other.status == null ? null : other.status.copy();
        this.paymentId = other.paymentId == null ? null : other.paymentId.copy();
        this.messageId = other.messageId == null ? null : other.messageId.copy();
        this.userId = other.userId == null ? null : other.userId.copy();
        this.gardenFieldId = other.gardenFieldId == null ? null : other.gardenFieldId.copy();
    }

    @Override
    public LeasingCriteria copy() {
        return new LeasingCriteria(this);
    }

}
