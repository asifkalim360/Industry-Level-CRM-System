package com.enterprise.crm.common.response;


import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ApiResponse<T>     // Generic <T> ka matlab: → Kisi bhi type ka data wrap kar sakte hain.
{
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
}
