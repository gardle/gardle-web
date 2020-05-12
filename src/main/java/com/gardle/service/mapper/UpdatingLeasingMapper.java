package com.gardle.service.mapper;

import com.gardle.domain.Leasing;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.leasing.UpdatingLeasingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GardenFieldRepository.class, UserRepository.class})
public interface UpdatingLeasingMapper extends EntityMapper<UpdatingLeasingDTO, Leasing> {

    @Mapping(source = "gardenFieldId", target = "gardenField")
    Leasing toEntity(UpdatingLeasingDTO leasingDTO);

    @Mapping(source = "gardenField.id", target = "gardenFieldId")
    UpdatingLeasingDTO toDto(Leasing leasing);

    default Leasing fromId(Long id) {
        if (id == null) {
            return null;
        }
        Leasing leasing = new Leasing();
        leasing.setId(id);
        return leasing;
    }
}
