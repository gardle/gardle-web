package com.gardle.validator;

import com.gardle.domain.Leasing;
import com.gardle.service.LeasingService;
import com.gardle.service.dto.leasing.CreatorLeasingDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class LeasingDataValidator implements ConstraintValidator<ValidLeasingData, CreatorLeasingDTO> {
    private static final Logger LOG = LoggerFactory.getLogger(LeasingDataValidator.class);
    // validation constants
    private static final int AMOUNT = 14;
    private static final ChronoUnit UNIT = ChronoUnit.DAYS;
    @Autowired
    private LeasingService leasingService;

    @Override
    public boolean isValid(CreatorLeasingDTO creatorLeasingDTO, ConstraintValidatorContext constraintValidatorContext) {
        boolean res = true;
        List<String> violations = new ArrayList<>();
        LOG.debug("Validating CreatorLeasingDTO {}", creatorLeasingDTO);

        if (creatorLeasingDTO == null) {
            LOG.debug("creatorLeasingDTO is null");
            return false;
        }
        if (creatorLeasingDTO.getTo() != null && creatorLeasingDTO.getFrom() != null) {
            checkFromAndToDate(creatorLeasingDTO, violations);
            checkOverlappings(creatorLeasingDTO, violations);
        } else {
            LOG.debug("from or to null");
            violations.add("{from or to null}");
        }
        checkGardenFieldName(creatorLeasingDTO, violations);

        if (!violations.isEmpty()) {
            res = false;
            buildConstraintViolationContext(violations, constraintValidatorContext);
        }
        return res;
    }

    private void checkGardenFieldName(CreatorLeasingDTO creatorLeasingDTO, List<String> violations) {
        if (StringUtils.isEmpty(creatorLeasingDTO.getGardenFieldName())) {
            violations.add("{gardenField name is emtpy}");
        }
    }

    private void checkOverlappings(final CreatorLeasingDTO creatorLeasingDTO, final List<String> violations) {
        if (creatorLeasingDTO.getGardenFieldId() == null) {
            violations.add("{gardenfieldId is null}");
            return;
        }
        List<Leasing> overlappings = leasingService.getOverLappingLeasingsForGardenfieldAndUserInInterval(
            creatorLeasingDTO.getGardenFieldId(),
            creatorLeasingDTO.getFrom(),
            creatorLeasingDTO.getTo()
        );

        if (!overlappings.isEmpty()) {
            LOG.debug("overlappings detected: {}", overlappings);
            violations.add("{leasing overlaps with other leasings}");
        }
    }

    private void checkFromAndToDate(CreatorLeasingDTO creatorLeasingDTO, List<String> violations) {
        if (!creatorLeasingDTO.getFrom().isBefore(creatorLeasingDTO.getTo())) {
            LOG.debug("from is before to");
            violations.add("{from date is before to date}");
        }
        if (!creatorLeasingDTO.getFrom().isAfter(Instant.now().plus(AMOUNT, UNIT))) {
            LOG.debug("from is in less than two weeks from now - not possible");
            violations.add("{from is in less than two weeks from now - not possible}");
        }
    }

    private void buildConstraintViolationContext(List<String> msgs, ConstraintValidatorContext constraintValidatorContext) {
        if (msgs.isEmpty()) {
            return;
        }
        constraintValidatorContext.disableDefaultConstraintViolation();
        for (String violation : msgs) {
            constraintValidatorContext.buildConstraintViolationWithTemplate(violation).addConstraintViolation();
        }
    }
}
