package cl.duoc.ms_barriodigital_report.exception;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import cl.duoc.ms_barriodigital_report.model.RangoTiempo;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarRangoInvalido(IllegalArgumentException ex) {
        List<String> valores = Arrays.stream(RangoTiempo.values())
                .map(RangoTiempo::getValor)
                .toList();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", ex.getMessage(),
                "valoresPermitidos", valores));
    }
}
