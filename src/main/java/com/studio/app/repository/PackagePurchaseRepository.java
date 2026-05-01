package com.studio.app.repository;

import com.studio.app.entity.PackagePurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link PackagePurchase} entities.
 */
@Repository
public interface PackagePurchaseRepository extends JpaRepository<PackagePurchase, Long> {

    /** All non-deleted package purchases for a student, newest first. */
    List<PackagePurchase> findByStudentIdAndDeletedFalseOrderByPaymentDateDesc(Long studentId);

    /** Active (non-exhausted, non-deleted) packages for a student, oldest first (FIFO). */
    @Query(value = """
            SELECT pp.*
            FROM studio.package_purchases pp
            WHERE pp.student_id = :studentId
              AND pp.deleted = false
              AND pp.classes_remaining > 0
            ORDER BY pp.payment_date ASC
            """, nativeQuery = true)
    List<PackagePurchase> findActivePackagesByStudent(@Param("studentId") Long studentId);

    /** Finds a specific non-deleted package purchase. */
    Optional<PackagePurchase> findByIdAndDeletedFalse(Long id);

    /**
     * Finds all non-deleted package purchases whose payment date falls within
     * the given range. Used for monthly earnings aggregation.
     */
    @Query(value = """
            SELECT pp.*
            FROM studio.package_purchases pp
            JOIN studio.students s ON s.id = pp.student_id
            WHERE pp.deleted = false
              AND s.deleted = false
              AND pp.payment_date >= :from
              AND pp.payment_date <= :to
            ORDER BY pp.payment_date ASC
            """, nativeQuery = true)
    List<PackagePurchase> findByPaymentDateRange(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
