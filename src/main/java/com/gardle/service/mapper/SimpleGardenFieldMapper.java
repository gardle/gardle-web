package com.gardle.service.mapper;

import com.gardle.domain.GardenField;
import com.gardle.service.dto.SimpleGardenFieldDTO;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SimpleGardenFieldMapper {

    GardenField toGardenField(SimpleGardenFieldDTO simpleGardenFieldDTO);

    SimpleGardenFieldDTO toDTO(GardenField gardenField);
}
