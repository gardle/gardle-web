package com.gardle.service.mapper;

import com.gardle.domain.Leasing;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {GardenFieldRepository.class, UserRepository.class})
public interface CreatorLeasingMapper extends EntityMapper<CreatorLeasingDTO, Leasing> {

    @Mapping(source = "gardenFieldId", target = "gardenField")
    Leasing toEntity(CreatorLeasingDTO leasingDTO);

    @Mapping(source = "gardenField.id", target = "gardenFieldId")
    @Mapping(source = "gardenField.name", target = "gardenFieldName")
    CreatorLeasingDTO toDto(Leasing leasing);

    default Leasing fromId(Long id) {
        if (id == null) {
            return null;
        }
        Leasing leasing = new Leasing();
        leasing.setId(id);
        return leasing;
    }
}
