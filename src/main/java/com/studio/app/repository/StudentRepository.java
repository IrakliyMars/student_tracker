package com.studio.app.repository;

import com.studio.app.entity.Student;
import com.studio.app.enums.PricingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for {@link Student} entities.
 * All queries automatically exclude soft-deleted records.
 */
@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    /** Returns all active (non-deleted) students. */
    List<Student> findAllByDeletedFalse();

    /** Returns all active (non-deleted) students, paginated. */
    Page<Student> findAllByDeletedFalse(Pageable pageable);

    /** Returns active students filtered by debtor flag. */
    List<Student> findAllByDeletedFalseAndDebtor(boolean debtor);

    /** Returns active students filtered by debtor flag, paginated. */
    Page<Student> findAllByDeletedFalseAndDebtor(boolean debtor, Pageable pageable);

    /** Returns active students filtered by pricing type. */
    List<Student> findAllByDeletedFalseAndPricingType(PricingType pricingType);

    /** Returns active students filtered by pricing type, paginated. */
    Page<Student> findAllByDeletedFalseAndPricingType(PricingType pricingType, Pageable pageable);

    /** Returns active students filtered by debtor flag and pricing type. */
    List<Student> findAllByDeletedFalseAndDebtorAndPricingType(boolean debtor, PricingType pricingType);

    /** Returns active students filtered by debtor flag and pricing type, paginated. */
    Page<Student> findAllByDeletedFalseAndDebtorAndPricingType(boolean debtor, PricingType pricingType, Pageable pageable);

    /** Finds a non-deleted student by ID. */
    Optional<Student> findByIdAndDeletedFalse(Long id);


    /** Finds non-deleted students whose name contains the given query (case-insensitive). */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """, nativeQuery = true)
    List<Student> searchByName(String query);

    /** Finds non-deleted students by name (case-insensitive), paginated. */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM studio.students s
            WHERE s.deleted = false
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            nativeQuery = true)
    Page<Student> searchByName(String query, Pageable pageable);

    /** Finds non-deleted students by name and debtor flag (case-insensitive). */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """, nativeQuery = true)
    List<Student> searchByNameAndDebtor(String query, boolean debtor);

    /** Finds non-deleted students by name and debtor flag (case-insensitive), paginated. */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            nativeQuery = true)
    Page<Student> searchByNameAndDebtor(String query, boolean debtor, Pageable pageable);

    /** Finds non-deleted students by name and pricing type (case-insensitive). */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """, nativeQuery = true)
    List<Student> searchByNameAndPricingType(String query, PricingType pricingType);

    /** Finds non-deleted students by name and pricing type (case-insensitive), paginated. */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM studio.students s
            WHERE s.deleted = false
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            nativeQuery = true)
    Page<Student> searchByNameAndPricingType(String query, PricingType pricingType, Pageable pageable);

    /** Finds non-deleted students by name, debtor flag, and pricing type (case-insensitive). */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """, nativeQuery = true)
    List<Student> searchByNameAndDebtorAndPricingType(String query, boolean debtor, PricingType pricingType);

    /** Finds non-deleted students by name, debtor flag, and pricing type (case-insensitive), paginated. */
    @Query(value = """
            SELECT s.*
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            countQuery = """
            SELECT COUNT(*)
            FROM studio.students s
            WHERE s.deleted = false
              AND s.debtor = :debtor
              AND s.pricing_type = :#{#pricingType.name()}
              AND (LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%')))
            """,
            nativeQuery = true)
    Page<Student> searchByNameAndDebtorAndPricingType(String query, boolean debtor, PricingType pricingType, Pageable pageable);

    /**
     * Finds active students by matching either student full name/parts or active payer full name.
     */
    @Query(value = """
            SELECT DISTINCT s.*
            FROM studio.students s
            LEFT JOIN studio.payers p
              ON p.student_id = s.id
             AND p.deleted = false
            WHERE s.deleted = false
              AND (
                    LOWER(CONCAT(s.first_name, ' ', s.last_name)) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(s.first_name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(s.last_name) LIKE LOWER(CONCAT('%', :query, '%'))
                 OR LOWER(COALESCE(p.full_name, '')) LIKE LOWER(CONCAT('%', :query, '%'))
              )
            ORDER BY s.first_name, s.last_name, s.id
            """, nativeQuery = true)
    List<Student> searchByStudentOrPayerName(String query);
}
