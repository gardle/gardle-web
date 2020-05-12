package com.gardle.service;

import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.service.dto.FilterBoundariesDTO;
import com.gardle.service.dto.GardenFieldDTO;
import com.gardle.service.exception.*;
import com.gardle.service.mapper.GardenFieldMapper;
import com.gardle.validator.GardenFieldFilterCriteriaValidator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GardenFieldService {

    private final Logger log = LoggerFactory.getLogger(GardenFieldService.class);

    private final GardenFieldRepository gardenFieldRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;
    private final SecurityHelperService securityHelperService;
    private final GardenFieldFilterCriteriaValidator gardenFieldFilterCriteriaValidator;
    private final GardenFieldMapper gardenFieldMapper;

    public GardenFieldDTO createGardenField(GardenFieldDTO gardenFieldDTO) {
        User currentUser = securityHelperService.getLoggedInUser();
        if (gardenFieldDTO.getOwner() != null && !gardenFieldDTO.getOwner().getId().equals(currentUser.getId())) {
            throw new MissingAuthorityForGardenFieldServiceException();
        }
        if (currentUser.getStripeAccountVerified() == null || !currentUser.getStripeAccountVerified()) {
            throw new MissingStripeVerificationServiceException();
        }
        GardenField entity = gardenFieldMapper.toEntity(gardenFieldDTO);
        entity.setOwner(currentUser);
        entity = gardenFieldRepository.save(entity);
        return gardenFieldMapper.toDto(entity);
    }

    public Page<GardenFieldDTO> getAllGardenFields(Pageable pageable) {
        return gardenFieldRepository.findAll(pageable).map(gardenFieldMapper::toDto);
    }

    public GardenFieldDTO findOne(Long id) {
        return gardenFieldRepository.findById(id).map(gardenFieldMapper::toDto).orElseThrow(GardenFieldNotFoundServiceException::new);
    }

    public Page<GardenFieldDTO> getAllUserGardenfields(Pageable pageable) {
        return gardenFieldRepository.findAllByOwner(pageable, this.securityHelperService.getLoggedInUser()).map(gardenFieldMapper::toDto);
    }

    public Optional<GardenFieldDTO> updateGardenField(GardenFieldDTO gardenFieldDTO) {
        return Optional.of(gardenFieldRepository
            .findById(gardenFieldDTO.getId()))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(gardenField -> {
                securityHelperService.checkAuthority(gardenField.getOwner().getId());
                gardenField.setName(gardenFieldDTO.getName());
                gardenField.setDescription(gardenFieldDTO.getDescription());
                gardenField.setSizeInM2(gardenFieldDTO.getSizeInM2());
                gardenField.setPricePerM2(gardenFieldDTO.getPricePerM2());
                gardenField.setLatitude(gardenFieldDTO.getLatitude());
                gardenField.setLongitude(gardenFieldDTO.getLongitude());
                gardenField.setCity(gardenFieldDTO.getCity());
                gardenField.setRoofed(gardenFieldDTO.getRoofed());
                gardenField.setGlassHouse(gardenFieldDTO.getGlassHouse());
                gardenField.setHigh(gardenFieldDTO.getHigh());
                gardenField.setWater(gardenFieldDTO.getWater());
                gardenField.setElectricity(gardenFieldDTO.getElectricity());
                gardenField.setPhValue(gardenFieldDTO.getPhValue());
                //This allows changing a gardenfield to a different user/owner!
                gardenField.setOwner(userRepository.findById(gardenFieldDTO.getOwner().getId())
                    .orElseThrow(GardenFieldWithoutOwnerServiceException::new));
                gardenFieldRepository.save(gardenField);
                log.debug("Changed Information for GardenField: {}", gardenFieldDTO);
                return gardenField;
            })
            .map(gardenFieldMapper::toDto);
    }

    public void deleteGardenField(Long id) {
        gardenFieldRepository.findById(id).ifPresent(gardenField -> {
            securityHelperService.checkAuthority(gardenField.getOwner().getId());
            gardenField.setDeleted(Instant.now());
            imageStorageService.deleteImages(id);
            gardenFieldRepository.save(gardenField);
            log.debug("Deleted GardenField: {}", gardenField);
        });
    }

    public Page<GardenFieldDTO> filterGardenFields(Pageable pageable, @Nullable GardenFieldFilterCriteria gardenFieldFilterCriteria) {
        final String validationResult = gardenFieldFilterCriteriaValidator.isValid(gardenFieldFilterCriteria);
        if (validationResult.isEmpty()) {
            if (gardenFieldFilterCriteria == null || gardenFieldFilterCriteria.checkAllNull()) {
                return gardenFieldRepository.findAll(pageable).map(gardenFieldMapper::toDto);
            }
            return gardenFieldRepository.filter(pageable, gardenFieldFilterCriteria).map(gardenFieldMapper::toDto);
        } else {
            throw new GardenFieldFilterCriteriaValidationServiceException(validationResult);
        }
    }

    public List<GardenFieldDTO> autocomplete(@Nullable final String partialSearchString) {
        return this.gardenFieldRepository.autocompleteSearch(partialSearchString)
            .stream().map(gardenFieldMapper::toDto).collect(Collectors.toList());
    }

    public FilterBoundariesDTO getFilterBoundaries() {
        FilterBoundariesDTO filterBoundariesDTO = new FilterBoundariesDTO();
        filterBoundariesDTO.setMinPrice(Math.floor(gardenFieldRepository.getMinPriceForPerMonth()));
        filterBoundariesDTO.setMaxPrice(Math.ceil(gardenFieldRepository.getMaxPriceForSizePerMonth()));

        filterBoundariesDTO.setMinSize(Math.floor(gardenFieldRepository.getMinSize()));
        filterBoundariesDTO.setMaxSize(Math.ceil(gardenFieldRepository.getMaxSize()));
        return filterBoundariesDTO;
    }
}
