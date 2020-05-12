package com.gardle;

import com.gardle.domain.Leasing;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class LeasingTest {

    @Test
    public void testGetRangeInDays_100() {
        Leasing leasing = new Leasing();
        leasing.setFrom(Instant.now());
        leasing.setTo(leasing.getFrom().plus(99, ChronoUnit.DAYS));
        assertThat(leasing.getPeriodInDays(), is(100L));
    }

    @Test
    public void testGetRangeInDays_2() {
        Leasing leasing = new Leasing();
        leasing.setFrom(Instant.now());
        leasing.setTo(leasing.getFrom().plus(1, ChronoUnit.DAYS));
        assertThat(leasing.getPeriodInDays(), is(2L));
    }

}
