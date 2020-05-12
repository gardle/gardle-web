package com.gardle.web.rest;

import com.gardle.domain.enumeration.LeasingState;
import com.gardle.domain.enumeration.LeasingStatus;
import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.GardenFieldService;
import com.gardle.service.LeasingService;
import com.gardle.service.dto.GardenFieldDTO;
import com.gardle.service.dto.leasing.LeasingDTO;
import io.github.jhipster.web.util.PaginationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.Instant;
import java.util.List;

@Api(value = "Endpoint for users gardenfields")
@RestController
@RequestMapping("/api/v1/gardenfields")
@RequiredArgsConstructor
public class UserGardenFieldController {

    private final Logger log = LoggerFactory.getLogger(GardenFieldController.class);
    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final GardenFieldService gardenFieldService;
    private final LeasingService leasingService;

    @ApiOperation(value = "Get a list of user gardenfields")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved users gardenfields"),
        @ApiResponse(code = 401, message = "Unauthenticated")
    })
    @GetMapping("/user")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Page<GardenFieldDTO>> getAllUserGardenfields(Pageable pageable) {
        Page<GardenFieldDTO> userGardenfieldsPage = gardenFieldService.getAllUserGardenfields(pageable);

        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), userGardenfieldsPage);
        return new ResponseEntity<>(userGardenfieldsPage, headers, HttpStatus.OK);
    }

    @ApiOperation(value = "Get a page of leasings for all gardenfields owned by the logged in user")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved leasings for a user"),
        @ApiResponse(code = 401, message = "Unauthenticated")
    })
    @GetMapping("/user/leasings")
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    public ResponseEntity<Page<LeasingDTO>> getLeasingsForGardenFields(Pageable pageable,
                                                                       @RequestParam(required = false, value = "leasingStatus") List<LeasingStatus> leasingStatusList,
                                                                       @RequestParam(required = false) Instant from,
                                                                       @RequestParam(required = false) Instant to,
                                                                       @RequestParam(required = false) LeasingState state) {
        Page<LeasingDTO> leasingPage = leasingService.findByOwner(pageable, leasingStatusList, from, to, state);
        return ResponseEntity.ok().body(leasingPage);
    }

}
