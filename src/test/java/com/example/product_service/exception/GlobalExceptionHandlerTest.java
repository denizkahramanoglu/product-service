package com.example.product_service.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private Clock clock;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        // Testlerin her çalışmasında saati 2026-07-28 10:00:00 anına sabitliyoruz
        Instant fixedInstant = Instant.parse("2026-07-28T10:00:00Z");
        ZoneId zoneId = ZoneId.of("UTC");

        when(clock.instant()).thenReturn(fixedInstant);
        when(clock.getZone()).thenReturn(zoneId);
    }

    @Test
    @DisplayName("BusinessException fırlatıldığında servisin belirlediği statü ve hata mesajı string timestamp ile dönmeli")
    void handleBusinessException_shouldReturnFormattedErrorResponse() {
        BusinessException exception = new BusinessException("Kayıt bulunamadı", HttpStatus.NOT_FOUND);

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleBusinessException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(404, response.getBody().get("status"));
        assertEquals("Kayıt bulunamadı", response.getBody().get("message"));
        assertEquals("2026-07-28T10:00", response.getBody().get("timestamp"));
    }

    @Test
    @DisplayName("Beklenmeyen Exception fırlatıldığında 500 Internal Server Error ve jenerik mesaj dönmeli")
    void handleGeneralException_shouldReturn500ErrorResponse() {
        Exception exception = new Exception("Veritabanı bağlantısı aniden koptu!"); // Loga yazılacak, kullanıcıya gitmeyecek

        ResponseEntity<Map<String, Object>> response = globalExceptionHandler.handleGeneralException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assert response.getBody() != null;
        assertEquals(500, response.getBody().get("status"));
        assertEquals("Sistemde beklenmeyen bir hata oluştu. Lütfen daha sonra tekrar deneyin.", response.getBody().get("message"));
        assertEquals("2026-07-28T10:00", response.getBody().get("timestamp"));
    }
}