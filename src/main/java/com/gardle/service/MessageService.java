package com.gardle.service;

import com.gardle.domain.Leasing;
import com.gardle.domain.Message;
import com.gardle.domain.User;
import com.gardle.domain.enumeration.MessageType;
import com.gardle.repository.MessageRepository;
import com.gardle.service.dto.MessageDTO;
import com.gardle.service.exception.MissingAuthorityForMessageThreadServiceException;
import com.gardle.service.mapper.MessageMapper;
import com.google.common.base.CaseFormat;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Message}.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class MessageService {

    private final Logger log = LoggerFactory.getLogger(MessageService.class);

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final SecurityHelperService securityHelperService;


    public MessageDTO save(MessageDTO messageDTO) {
        log.debug("Request to save Message : {}", messageDTO);
        securityHelperService.checkPermission(messageDTO.getUserFrom().getId());
        Message message = messageMapper.toEntity(messageDTO);
        message.setType(MessageType.USER); //So far, only user messages are supported
        setThreadBetweenTwoUsers(message);
        message.setOpened(false);
        message = messageRepository.save(message);
        return messageMapper.toDto(message);
    }

    public void delete(Long id) {
        log.debug("Request to delete Message : {}", id);
        messageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> getLatestThreadMessagesForLoggedInUser(Pageable pageable) {
        log.debug("Request to get latest message in each thread for logged in user");
        Iterator<Sort.Order> argIterator = pageable.getSort().iterator();
        List<Sort.Order> orderList = new LinkedList<>();
        while (argIterator.hasNext()) {
            Sort.Order currentOrder = argIterator.next();
            String camelCaseProperty = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, currentOrder.getProperty());
            Sort.Order snakeCaseOrder = new Sort.Order(currentOrder.getDirection(), camelCaseProperty);
            orderList.add(snakeCaseOrder);
        }
        Sort snakeCaseSort = Sort.by(orderList);
        return messageRepository.getLatestThreadMessagesForUser(securityHelperService.getLoggedInUser(), PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), snakeCaseSort)).map(messageMapper::toDto);
    }

    public List<MessageDTO> getUnreadNotificationsForLoggedInUser() {
        log.debug("Request to get latest unread messages for logged in user per thread");
        return messageRepository.getUnreadNotificationsForUser(securityHelperService.getLoggedInUser()).stream()
            .map(messageMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateThreadToOpenedForUser(UUID thread) {
        log.debug("Request to open messages in thread: {}", thread);
        if (messageRepository.canUserAccessThread(securityHelperService.getLoggedInUser(), thread)) {
            messageRepository.updateThreadToOpenedForUser(securityHelperService.getLoggedInUser(), thread);
        } else {
            throw new MissingAuthorityForMessageThreadServiceException();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void updateAllSystemNotificationsToOpenedForLoggedInUser() {
        log.debug("Request to open all system messages for logged-in user");
        messageRepository.updateSystemMessagesToOpenedForUser(securityHelperService.getLoggedInUser());
    }

    @Transactional(readOnly = true)
    public Page<MessageDTO> findByThread(UUID thread, Pageable pageable) {
        log.debug("Request to find all messages in thread : {}", thread);
        if (messageRepository.canUserAccessThread(securityHelperService.getLoggedInUser(), thread)) {
            return messageRepository.getByThread(thread, pageable).map(messageMapper::toDto);
        } else {
            throw new MissingAuthorityForMessageThreadServiceException();
        }
    }

    public MessageDTO createLeasingNotification(Leasing leasing) {
        log.debug("Request to create SYSTEM_LEASING_OPEN notification for leasing: {}", leasing.getId());
        Message notification = new Message();
        User userFrom = null;
        User userTo = null;
        MessageType msgType = null;
        switch (leasing.getStatus()){
            case OPEN:
                userFrom = leasing.getUser();
                userTo = leasing.getGardenField().getOwner();
                msgType = MessageType.SYSTEM_LEASING_OPEN;
                break;
            case CANCELLED:
                userFrom = leasing.getUser();
                userTo = leasing.getGardenField().getOwner();
                msgType = MessageType.SYSTEM_LEASING_CANCELLED;
                break;
            case RESERVED:
                userFrom = leasing.getGardenField().getOwner();
                userTo = leasing.getUser();
                msgType = MessageType.SYSTEM_LEASING_RESERVED;
                break;
            case REJECTED:
                userFrom = leasing.getGardenField().getOwner();
                userTo = leasing.getUser();
                msgType = MessageType.SYSTEM_LEASING_REJECTED;
                break;
            default:
                break;
        }
        notification.setLeasing(leasing);
        notification.setUserFrom(userFrom);
        notification.setUserTo(userTo);
        setThreadBetweenTwoUsers(notification);
        notification.setOpened(false);
        notification.setType(msgType);
        notification = messageRepository.save(notification);
        return messageMapper.toDto(notification);
    }

    private void setThreadBetweenTwoUsers(Message message) {
        UUID existingThread = messageRepository.threadBetweenTwoUsers(message.getUserFrom(), message.getUserTo());
        message.setThread(existingThread == null ? UUID.randomUUID() : existingThread);
    }
}
