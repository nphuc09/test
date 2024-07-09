package com.viettel.importwiz.exception.custom;

import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.viettel.importwiz.constant.error.ErrorCodes.ERROR_CODES;

@Data
@ResponseStatus(HttpStatus.NOT_FOUND)
public class RecordNotFoundException extends RuntimeException {
    private final String code;
    private final String message;

    public RecordNotFoundException(String code) {
        this.code = code;
        this.message = ERROR_CODES.get(code);
    }
}
