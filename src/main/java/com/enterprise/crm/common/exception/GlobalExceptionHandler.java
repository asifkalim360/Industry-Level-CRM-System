package com.enterprise.crm.common.exception;

import com.enterprise.crm.common.response.ApiResponse;
import org.aspectj.apache.bcel.classfile.Code;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestControllerAdvice       // @RestControllerAdvice → Pure application ke exceptions yaha handle honge.
                            // Agar ye nahi use karte to 👉 Har controller me try-catch likhna padta. jisse Code messy ho jata.
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<?> handleBusinessException(BusinessException ex) {
        return ApiResponse.builder()
                .success(false)
                .message(ex.getMessage())
                .data(null)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
