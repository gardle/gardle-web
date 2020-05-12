package com.gardle.service.mapper;

import com.gardle.domain.Leasing;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.leasing.LeasingDTO;
import org.mapstruct.Mapper;

/**
 * Mapper for the entity {@link Leasing} and its DTO {@link LeasingDTO}.
 */
@Mapper(componentModel = "spring", uses = {MessageMapper.class, SimpleGardenFieldMapper.class,
    SimpleUserMapper.class, GardenFieldRepository.class, UserRepository.class})
public interface LeasingMapper extends EntityMapper<LeasingDTO, Leasing> {

    Leasing toEntity(LeasingDTO leasingDTO);

    LeasingDTO toDto(Leasing leasing);

    default Leasing fromId(Long id) {
        if (id == null) {
            return null;
        }
        Leasing leasing = new Leasing();
        leasing.setId(id);
        return leasing;
    }
}
