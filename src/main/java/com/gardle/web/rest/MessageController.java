package com.gardle.web.rest;

import com.gardle.security.AuthoritiesConstants;
import com.gardle.service.MessageService;
import com.gardle.service.dto.MessageDTO;
import io.github.jhipster.web.util.PaginationUtil;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
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
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing {@link com.gardle.domain.Message}.
 */
@RestController
@RequestMapping("/api/v1")
public class MessageController {

    private final Logger log = LoggerFactory.getLogger(MessageController.class);


    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @ApiOperation(value = "Create a message")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Successfully created a message"),
        @ApiResponse(code = 400, message = "Request is not well formed, maybe missing field"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 403, message = "You are not authorized to create a message")
    })
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @PostMapping("/messages")
    public ResponseEntity<MessageDTO> createMessage(@RequestBody @Valid MessageDTO messageDTO) {
        log.debug("REST request to save Message : {}", messageDTO);
        MessageDTO result = messageService.save(messageDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @ApiOperation(value = "Get all messages in chosen thread")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved messages"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 403, message = "You are not authorized to access this thread")
    })
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @GetMapping("/messages/thread/{id}")
    public ResponseEntity<Page<MessageDTO>> getMessagesByThread(@PathVariable("id") UUID threadId, Pageable pageable, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get Message with thread id : {}", threadId);
        Page<MessageDTO> page = messageService.findByThread(threadId, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder, page);
        return ResponseEntity.ok().headers(headers).body(page);

    }

    @ApiOperation(value = "View a page of available messages")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved messages"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 403, message = "You are not authorized to read a message")
    })
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @GetMapping("/messages")
    public ResponseEntity<Page<MessageDTO>> getLatestMessageInThreads(Pageable pageable, UriComponentsBuilder uriBuilder) {
        log.debug("REST request to get all new Messages");
        Page<MessageDTO> page = messageService.getLatestThreadMessagesForLoggedInUser(pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(uriBuilder, page);
        return ResponseEntity.ok().headers(headers).body(page);
    }

    @ApiOperation(value = "Set every unopened message in thread addressed to the logged-in user to opened")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully opened messages"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 403, message = "You are not authorized to open messages in this thread")
    })
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @PutMapping("/messages/thread/{id}")
    public ResponseEntity<Void> updateThreadToOpenedForUser(@PathVariable("id") UUID threadId) {
        log.debug("REST request to open Messages in a thread");
        messageService.updateThreadToOpenedForUser(threadId);
        return ResponseEntity.ok().build();
    }

    @ApiOperation(value = "Set every unopened system message addressed to the logged-in user to opened")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully opened messages"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
    })
    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @PutMapping("/messages/unread/system")
    public ResponseEntity<Void> updateSystemMessagesToOpenedForLoggedInUser() {
        log.debug("REST request to open system messages for logged in user");
        messageService.updateAllSystemNotificationsToOpenedForLoggedInUser();
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole(\"" + AuthoritiesConstants.USER + "\")")
    @GetMapping("/messages/unread/")
    public ResponseEntity<List<MessageDTO>> getUnreadNotificationsForLoggedInUser() {
        log.debug("REST request for unread notifications");
        List<MessageDTO> result = messageService.getUnreadNotificationsForLoggedInUser();
        return ResponseEntity.ok(result);
    }

}
