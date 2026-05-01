package com.studio.app.controller.impl;
import com.studio.app.controller.PayerApi;
import com.studio.app.dto.request.PayerRequest;
import com.studio.app.dto.response.PayerResponse;
import com.studio.app.service.PayerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller implementation for student payer operations.
 * Delegates to {@link PayerService} for business logic.
 */
@RestController
@RequiredArgsConstructor
public class PayerController implements PayerApi {
    private final PayerService payerService;

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PayerResponse> addPayer(Long studentId, PayerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(payerService.addPayer(studentId, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Page<PayerResponse>> getPayersForStudent(Long studentId, int page, int size) {
        var pageable = PageRequest.of(page, size,
                Sort.by("fullName").ascending().and(Sort.by("id")));
        return ResponseEntity.ok(payerService.getPayersForStudent(studentId, pageable));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<PayerResponse> updatePayer(Long studentId, Long payerId, PayerRequest request) {
        return ResponseEntity.ok(payerService.updatePayer(studentId, payerId, request));
    }

    /** {@inheritDoc} */
    @Override
    public ResponseEntity<Void> removePayer(Long studentId, Long payerId) {
        payerService.removePayer(studentId, payerId);
        return ResponseEntity.noContent().build();
    }
}
