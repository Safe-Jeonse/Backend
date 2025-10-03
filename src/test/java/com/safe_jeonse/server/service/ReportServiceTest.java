package com.safe_jeonse.server.service;

import com.safe_jeonse.server.dto.request.ReportRequest;
import com.safe_jeonse.server.dto.response.ReportResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private DeepCheckService deepCheckService;

    @Mock
    private QuickCheckService quickCheckService;

    @Mock
    private MultipartFile mockRegistryFile;

    @InjectMocks
    private ReportService reportService;

    @Test
    @DisplayName("PDF X")
    void quickCheck() {
        // given
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setRegistryFile(mockRegistryFile);
        when(mockRegistryFile.isEmpty()).thenReturn(true);
        when(quickCheckService.quickCheck()).thenReturn("quick");

        // when
        String actualResponse = reportService.generateReport(reportRequest);

        // then
        Assertions.assertThat(actualResponse).isEqualTo("quick");
        verify(quickCheckService, times(1)).quickCheck();
        verifyNoInteractions(deepCheckService);

    }

    @Test
    @DisplayName("PDF O")
    void  deepCheck(){
        // given
        ReportRequest reportRequest = new ReportRequest();
        reportRequest.setRegistryFile(mockRegistryFile);
        when(mockRegistryFile.isEmpty()).thenReturn(false);
        when(deepCheckService.deepCheck()).thenReturn("deep");

        // when
        String actualResponse = reportService.generateReport(reportRequest);

        // then
        Assertions.assertThat(actualResponse).isEqualTo("deep");
        verify(deepCheckService, times(1)).deepCheck();
        verifyNoInteractions(quickCheckService);
    }

}