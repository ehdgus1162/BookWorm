package com.bookworm.domain.common;


import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "library.policy")
@Validated
@Getter @Setter
public class LibraryPolicy {

    @Min(value = 1, message = "기본 대출 기간은 최소 1일 이상이어야 합니다")
    @Max(value = 90, message = "기본 대출 기간은 최대 90일을 초과할 수 없습니다")
    private int defaultLoanPeriodDays = 14;

    @Min(value = 0, message = "최대 연장 횟수는 0 이상이어야 합니다")
    @Max(value = 10, message = "최대 연장 횟수는 10회를 초과할 수 없습니다")
    private int maxExtensionCount = 2;

    @Min(value = 0, message = "일일 연체료는 0원 이상이어야 합니다")
    @Max(value = 10000, message = "일일 연체료는 10,000원을 초과할 수 없습니다")
    private int dailyOverdueFine = 100;

    @Min(value = 1, message = "예약 만료 시간은 최소 1시간 이상이어야 합니다")
    @Max(value = 168, message = "예약 만료 시간은 최대 168시간(7일)을 초과할 수 없습니다")
    private int reservationExpireHours = 48;

    @Min(value = 1, message = "재고 부족 임계값은 최소 1 이상이어야 합니다")
    @Max(value = 100, message = "재고 부족 임계값은 최대 100을 초과할 수 없습니다")
    private int lowStockThreshold = 2;


}
