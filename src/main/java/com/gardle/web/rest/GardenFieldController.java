package com.gardle.web.rest;

import com.gardle.domain.criteria.GardenFieldFilterCriteria;
import com.gardle.domain.criteria.LocationFilterCriteria;
import com.gardle.domain.enumeration.LeasingState;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.GardenFieldService;
import com.gardle.service.LeasingService;
import com.gardle.service.dto.FilterBoundariesDTO;
import com.gardle.service.dto.GardenFieldDTO;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.web.rest.errors.BadRequestException;
import com.gardle.web.rest.errors.GardleErrorKey;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Api(value = "Endpoint for gardenfields")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GardenFieldController {

    private final Logger log = LoggerFactory.getLogger(GardenFieldController.class);
    @Value("${jhipster.clientApp.name}")
    private String applicationName;
    private final GardenFieldService gardenFieldService;
    private final LeasingService leasingService;

    @ApiOperation(value = "Create a gardenfield")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created a gardenfield"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to create a gardenfield"),
    })
    @PostMapping("/gardenfields")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<GardenFieldDTO> createGardenField(
        @ApiParam(value = "Gardenfield object which will be saved", required = true)
        @Valid @RequestBody GardenFieldDTO gardenFieldDTO) throws URISyntaxException, BadRequestException {
        log.debug("REST request to save GardenField : {}", gardenFieldDTO);

        if (gardenFieldDTO.getId() != null) {
            throw new BadRequestException(GardleErrorKey.GARDENFIELD_ID_ALREADY_EXISTS);
        }

        GardenFieldDTO newGardenFieldDTO = gardenFieldService.createGardenField(gardenFieldDTO);
        return ResponseEntity.created(new URI("/api/v1/gardenFields/" + newGardenFieldDTO.getId()))
            .body(newGardenFieldDTO);

    }

    @ApiOperation(value = "View a page of available gardenfields")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfields")
    })
    @GetMapping("/gardenfields")
    public ResponseEntity<Page<GardenFieldDTO>> getAllGardenFields(Pageable pageable) {
        final Page<GardenFieldDTO> page = gardenFieldService.getAllGardenFields(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return new ResponseEntity<>(page, headers, HttpStatus.OK);
    }

    @GetMapping("/gardenfields/{id}")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully found the gardenfield"),
        @ApiResponse(code = 404, message = "Gardenfield has not been found")
    })
    public ResponseEntity<GardenFieldDTO> findOne(@PathVariable long id) {
        return new ResponseEntity<>(gardenFieldService.findOne(id), HttpStatus.OK);
    }

    @ApiOperation(value = "Update a gardenfield")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully updated a gardenfield"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to update this gardenfield"),
        @ApiResponse(code = 409, message = "Gardenfield must have an owner")
    })
    @PutMapping("/gardenfields")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<GardenFieldDTO> updateGardenField(
        @ApiParam(value = "Gardenfield object which will be saved", required = true)
        @Valid @RequestBody GardenFieldDTO gardenFieldDTO) {
        log.debug("REST request to update GardenField : {}", gardenFieldDTO);
        Optional<GardenFieldDTO> updatedGardenFieldDTO = gardenFieldService.updateGardenField(gardenFieldDTO);

        return ResponseUtil.wrapOrNotFound(updatedGardenFieldDTO,
            HeaderUtil.createAlert(applicationName, "gardenFieldManagement.updated", gardenFieldDTO.getId().toString()));
    }

    @ApiOperation(value = "Delete a gardenfield")
    @DeleteMapping("/gardenfields/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Void> deleteGardenField(
        @ApiParam(value = "Gardenfield Id from which gardenfield object will delete from system", required = true)
        @PathVariable Long id) {
        log.debug("REST request to delete GardenField: {}", id);
        gardenFieldService.deleteGardenField(id);
        return ResponseEntity.noContent().headers(
            HeaderUtil.createAlert(applicationName, "gardenFieldManagement.deleted", id.toString())).build();
    }

    @ApiOperation(value = "View a page of found fields")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved gardenfields"),
        @ApiResponse(code = 400, message = "BadRequest")
    })
    @GetMapping("/gardenfields/filter")
    public ResponseEntity<Page<GardenFieldDTO>> filterGardenFields(Pageable pageable,
                                                                   @RequestParam(required = false, value = "latitude") Double latitude,
                                                                   @RequestParam(required = false, value = "longitude") Double longitude,
                                                                   @RequestParam(required = false, value = "radiusInKM") Integer radiusInKM,
                                                                   @RequestParam(required = false, value = "minPrice") Double minPricePerMonth,
                                                                   @RequestParam(required = false, value = "maxPrice") Double maxPricePerMonth,
                                                                   @RequestParam(required = false, value = "sizeInM2LowerBound") Double sizeInM2LowerBound,
                                                                   @RequestParam(required = false, value = "sizeInM2UpperBound") Double sizeInM2UpperBound,
                                                                   @RequestParam(required = false, value = "roofed") Boolean roofed,
                                                                   @RequestParam(required = false, value = "leasingTimeFrom") Instant leasingTimeFrom,
                                                                   @RequestParam(required = false, value = "leasingTimeTo") Instant leasingTimeTo,
                                                                   @RequestParam(required = false, value = "keywords") String keywords,
                                                                   @RequestParam(required = false, value = "water") Boolean water,
                                                                   @RequestParam(required = false, value = "electricity") Boolean electricity,
                                                                   @RequestParam(required = false, value = "high") Boolean high,
                                                                   @RequestParam(required = false, value = "glassHouse") Boolean glassHouse) {

        GardenFieldFilterCriteria gardenFieldFilterCriteria = new GardenFieldFilterCriteria(
            new LocationFilterCriteria(latitude, longitude, radiusInKM), minPricePerMonth, maxPricePerMonth,
            sizeInM2LowerBound, sizeInM2UpperBound, roofed, leasingTimeFrom, leasingTimeTo, keywords, water, electricity, high, glassHouse);

        Page<GardenFieldDTO> resultPage = this.gardenFieldService.filterGardenFields(pageable, gardenFieldFilterCriteria);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), resultPage);
        return new ResponseEntity<>(resultPage, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Autocomplete search results for gardenfields")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved autocompletion results")
    })
    @GetMapping("/gardenfields/autocomplete")
    public ResponseEntity<List<GardenFieldDTO>> autocomplete(@RequestParam(value = "partialSearchString") String partialSearchString) {
        List<GardenFieldDTO> resultList = this.gardenFieldService.autocomplete(partialSearchString);
        return new ResponseEntity<>(resultList, HttpStatus.OK);
    }

    @ApiOperation(value = "Get a page of leasings for leasings of a gardenfield")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved leasings for a gardenfield"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 403, message = "No permission for this gardenfield")
    })
    @GetMapping("/gardenfields/{gardenFieldId}/leasings")
    public ResponseEntity<Page<LeasingDTO>> getLeasingsForGardenFields(Pageable pageable, @PathVariable Long gardenFieldId,
                                                                       @RequestParam(required = false, value = "leasingStatus") List<LeasingStatus> leasingStatusList,
                                                                       @RequestParam(required = false) Instant from,
                                                                       @RequestParam(required = false) Instant to,
                                                                       @RequestParam(required = false) LeasingState state) {
        Page<LeasingDTO> leasingPage = leasingService.findByGardenFieldId(pageable, gardenFieldId, leasingStatusList, from, to, state);
        return ResponseEntity.ok().body(leasingPage);
    }

    @ApiOperation(value = "Boundaries for gardenfield filters")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved boundaries for gardenfield filters")
    })
    @GetMapping("/gardenfields/filterBoundaries")
    public ResponseEntity<FilterBoundariesDTO> getFilterBoundaries() {
        return new ResponseEntity<>(this.gardenFieldService.getFilterBoundaries(), HttpStatus.OK);
    }
}
