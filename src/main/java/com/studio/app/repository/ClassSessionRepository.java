package com.studio.app.repository;

import com.studio.app.entity.ClassSession;
import com.studio.app.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link ClassSession} entities.
 */
@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, Long> {

    /** All non-deleted sessions for a student ordered by date and time. */
    List<ClassSession> findByStudentIdAndDeletedFalseOrderByClassDateAscStartTimeAsc(Long studentId);

    /** Non-deleted sessions for a student from the given date (inclusive). */
    List<ClassSession> findByStudentIdAndClassDateGreaterThanEqualAndDeletedFalseOrderByClassDateAscStartTimeAsc(
            Long studentId, LocalDate from);

    /** Non-deleted sessions for a student up to the given date (inclusive). */
    List<ClassSession> findByStudentIdAndClassDateLessThanEqualAndDeletedFalseOrderByClassDateAscStartTimeAsc(
            Long studentId, LocalDate to);

    /** Non-deleted sessions for a student within an inclusive date range. */
    @Query(value = """
            SELECT cs.*
            FROM studio.class_sessions cs
            WHERE cs.student_id = :studentId
              AND cs.deleted = false
              AND cs.class_date >= :from
              AND cs.class_date <= :to
            ORDER BY cs.class_date, cs.start_time
            """, nativeQuery = true)
    List<ClassSession> findByStudentIdAndDateRange(
            @Param("studentId") Long studentId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Calendar query — all non-deleted sessions across all students
     * within a date range, ordered chronologically.
     */
    @Query(value = """
            SELECT cs.*
            FROM studio.class_sessions cs
            JOIN studio.students s ON s.id = cs.student_id
            WHERE cs.deleted = false
              AND s.deleted = false
              AND cs.class_date >= :from
              AND cs.class_date <= :to
            ORDER BY cs.class_date, cs.start_time
            """, nativeQuery = true)
    List<ClassSession> findCalendarSessions(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** All paid sessions for a student (for payment summary). */
    List<ClassSession> findByStudentIdAndPaymentStatusAndDeletedFalse(
            Long studentId, PaymentStatus paymentStatus);

    /** Finds a non-deleted session by its ID. */
    Optional<ClassSession> findByIdAndDeletedFalse(Long id);

    /** All non-deleted sessions linked to a specific package purchase. */
    List<ClassSession> findByPackagePurchaseIdAndDeletedFalse(Long packagePurchaseId);

    /**
     * Finds all PAID (per-class) sessions within a date range, excluding PACKAGE payments.
     * Used for earnings aggregation.
     */
    @Query(value = """
            SELECT cs.*
            FROM studio.class_sessions cs
            JOIN studio.students s ON s.id = cs.student_id
            WHERE cs.deleted = false
              AND s.deleted = false
              AND cs.payment_status = 'PAID'
              AND cs.class_date >= :from
              AND cs.class_date <= :to
            ORDER BY cs.class_date, cs.start_time
            """, nativeQuery = true)
    List<ClassSession> findPaidSessionsByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Finds per-class sessions in a date range that are collectible or already earned.
     * Includes PAID and UNPAID sessions, excludes CANCELLED and PACKAGE-covered sessions.
     */
    @Query(value = """
            SELECT cs.*
            FROM studio.class_sessions cs
            JOIN studio.students s ON s.id = cs.student_id
            WHERE cs.deleted = false
              AND s.deleted = false
              AND cs.payment_status IN ('PAID', 'UNPAID')
              AND cs.status <> 'CANCELLED'
              AND cs.class_date >= :from
              AND cs.class_date <= :to
            ORDER BY cs.class_date, cs.start_time
            """, nativeQuery = true)
    List<ClassSession> findCollectiblePerClassSessionsByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /**
     * Finds per-class sessions in a date range that represent potential earnings,
     * including cancelled sessions. Includes PAID and UNPAID sessions,
     * excludes PACKAGE-covered sessions.
     */
    @Query(value = """
            SELECT cs.*
            FROM studio.class_sessions cs
            JOIN studio.students s ON s.id = cs.student_id
            WHERE cs.deleted = false
              AND s.deleted = false
              AND cs.payment_status IN ('PAID', 'UNPAID')
              AND cs.class_date >= :from
              AND cs.class_date <= :to
            ORDER BY cs.class_date, cs.start_time
            """, nativeQuery = true)
    List<ClassSession> findPotentialPerClassSessionsIncludingCancellationsByDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    /** True when student has at least one unpaid non-cancelled session that has already happened. */
    @Query(value = """
            SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END
            FROM studio.class_sessions cs
            JOIN studio.students s ON s.id = cs.student_id
            WHERE cs.deleted = false
              AND s.deleted = false
              AND cs.student_id = :studentId
              AND cs.payment_status = 'UNPAID'
              AND cs.status <> 'CANCELLED'
              AND (
                    cs.class_date < :localDate
                    OR (cs.class_date = :localDate AND cs.start_time <= :localTime)
                  )
            """, nativeQuery = true)
    boolean existsUnpaidOccurredSessionForStudent(
            @Param("studentId") Long studentId,
            @Param("localDate") LocalDate localDate,
            @Param("localTime") java.time.LocalTime localTime);
}
