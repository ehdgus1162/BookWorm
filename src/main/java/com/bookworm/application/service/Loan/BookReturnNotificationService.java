package com.bookworm.application.service.Loan;

import com.bookworm.application.dto.LoanResponse;
import com.bookworm.application.service.Loan.BookReturnService;
import com.bookworm.domain.common.TimeProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 반납 알림 서비스
 *
 * 역할:
 * 1. 연체 알림 발송
 * 2. 반납 예정 알림 발송
 * 3. 정기적인 알림 작업 스케줄링
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookReturnNotificationService {

    private final BookReturnService bookReturnService;
    private final TimeProvider timeProvider;
    // private final EmailService emailService; // 실제 구현 시 이메일 서비스 추가
    // private final SmsService smsService;     // 실제 구현 시 SMS 서비스 추가

    /**
     * 매일 오전 9시에 연체 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void sendOverdueNotifications() {
        log.info("연체 알림 작업 시작");

        try {
            List<LoanResponse> overdueLoans = bookReturnService.findOverdueLoans();

            if (overdueLoans.isEmpty()) {
                log.info("연체된 대출이 없습니다.");
                return;
            }

            log.info("연체된 대출 {} 건에 대해 알림을 발송합니다.", overdueLoans.size());

            for (LoanResponse loan : overdueLoans) {
                sendOverdueNotification(loan);
            }

            log.info("연체 알림 발송 완료. 발송 건수: {}", overdueLoans.size());

        } catch (Exception e) {
            log.error("연체 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 매일 오전 10시에 반납 예정 알림 발송 (3일 전)
     */
    @Scheduled(cron = "0 0 10 * * *")
    public void sendUpcomingDueNotifications() {
        log.info("반납 예정 알림 작업 시작");

        try {
            List<LoanResponse> upcomingLoans = bookReturnService.findUpcomingDueLoans(3);

            if (upcomingLoans.isEmpty()) {
                log.info("곧 반납 예정인 대출이 없습니다.");
                return;
            }

            log.info("곧 반납 예정인 대출 {} 건에 대해 알림을 발송합니다.", upcomingLoans.size());

            for (LoanResponse loan : upcomingLoans) {
                sendUpcomingDueNotification(loan);
            }

            log.info("반납 예정 알림 발송 완료. 발송 건수: {}", upcomingLoans.size());

        } catch (Exception e) {
            log.error("반납 예정 알림 발송 중 오류 발생", e);
        }
    }

    /**
     * 개별 연체 알림 발송
     */
    private void sendOverdueNotification(LoanResponse loan) {
        try {
            String message = String.format(
                    "안녕하세요, %s님. 대출하신 도서 '%s'가 %d일 연체되었습니다. 빠른 시일 내에 반납해 주시기 바랍니다.",
                    loan.userName(),
                    loan.bookTitle(),
                    loan.overdueDays()
            );

            // 실제 구현 시 이메일/SMS 발송
            log.info("연체 알림 발송: 사용자={}, 도서={}, 연체일수={}",
                    loan.userName(), loan.bookTitle(), loan.overdueDays());

            // emailService.send(loan.userEmail(), "도서 연체 알림", message);

        } catch (Exception e) {
            log.error("연체 알림 발송 실패: 대출 ID={}", loan.id(), e);
        }
    }

    /**
     * 개별 반납 예정 알림 발송
     */
    private void sendUpcomingDueNotification(LoanResponse loan) {
        try {
            String message = String.format(
                    "안녕하세요, %s님. 대출하신 도서 '%s'의 반납 예정일이 %d일 후입니다. 연체되지 않도록 미리 반납해 주시기 바랍니다.",
                    loan.userName(),
                    loan.bookTitle(),
                    loan.daysUntilDue()
            );

            // 실제 구현 시 이메일/SMS 발송
            log.info("반납 예정 알림 발송: 사용자={}, 도서={}, 남은일수={}",
                    loan.userName(), loan.bookTitle(), loan.daysUntilDue());

            // emailService.send(loan.userEmail(), "도서 반납 예정 알림", message);

        } catch (Exception e) {
            log.error("반납 예정 알림 발송 실패: 대출 ID={}", loan.id(), e);
        }
    }

    /**
     * 수동 알림 발송 (관리자용)
     */
    public void sendManualNotification(Long loanId, String customMessage) {
        log.info("수동 알림 발송 요청: 대출 ID={}", loanId);

        try {
            LoanResponse loan = bookReturnService.findLoanDetails(loanId);

            String message = customMessage != null ? customMessage :
                    String.format("안녕하세요, %s님. 대출하신 도서 '%s'에 대한 알림입니다.",
                            loan.userName(), loan.bookTitle());

            log.info("수동 알림 발송: 사용자={}, 도서={}, 메시지={}",
                    loan.userName(), loan.bookTitle(), message);

            // emailService.send(loan.userEmail(), "도서 대출 알림", message);

        } catch (Exception e) {
            log.error("수동 알림 발송 실패: 대출 ID={}", loanId, e);
            throw new RuntimeException("알림 발송에 실패했습니다.", e);
        }
    }
}