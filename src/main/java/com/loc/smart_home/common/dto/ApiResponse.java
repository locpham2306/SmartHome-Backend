package com.loc.smart_home.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T>{
    private boolean success;
    private String strCode;
    private String message;
    private T data;
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "Success", "Success",data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return error("ERROR", message);
    }

    public static <T> ApiResponse<T> error(String strCode, String message) {
        return new ApiResponse<>(false, strCode, message, null);
    }
}
