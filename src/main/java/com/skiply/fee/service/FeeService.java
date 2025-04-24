//package com.skiply.fee.service;
//
//import com.skiply.fee.client.StudentClient;
//import com.skiply.fee.dto.FeeRequest;
//import com.skiply.fee.dto.Receipt;
//import com.skiply.fee.dto.StudentDto;
//import com.skiply.fee.exception.FeeTransactionException;
//import com.skiply.fee.model.FeeTransaction;
//import com.skiply.fee.repository.FeeRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class FeeService {
//    private final FeeRepository feeRepository;
//    private final StudentClient studentClient;
//
//    @Transactional
//    public FeeTransaction collectFee(FeeRequest feeRequest) {
//        log.info("Collecting fee for student ID: {}", feeRequest.getStudentId());
//
//        try {
//            // Generate reference number
//            String referenceNumber = "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
//            StudentDto student = studentClient.getStudentById(feeRequest.getStudentId());
//            FeeTransaction transaction = new FeeTransaction();
//            transaction.setStudentId(feeRequest.getStudentId());
//            transaction.setAmount(feeRequest.getAmount());
//            transaction.setReferenceNumber(referenceNumber);
//            transaction.setTransactionDateTime(LocalDateTime.now());
//            transaction.setCardNumber(maskCardNumber(feeRequest.getCardNumber()));
//            transaction.setCardType(feeRequest.getCardType());
//
//            FeeTransaction savedTransaction = feeRepository.save(transaction);
//            log.info("Fee collected successfully. Reference: {}", referenceNumber);
//            return savedTransaction;
//        } catch (Exception e) {
//            log.error("Failed to collect fee: {}", e.getMessage());
//            throw new FeeTransactionException("Failed to process fee payment. Check if Student ID is valid");
//        }
//    }
//
//    public List<FeeTransaction> getFeeTransactionsByStudent(String studentId) {
//        log.debug("Fetching fee transactions for student ID: {}", studentId);
//        return feeRepository.findByStudentId(studentId);
//    }
//
//    public Receipt generateReceipt(String referenceNumber) {
//        log.info("Generating receipt for reference: {}", referenceNumber);
//        FeeTransaction transaction = feeRepository.findByReferenceNumber(referenceNumber)
//                .orElseThrow(() -> {
//                    log.warn("Transaction not found with reference: {}", referenceNumber);
//                    return new FeeTransactionException("Transaction not found");
//                });
//
//        try {
//            StudentDto student = studentClient.getStudentById(transaction.getStudentId());
//
//            Receipt receipt = new Receipt();
//            receipt.setStudentName(student.getName());
//            receipt.setStudentId(student.getStudentId());
//            receipt.setReferenceNumber(transaction.getReferenceNumber());
//            receipt.setTransactionDateTime(transaction.getTransactionDateTime());
//            receipt.setCardNumber(transaction.getCardNumber());
//            receipt.setCardType(transaction.getCardType());
//            receipt.setAmount(transaction.getAmount());
//            receipt.setSchoolName(student.getSchoolName());
//            receipt.setGrade(student.getGrade());
//
//            log.debug("Receipt generated successfully for reference: {}", referenceNumber);
//            return receipt;
//        } catch (Exception e) {
//            log.error("Failed to generate receipt: {}", e.getMessage());
//            throw new FeeTransactionException("Failed to fetch student details");
//        }
//    }
//
//    private String maskCardNumber(String cardNumber) {
//        if (cardNumber == null || cardNumber.length() < 4) {
//            return "****";
//        }
//        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
//    }
//}

package com.skiply.fee.service;

import com.skiply.fee.client.StudentClient;
import com.skiply.fee.dto.FeeRequest;
import com.skiply.fee.dto.Receipt;
import com.skiply.fee.dto.StudentDto;
import com.skiply.fee.exception.*;
import com.skiply.fee.model.FeeTransaction;
import com.skiply.fee.repository.FeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FeeService {
    private final FeeRepository feeRepository;
    private final StudentClient studentClient;
    private final EmailService emailService;

    private static final BigDecimal MINIMUM_FEE_AMOUNT = new BigDecimal("1.00");
    private static final BigDecimal MAXIMUM_FEE_AMOUNT = new BigDecimal("100000.00");
    private static final int MAX_DAILY_TRANSACTIONS = 5;

    @Transactional
    public FeeTransaction collectFee(FeeRequest feeRequest) {
        log.info("Collecting fee for student ID: {}", feeRequest.getStudentId());
        validateFeeRequest(feeRequest);

        try {
            StudentDto student = studentClient.getStudentById(feeRequest.getStudentId());

            // Generate reference number
            String referenceNumber = generateReferenceNumber();

            FeeTransaction transaction = new FeeTransaction();
            transaction.setStudentId(feeRequest.getStudentId());
            transaction.setAmount(feeRequest.getAmount());
            transaction.setReferenceNumber(referenceNumber);
            transaction.setTransactionDateTime(LocalDateTime.now());
            transaction.setCardNumber(maskCardNumber(feeRequest.getCardNumber()));
            transaction.setCardType(feeRequest.getCardType());

            FeeTransaction savedTransaction = feeRepository.save(transaction);

//            // Generate and send receipt email
//            Receipt receipt = generateReceiptForEmail(savedTransaction, student);
//            emailService.sendReceiptEmail(receipt, student.getEmailId());

            log.info("Fee collected Successfully. Reference: {}", referenceNumber);

            return savedTransaction;
        } catch (Exception e) {
            log.error("Failed to collect fee: {}", e.getMessage());
            throw new FeeTransactionException("Failed to process fee payment: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<FeeTransaction> getFeeTransactionsByStudent(String studentId) {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidParameterException("Student ID cannot be empty");
        }

        log.debug("Fetching fee transactions for student ID: {}", studentId);
        List<FeeTransaction> transactions = feeRepository.findByStudentId(studentId);

        if (transactions.isEmpty()) {
            log.info("No transactions found for student: {}", studentId);
        }
        return transactions;
    }

    @Transactional(readOnly = true)
    public Receipt generateReceipt(String referenceNumber) {
        if (referenceNumber == null || referenceNumber.trim().isEmpty()) {
            throw new InvalidParameterException("Reference number cannot be empty");
        }

        log.info("Generating receipt for reference: {}", referenceNumber);
        FeeTransaction transaction = feeRepository.findByReferenceNumber(referenceNumber)
                .orElseThrow(() -> new TransactionNotFoundException("Transaction not found: " + referenceNumber));

        try {
            StudentDto student = studentClient.getStudentById(transaction.getStudentId());
            Receipt receipt = createReceipt(transaction, student);

            // Send receipt email
            emailService.sendReceiptEmail(receipt, student.getEmailId());
            log.info("Receipt generated and email sent for reference: {}", referenceNumber);

            return receipt;
        } catch (StudentNotFoundException e) {
            log.error("Student not found while generating receipt: {}", transaction.getStudentId());
            throw new ReceiptGenerationException("Unable to generate receipt: Student not found");
        } catch (Exception e) {
            log.error("Failed to generate receipt: {}", e.getMessage());
            throw new ReceiptGenerationException("Failed to generate receipt");
        }
    }

    @Transactional(readOnly = true)
    public List<FeeTransaction> getAllTransactions() {
        log.debug("Fetching all transactions");

        List<FeeTransaction> transactions = feeRepository.findAll();

        if (transactions.isEmpty()) {
            log.info("No transactions found in the system");
        } else {
            log.debug("Found {} transactions", transactions.size());
        }
        return transactions;
    }


    private Receipt generateReceiptForEmail(FeeTransaction transaction, StudentDto student) {
        Receipt receipt = new Receipt();
        receipt.setStudentName(student.getName());
        receipt.setStudentId(student.getStudentId());
        receipt.setReferenceNumber(transaction.getReferenceNumber());
        receipt.setTransactionDateTime(transaction.getTransactionDateTime());
        receipt.setCardNumber(transaction.getCardNumber());
        receipt.setCardType(transaction.getCardType());
        receipt.setAmount(transaction.getAmount());
        receipt.setSchoolName(student.getSchoolName());
        receipt.setGrade(student.getGrade());
        return receipt;
    }


    private void validateFeeRequest(FeeRequest feeRequest) {
        if (feeRequest.getAmount().compareTo(MINIMUM_FEE_AMOUNT) < 0) {
            throw new InvalidFeeAmountException("Fee amount must be at least " + MINIMUM_FEE_AMOUNT);
        }
        if (feeRequest.getAmount().compareTo(MAXIMUM_FEE_AMOUNT) > 0) {
            throw new InvalidFeeAmountException("Fee amount cannot exceed " + MAXIMUM_FEE_AMOUNT);
        }
        if (!isValidCardNumber(feeRequest.getCardNumber())) {
            throw new InvalidPaymentDetailsException("Invalid card number");
        }
    }

    private boolean isValidCardNumber(String cardNumber) {
        return cardNumber != null && cardNumber.matches("\\d{16}");
    }

    private String generateReferenceNumber() {
        return "REF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Receipt createReceipt(FeeTransaction transaction, StudentDto student) {
        Receipt receipt = new Receipt();
        receipt.setStudentName(student.getName());
        receipt.setStudentId(student.getStudentId());
        receipt.setReferenceNumber(transaction.getReferenceNumber());
        receipt.setTransactionDateTime(transaction.getTransactionDateTime());
        receipt.setCardNumber(transaction.getCardNumber());
        receipt.setCardType(transaction.getCardType());
        receipt.setAmount(transaction.getAmount());
        receipt.setSchoolName(student.getSchoolName());
        receipt.setGrade(student.getGrade());
        return receipt;
    }

    private String maskCardNumber(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            return "****";
        }
        return "****-****-****-" + cardNumber.substring(cardNumber.length() - 4);
    }
}