package com.gardle.web.rest;

import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.LeasingQueryService;
import com.gardle.service.LeasingService;
import com.gardle.service.dto.LeasingCriteria;
import com.gardle.service.dto.leasing.LeasingDTO;
import com.gardle.service.dto.leasing.LeasingDateRangeDTO;
import com.gardle.service.dto.leasing.UpdatingLeasingDTO;
import com.gardle.web.rest.errors.BadRequestException;
import com.gardle.web.rest.errors.GardleErrorKey;
import io.github.jhipster.web.util.HeaderUtil;
import io.github.jhipster.web.util.PaginationUtil;
import io.github.jhipster.web.util.ResponseUtil;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * REST controller for managing {@link com.gardle.domain.Leasing}.
 */
@Api(value = "Endpoint for Leasings")
@RestController
@RequestMapping("/api/v1")
public class LeasingController {

    private final Logger log = LoggerFactory.getLogger(LeasingController.class);

    private static final String ENTITY_NAME = "leasing";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final LeasingService leasingService;

    private final LeasingQueryService leasingQueryService;

    public LeasingController(LeasingService leasingService, LeasingQueryService leasingQueryService) {
        this.leasingService = leasingService;
        this.leasingQueryService = leasingQueryService;
    }

    /**
     * {@code PUT  /leasings} : Updates an existing leasing.
     *
     * @param leasingDTO the leasingDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated leasingDTO,
     * or with status {@code 400 (Bad Request)} if the leasingDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the leasingDTO couldn't be updated.
     */
    @ApiOperation(value = "update a leasing")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created a leasing"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "You are not authorized to create a leasing"),
        @ApiResponse(code = 409, message = "There are overlapping leasings for the time frame of the leasing to create")
    })
    @PutMapping("/leasings")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<LeasingDTO> updateLeasing(@ApiParam(value = "leasing data to update")
                                                    @NotNull @RequestBody @Valid UpdatingLeasingDTO leasingDTO) throws BadRequestException {
        log.debug("REST request to update Leasing : {}", leasingDTO);
        if (leasingDTO.getId() == null) {
            throw new BadRequestException(GardleErrorKey.LEASING_INVALID_ID);
        }
        LeasingDTO result = leasingService.updateLeasing(leasingDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, leasingDTO.getId().toString()))
            .body(result);
    }

    /**
     * {@code GET  /leasings} : get all the leasings.
     *
     * @param pageable    the pagination information.
     * @param queryParams a {@link MultiValueMap} query parameters.
     * @param uriBuilder  a {@link UriComponentsBuilder} URI builder.
     * @param criteria    the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of leasings in body.
     */
    @ApiOperation(value = "get a list of leasings")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved leasings")
    })
    @GetMapping("/leasings")
    public ResponseEntity<Page<LeasingDTO>> getAllLeasings(LeasingCriteria criteria,
                                                           Pageable pageable,
                                                           @RequestParam MultiValueMap<String, String> queryParams,
                                                           UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get Leasings by criteria: {}", criteria);
        Page<LeasingDTO> page = leasingQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder.queryParams(queryParams), page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    /**
     * {@code GET  /leasings/:id} : get the "id" leasing.
     *
     * @param id the id of the leasingDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the leasingDTO, or with status {@code 404 (Not Found)}.
     */
    @ApiOperation(value = "get a single leasing")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved leasing"),
        @ApiResponse(code = 401, message = "You are not authorized to view a leasing"),
        @ApiResponse(code = 403, message = "You have not the permission to view this leasing"),
        @ApiResponse(code = 404, message = "Leasing not available")
    })
    @GetMapping("/leasings/{id}")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<LeasingDTO> getLeasing(@ApiParam(value = "leasing to get", required = true) @PathVariable Long id) {
        log.debug("REST request to get Leasing : {}", id);
        Optional<LeasingDTO> leasingDTO = leasingService.findOne(id);
        return ResponseUtil.wrapOrNotFound(leasingDTO);
    }

    @ApiOperation(value = "Get leased date ranges for a specific gardenField")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved date ranges"),
        @ApiResponse(code = 409, message = "Gardenfield does not exist")
    })
    @GetMapping("/leasings/{gardenFieldId}/leasedDateRanges")
    public ResponseEntity<List<LeasingDateRangeDTO>> getLeasedDateRanges(@ApiParam(value = "Gardenfield Id for which the leased date ranges should be returned", required = true)
                                                                         @PathVariable Long gardenFieldId,
                                                                         @ApiParam(value = "from boundary for ranges")
                                                                         @RequestParam(required = false) Instant from,
                                                                         @ApiParam(value = "to boundary for ranges")
                                                                         @RequestParam(required = false) Instant to) {
        log.debug("REST request to get leased date ranges : {}", gardenFieldId);
        List<LeasingDateRangeDTO> leasingDateRanges = leasingService.getLeasedDateRanges(gardenFieldId, from, to);
        return ResponseEntity.ok().body(leasingDateRanges);
    }

}
