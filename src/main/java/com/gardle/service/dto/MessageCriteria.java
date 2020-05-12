package com.gardle.service.dto;

import com.gardle.domain.enumeration.MessageType;
import com.gardle.web.rest.MessageController;
import io.github.jhipster.service.Criteria;
import io.github.jhipster.service.filter.Filter;
import io.github.jhipster.service.filter.LongFilter;
import lombok.Data;

import java.io.Serializable;

/**
 * Criteria class for the {@link com.gardle.domain.Message} entity. This class is used
 * in {@link MessageController} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /messages?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@Data
public class MessageCriteria implements Serializable, Criteria {
    /**
     * Class for filtering MessageType
     */
    public static class MessageTypeFilter extends Filter<MessageType> {

        public MessageTypeFilter() {
        }

        public MessageTypeFilter(MessageTypeFilter filter) {
            super(filter);
        }

        @Override
        public MessageTypeFilter copy() {
            return new MessageTypeFilter(this);
        }

    }

    private static final long serialVersionUID = 1L;


    private MessageTypeFilter type;

    private LongFilter leasingId;

    public MessageCriteria() {
    }

    public MessageCriteria(MessageCriteria other) {
        this.type = other.type == null ? null : other.type.copy();
        this.leasingId = other.leasingId == null ? null : other.leasingId.copy();
    }

    @Override
    public MessageCriteria copy() {
        return new MessageCriteria(this);
    }


}
