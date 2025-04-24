package com.skiply.fee.service;

import com.skiply.fee.client.StudentClient;
import com.skiply.fee.dto.FeeRequest;
import com.skiply.fee.dto.Receipt;
import com.skiply.fee.dto.StudentDto;
import com.skiply.fee.exception.*;
import com.skiply.fee.model.FeeTransaction;
import com.skiply.fee.repository.FeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeeServiceTest {

    @Mock
    private FeeRepository feeRepository;

    @Mock
    private StudentClient studentClient;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private FeeService feeService;

    private FeeRequest validFeeRequest;
    private StudentDto mockStudent;
    private FeeTransaction mockTransaction;

    @BeforeEach
    void setUp() {
        // Setup valid fee request
        validFeeRequest = new FeeRequest();
        validFeeRequest.setStudentId("STD001");
        validFeeRequest.setAmount(new BigDecimal("100.00"));
        validFeeRequest.setCardNumber("1234567890123456");
        validFeeRequest.setCardType("VISA");

        // Setup mock student
        mockStudent = new StudentDto();
        mockStudent.setStudentId("STD001");
        mockStudent.setName("John Doe");
        mockStudent.setEmailId("john@example.com");
        mockStudent.setSchoolName("Test School");
        mockStudent.setGrade("Grade 5");

        // Setup mock transaction
        mockTransaction = new FeeTransaction();
        mockTransaction.setStudentId("STD001");
        mockTransaction.setAmount(new BigDecimal("100.00"));
        mockTransaction.setReferenceNumber("REF-12345678");
        mockTransaction.setTransactionDateTime(LocalDateTime.now());
        mockTransaction.setCardNumber("****-****-****-3456");
        mockTransaction.setCardType("VISA");
    }

    @Nested
    @DisplayName("collectFee Tests")
    class CollectFeeTests {

        @Test
        @DisplayName("Should successfully collect fee with valid request")
        void shouldCollectFeeSuccessfully() {
            // Arrange
            when(studentClient.getStudentById(anyString())).thenReturn(mockStudent);
            when(feeRepository.save(any(FeeTransaction.class))).thenReturn(mockTransaction);

            // Act
            FeeTransaction result = feeService.collectFee(validFeeRequest);

            // Assert
            assertNotNull(result);
            assertEquals(mockTransaction.getReferenceNumber(), result.getReferenceNumber());
        }

        @Test
        @DisplayName("Should throw InvalidFeeAmountException when amount is too low")
        void shouldThrowExceptionWhenAmountTooLow() {
            // Arrange
            validFeeRequest.setAmount(new BigDecimal("0.50"));

            // Act & Assert
            assertThrows(InvalidFeeAmountException.class, () -> feeService.collectFee(validFeeRequest));
        }

        @Test
        @DisplayName("Should throw InvalidFeeAmountException when amount is too high")
        void shouldThrowExceptionWhenAmountTooHigh() {
            // Arrange
            validFeeRequest.setAmount(new BigDecimal("100001.00"));

            // Act & Assert
            assertThrows(InvalidFeeAmountException.class, () -> feeService.collectFee(validFeeRequest));
        }

        @Test
        @DisplayName("Should throw InvalidPaymentDetailsException for invalid card number")
        void shouldThrowExceptionForInvalidCardNumber() {
            // Arrange
            validFeeRequest.setCardNumber("123");

            // Act & Assert
            assertThrows(InvalidPaymentDetailsException.class, () -> feeService.collectFee(validFeeRequest));
        }
    }

    @Nested
    @DisplayName("getFeeTransactionsByStudent Tests")
    class GetFeeTransactionsByStudentTests {

        @Test
        @DisplayName("Should return list of transactions for valid student ID")
        void shouldReturnTransactionsForValidStudentId() {
            // Arrange
            List<FeeTransaction> expectedTransactions = Arrays.asList(mockTransaction);
            when(feeRepository.findByStudentId(anyString())).thenReturn(expectedTransactions);

            // Act
            List<FeeTransaction> result = feeService.getFeeTransactionsByStudent("STD001");

            // Assert
            assertThat(result).hasSize(1);
            assertEquals(expectedTransactions, result);
        }

        @Test
        @DisplayName("Should throw InvalidParameterException for null student ID")
        void shouldThrowExceptionForNullStudentId() {
            assertThrows(InvalidParameterException.class, () -> feeService.getFeeTransactionsByStudent(null));
        }

        @Test
        @DisplayName("Should return empty list when no transactions found")
        void shouldReturnEmptyListWhenNoTransactions() {
            // Arrange
            when(feeRepository.findByStudentId(anyString())).thenReturn(List.of());

            // Act
            List<FeeTransaction> result = feeService.getFeeTransactionsByStudent("STD001");

            // Assert
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("generateReceipt Tests")
    class GenerateReceiptTests {

        @Test
        @DisplayName("Should generate receipt for valid reference number")
        void shouldGenerateReceiptSuccessfully() {
            // Arrange
            when(feeRepository.findByReferenceNumber(anyString())).thenReturn(Optional.of(mockTransaction));
            when(studentClient.getStudentById(anyString())).thenReturn(mockStudent);
            doNothing().when(emailService).sendReceiptEmail(any(Receipt.class), anyString());


            // Act
            Receipt result = feeService.generateReceipt("REF-12345678");

            // Assert
            assertNotNull(result);
            assertEquals(mockStudent.getName(), result.getStudentName());
            assertEquals(mockTransaction.getReferenceNumber(), result.getReferenceNumber());
            verify(emailService).sendReceiptEmail(any(Receipt.class), eq(mockStudent.getEmailId()));
        }

        @Test
        @DisplayName("Should throw TransactionNotFoundException for invalid reference")
        void shouldThrowExceptionForInvalidReference() {
            // Arrange
            when(feeRepository.findByReferenceNumber(anyString())).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(TransactionNotFoundException.class,
                    () -> feeService.generateReceipt("INVALID-REF"));
        }

        @Test
        @DisplayName("Should throw InvalidParameterException for null reference")
        void shouldThrowExceptionForNullReference() {
            assertThrows(InvalidParameterException.class,
                    () -> feeService.generateReceipt(null));
        }

        @Test
        @DisplayName("Should throw ReceiptGenerationException when student not found")
        void shouldThrowExceptionWhenStudentNotFound() {
            // Arrange
            when(feeRepository.findByReferenceNumber(anyString())).thenReturn(Optional.of(mockTransaction));
            when(studentClient.getStudentById(anyString())).thenThrow(StudentNotFoundException.class);

            // Act & Assert
            assertThrows(ReceiptGenerationException.class,
                    () -> feeService.generateReceipt("REF-12345678"));
        }
    }
}

