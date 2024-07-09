package com.viettel.importwiz.exception;

import com.viettel.importwiz.exception.custom.*;
import com.viettel.importwiz.response.ExceptionResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

import static com.viettel.importwiz.constant.error.ErrorCodes.*;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlingCenter extends ResponseEntityExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ExceptionResponse> handleBusinessException(BusinessException businessException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(businessException.getCode(), businessException.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(CorsUnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleCorsUnauthorizedException(CorsUnauthorizedException corsUnauthorizedException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(corsUnauthorizedException.getCode(), corsUnauthorizedException.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RecordNotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleRecordNotFoundException(RecordNotFoundException recordNotFoundException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(recordNotFoundException.getCode(), recordNotFoundException.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ExceptionResponse> handleUnauthorizedException(UnauthorizedException unauthorizedException) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(unauthorizedException.getCode(), unauthorizedException.getMessage());
        return new ResponseEntity<>(exceptionResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionResponse> handleBadRequestException(BadRequestException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionResponse> handleNotFoundException(NotFoundException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(exceptionResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ExceptionResponse> handleTooManyRequestException(TooManyRequestException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(ex.getCode(), ex.getMessage());

        return new ResponseEntity<>(exceptionResponse, HttpStatus.TOO_MANY_REQUESTS);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ObjectError> objectErrors = ex.getBindingResult().getAllErrors();
        String messages = String.join(",",
            objectErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.toList()));
        return new ResponseEntity<>(new ExceptionResponse(ARG_NOT_VALID, messages),
            HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleCommonException(Exception ex) {
        ex.printStackTrace();
        ExceptionResponse exceptionResponse = new ExceptionResponse(AN_ERROR_OCCURRED, ERROR_CODES.get(AN_ERROR_OCCURRED));
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
