package com.viettel.importwiz.exception.custom;

import lombok.Data;

import static com.viettel.importwiz.constant.error.ErrorCodes.ERROR_CODES;

@Data
public class BadRequestException extends RuntimeException {
    private final String code;
    private final String message;

    public BadRequestException(String code) {
        this.code = code;
        this.message = ERROR_CODES.get(code);
    }
}
