package com.skiply.fee.controller;

import com.skiply.fee.dto.FeeRequest;
import com.skiply.fee.dto.Receipt;
import com.skiply.fee.model.FeeTransaction;
import com.skiply.fee.service.FeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/fees")
@RequiredArgsConstructor
@Tag(name = "Fee API", description = "API for fee collection and receipt generation")
@Slf4j
public class FeeController {
    private final FeeService feeService;

    @PostMapping
    @Operation(summary = "Collect fee payment")
    public ResponseEntity<FeeTransaction> collectFee(@Valid @RequestBody FeeRequest feeRequest) {
        log.info("Received fee collection request for student: {}", feeRequest.getStudentId());
        FeeTransaction transaction = feeService.collectFee(feeRequest);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "Get all fee transactions for a student")
    public ResponseEntity<List<FeeTransaction>> getFeeTransactionsByStudent(
            @PathVariable String studentId) {
        log.debug("Fetching all transactions for student: {}", studentId);
        return ResponseEntity.ok(feeService.getFeeTransactionsByStudent(studentId));
    }

    @GetMapping("/receipt/{referenceNumber}")
    @Operation(summary = "Generate and email receipt for a transaction\n")
    public ResponseEntity<Receipt> generateAndEmailReceipt(@PathVariable String referenceNumber) {
        log.info("Generating and emailing receipt for transaction: {}", referenceNumber);
        return ResponseEntity.ok(feeService.generateReceipt(referenceNumber));
    }

    @GetMapping
    @Operation(summary = "Get all fee transactions")
    public ResponseEntity<List<FeeTransaction>> getAllTransactions() {
        log.info("Fetching all fee transactions");
        return ResponseEntity.ok(feeService.getAllTransactions());
    }

}