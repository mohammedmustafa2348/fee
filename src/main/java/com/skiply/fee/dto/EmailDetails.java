package com.skiply.fee.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailDetails {
    private String recipient;
    private String subject;
    private String message;
    private String attachment;
}