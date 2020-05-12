package com.gardle.service.mapper;

import com.gardle.domain.Message;
import com.gardle.service.dto.MessageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper for the entity {@link Message} and its DTO {@link MessageDTO}.
 */
@Mapper(componentModel = "spring", uses = {SimpleUserMapper.class})
public interface MessageMapper extends EntityMapper<MessageDTO, Message> {

    @Mapping(target = "type", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "opened", ignore = true)
    @Mapping(target = "id", ignore = true)
    Message toEntity(MessageDTO messageDTO);

    default Message fromId(Long id) {
        if (id == null) {
            return null;
        }
        Message message = new Message();
        message.setId(id);
        return message;
    }
}
