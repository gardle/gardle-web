package com.gardle.service;


import com.gardle.domain.GardenField;
import com.gardle.domain.User;
import com.gardle.repository.GardenFieldRepository;
import com.gardle.repository.UserRepository;
import com.gardle.security.SecurityUtils;
import com.gardle.service.exception.GardenFieldUnknownServiceException;
import com.gardle.service.exception.MissingAuthorityServiceException;
import com.gardle.service.exception.MissingPermissionServiceException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SecurityHelperService {

    private final Logger log = LoggerFactory.getLogger(SecurityHelperService.class);

    private final UserRepository userRepository;
    private final GardenFieldRepository gardenFieldRepository;

    public User getLoggedInUser() {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(MissingAuthorityServiceException::new);
        return userRepository.findOneByLogin(login).orElseThrow(MissingAuthorityServiceException::new);
    }

    public void checkAuthority(Long userId) {
        User user = getLoggedInUser();
        if (!user.getId().equals(userId)) {
            throw new MissingAuthorityServiceException();
        }
    }

    public void checkPermission(Long userId) {
        User user = getLoggedInUser();
        if (!user.getId().equals(userId)) {
            throw new MissingPermissionServiceException("User has not the permission for this action");
        }
    }

    public void checkAuthorityByGardenFieldId(Long gardenFieldId) {
        User user = getLoggedInUser();
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(() -> new GardenFieldUnknownServiceException("Unknown Gardenfield"));
        if (!user.getId().equals(gardenField.getOwner().getId())) {
            throw new MissingAuthorityServiceException();
        }
    }

    public void checkPermissionByGardenFieldId(Long gardenFieldId) {
        User user = getLoggedInUser();
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(() -> new GardenFieldUnknownServiceException("Unknown Gardenfield"));
        if (!user.getId().equals(gardenField.getOwner().getId())) {
            throw new MissingPermissionServiceException("User has not the permission for the requested gardenfield");
        }
    }

    public boolean loggedInUserIsOwnerOfGardenField(Long gardenFieldId) {
        User user = getLoggedInUser();
        GardenField gardenField = gardenFieldRepository.findById(gardenFieldId)
            .orElseThrow(() -> new GardenFieldUnknownServiceException("Unknown Gardenfield"));
        return user.getId().equals(gardenField.getOwner().getId());
    }

}
