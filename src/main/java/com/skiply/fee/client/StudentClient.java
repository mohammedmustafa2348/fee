package com.skiply.fee.client;


import com.skiply.fee.dto.StudentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class StudentClient {
    private final WebClient webClient;

    public StudentClient(@Value("${student.service.url}") String studentServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(studentServiceUrl)
                .build();
    }

    public StudentDto getStudentById(String studentId) {
        return webClient.get()
                .uri("/api/students/{studentId}", studentId)
                .retrieve()
                .bodyToMono(StudentDto.class)
                .block();
    }
}