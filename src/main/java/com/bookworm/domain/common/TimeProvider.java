package com.bookworm.domain.common;

import lombok.Getter;
import org.springframework.stereotype.Component;

import org.springframework.stereotype.Component;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
@Getter
public class TimeProvider {

    private final Clock clock;

    public TimeProvider() {
        this.clock = Clock.systemDefaultZone();
    }

    public TimeProvider(Clock clock) {
        this.clock = clock;
    }

    public LocalDate currentDate() {
        return LocalDate.now(clock);
    }

    public LocalDateTime currentDateTime() {
        return LocalDateTime.now(clock);
    }

}