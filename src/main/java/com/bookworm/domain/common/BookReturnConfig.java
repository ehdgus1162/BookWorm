package com.bookworm.domain.common;

import com.bookworm.domain.common.TimeProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;

/**
 * 도서 반납 관련 설정
 */
@Configuration
@EnableScheduling  // 스케줄링 기능 활성화
public class BookReturnConfig {

    /**
     * 시스템 시간 제공자 (운영 환경용)
     */
    @Bean
    @Primary
    public TimeProvider systemTimeProvider() {
        return new TimeProvider(Clock.systemDefaultZone());
    }

    /**
     * 테스트용 고정 시간 제공자 (필요시 사용)
     */
    @Bean("fixedTimeProvider")
    public TimeProvider fixedTimeProvider() {
        // 테스트용 고정 시간 (예: 2024-01-01)
        Clock fixedClock = Clock.fixed(
                java.time.Instant.parse("2024-01-01T00:00:00Z"),
                java.time.ZoneId.systemDefault()
        );
        return new TimeProvider(fixedClock);
    }
}
