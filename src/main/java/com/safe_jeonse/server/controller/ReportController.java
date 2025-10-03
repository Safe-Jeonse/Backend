package com.safe_jeonse.server.controller;

import com.safe_jeonse.server.dto.request.ReportRequest;
import com.safe_jeonse.server.dto.response.ReportResponse;
import com.safe_jeonse.server.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @PostMapping(value = "/api/report", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReportResponse> report(@Valid @ModelAttribute ReportRequest request) {
        reportService.generateReport(request);
        ReportResponse response = new ReportResponse("test");
        return ResponseEntity.ok(response);
    }
}
