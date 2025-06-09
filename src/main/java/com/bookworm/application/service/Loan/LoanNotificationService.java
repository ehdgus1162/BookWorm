package com.bookworm.application.service.Loan;

import com.bookworm.domain.entity.BookLoan;
import com.bookworm.infrastructure.repository.BookLoanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 대출 알림 서비스
 * - 반납 예정일 알림
 * - 연체 알림
 * - 대출 완료 알림
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LoanNotificationService {

    private final BookLoanRepository bookLoanRepository;
    // private final EmailNotificationService emailNotificationService; // 실제 구현 시 필요

    /**
     * 대출 완료 알림 (비동기)
     */
    @Async
    public void sendLoanConfirmationNotification(BookLoan loan) {
        log.info("대출 완료 알림 발송 - 사용자: {}, 도서: '{}'",
                loan.getUser().getFullName(), loan.getBook().getTitle().getValue());

        // 실제 이메일 발송 로직
        try {
            String userEmail = loan.getUser().getEmail().getValue();
            String message = String.format(
                    "안녕하세요 %s님, '%s' 도서가 성공적으로 대출되었습니다. 반납 예정일: %s",
                    loan.getUser().getFullName(),
                    loan.getBook().getTitle().getValue(),
                    loan.getLoanPeriod().getDueDate()
            );

            // emailNotificationService.sendEmail(userEmail, "대출 완료 알림", message);
            log.info("대출 완료 알림 발송 완료 - {}", userEmail);

        } catch (Exception e) {
            log.error("대출 완료 알림 발송 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 반납 예정일 알림 (배치 처리)
     */
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    @Transactional(readOnly = true)
    public void sendDueReminderNotifications() {
        log.info("반납 예정일 알림 배치 시작");

        try {
            // 3일 후 반납 예정인 도서
            List<BookLoan> upcomingDueLoans = bookLoanRepository.findUpcomingDueLoans(3);

            for (BookLoan loan : upcomingDueLoans) {
                sendDueReminderNotification(loan);
            }

            log.info("반납 예정일 알림 배치 완료 - 발송 건수: {}", upcomingDueLoans.size());
        } catch (Exception e) {
            log.error("반납 예정일 알림 배치 처리 중 오류 발생", e);
        }
    }

    /**
     * 연체 알림 (배치 처리)
     */
    @Scheduled(cron = "0 0 10 * * *") // 매일 오전 10시
    @Transactional(readOnly = true)
    public void sendOverdueNotifications() {
        log.info("연체 알림 배치 시작");

        try {
            List<BookLoan> overdueLoans = bookLoanRepository.findOverdueLoans();

            for (BookLoan loan : overdueLoans) {
                sendOverdueNotification(loan);
            }

            log.info("연체 알림 배치 완료 - 발송 건수: {}", overdueLoans.size());
        } catch (Exception e) {
            log.error("연체 알림 배치 처리 중 오류 발생", e);
        }
    }

    /**
     * 개별 반납 예정일 알림 발송
     */
    @Async
    public void sendDueReminderNotification(BookLoan loan) {
        try {
            String userEmail = loan.getUser().getEmail().getValue();
            String message = String.format(
                    "안녕하세요 %s님, 대출하신 '%s' 도서의 반납 예정일이 %d일 남았습니다. 반납 예정일: %s",
                    loan.getUser().getFullName(),
                    loan.getBook().getTitle().getValue(),
                    loan.getDaysUntilDue(),
                    loan.getLoanPeriod().getDueDate()
            );

            // emailNotificationService.sendEmail(userEmail, "반납 예정일 알림", message);
            log.info("반납 예정일 알림 발송 완료 - 사용자: {}, 도서: '{}'",
                    loan.getUser().getFullName(), loan.getBook().getTitle().getValue());

        } catch (Exception e) {
            log.error("반납 예정일 알림 발송 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 개별 연체 알림 발송
     */
    @Async
    public void sendOverdueNotification(BookLoan loan) {
        try {
            String userEmail = loan.getUser().getEmail().getValue();
            String message = String.format(
                    "안녕하세요 %s님, 대출하신 '%s' 도서가 %d일 연체되었습니다. 빠른 시일 내에 반납해 주세요.",
                    loan.getUser().getFullName(),
                    loan.getBook().getTitle().getValue(),
                    loan.getLoanPeriod().getOverdueDays()
            );

            // emailNotificationService.sendEmail(userEmail, "연체 알림", message);
            log.info("연체 알림 발송 완료 - 사용자: {}, 도서: '{}'",
                    loan.getUser().getFullName(), loan.getBook().getTitle().getValue());

        } catch (Exception e) {
            log.error("연체 알림 발송 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 반납 완료 알림
     */
    @Async
    public void sendReturnConfirmationNotification(BookLoan loan) {
        log.info("반납 완료 알림 발송 - 사용자: {}, 도서: '{}'",
                loan.getUser().getFullName(), loan.getBook().getTitle().getValue());

        try {
            String userEmail = loan.getUser().getEmail().getValue();
            String message = String.format(
                    "안녕하세요 %s님, '%s' 도서가 성공적으로 반납되었습니다. 이용해 주셔서 감사합니다.",
                    loan.getUser().getFullName(),
                    loan.getBook().getTitle().getValue()
            );

            // emailNotificationService.sendEmail(userEmail, "반납 완료 알림", message);
            log.info("반납 완료 알림 발송 완료 - {}", userEmail);

        } catch (Exception e) {
            log.error("반납 완료 알림 발송 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }

    /**
     * 대출 연장 알림
     */
    @Async
    public void sendExtensionConfirmationNotification(BookLoan loan, int extensionDays) {
        log.info("대출 연장 알림 발송 - 사용자: {}, 도서: '{}', 연장 일수: {}일",
                loan.getUser().getFullName(), loan.getBook().getTitle().getValue(), extensionDays);

        try {
            String userEmail = loan.getUser().getEmail().getValue();
            String message = String.format(
                    "안녕하세요 %s님, '%s' 도서의 대출이 %d일 연장되었습니다. 새로운 반납 예정일: %s",
                    loan.getUser().getFullName(),
                    loan.getBook().getTitle().getValue(),
                    extensionDays,
                    loan.getLoanPeriod().getDueDate()
            );

            // emailNotificationService.sendEmail(userEmail, "대출 연장 알림", message);
            log.info("대출 연장 알림 발송 완료 - {}", userEmail);

        } catch (Exception e) {
            log.error("대출 연장 알림 발송 실패 - 대출 ID: {}", loan.getId(), e);
        }
    }
}