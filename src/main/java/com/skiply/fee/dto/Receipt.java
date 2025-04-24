package com.skiply.fee.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Receipt {
    private String studentName;
    private String studentId;
    private String referenceNumber;
    private LocalDateTime transactionDateTime;
    private String cardNumber;
    private String cardType;
    private BigDecimal amount;
    private String schoolName;
    private String grade;
}