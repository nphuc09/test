package com.viettel.importwiz.exception.custom;

import lombok.Data;

import static com.viettel.importwiz.constant.error.ErrorCodes.ERROR_CODES;

@Data
public class NotFoundException extends RuntimeException {
    private final String code;
    private final String message;

    public NotFoundException(String code) {
        this.code = code;
        this.message = ERROR_CODES.get(code);
    }
}
