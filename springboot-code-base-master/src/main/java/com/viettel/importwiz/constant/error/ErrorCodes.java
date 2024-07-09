package com.viettel.importwiz.constant.error;

import java.util.HashMap;
import java.util.Map;

public class ErrorCodes {

    public static final String AN_ERROR_OCCURRED = "AN_ERROR_OCCURRED";
    public static final String FILE_UPLOAD_FAIL = "FILE_UPLOAD_FAIL";
    public static final String FILE_DOWNLOAD_FAIL = "FILE_DOWNLOAD_FAIL";
    public static final String COULD_NOT_INIT_FOLDER_UPLOAD = "COULD_NOT_INIT_FOLDER_UPLOAD";
    public static final String ACCOUNT_NOT_EXIST = "ACCOUNT_NOT_EXIST";
    public static final String EMAIL_ALREADY_EXIST = "EMAIL_ALREADY_EXIST";
    public static final String USERNAME_ALREADY_EXIST = "USERNAME_ALREADY_EXIST";
    public static final String INVALID_JWT_TOKEN = "INVALID_JWT_TOKEN";
    public static final String EXPIRED_JWT_TOKEN = "EXPIRED_JWT_TOKEN";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String ROLE_API_MAP_NOT_EXIST = "ROLE_API_MAP_NOT_EXIST";
    public static final String INVALID_TICKET = "INVALID_TICKET";
    public static final String SERVICE_NAME_ALREADY_EXIST = "SERVICE_NAME_ALREADY_EXIST";
    public static final String SERVICE_NOT_EXIST = "SERVICE_NOT_EXIST";
    public static final String SERVICE_INFO_ALREADY_EXIST = "SERVICE_INFO_ALREADY_EXIST";

    public static final String SERVICE_INFO_NOT_EXIST = "SERVICE_INFO_NOT_EXIST";

    public static final String PERMISSION_DENIED = "PERMISSION_DENIED";
    public static final String IMPORT_PERMISSION_DENIED = "IMPORT_PERMISSION_DENIED";
    public static final String IMPORT_PERMISSION_ALREADY_EXIST = "IMPORT_PERMISSION_ALREADY_EXIST";
    public static final String IMPORT_PERMISSION_NOT_EXIST = "IMPORT_PERMISSION_NOT_EXIST";
    public static final String CONVERT_TO_CSV_FAIL = "CONVERT_TO_CSV_FAIL";
    public static final String INPUT_DATA_INVALID = "INPUT_DATA_INVALID";
    public static final String TOO_MANY_REQUEST = "TOO_MANY_REQUEST";
    public static final String ARG_NOT_VALID = "ARG_NOT_VALID";
    public static final Map<String, String> ERROR_CODES = new HashMap<>();

    static {
        ERROR_CODES.put(AN_ERROR_OCCURRED, "An error occurred!");
        ERROR_CODES.put(FILE_UPLOAD_FAIL, "File upload fail!");
        ERROR_CODES.put(FILE_DOWNLOAD_FAIL, "File download fail!");
        ERROR_CODES.put(COULD_NOT_INIT_FOLDER_UPLOAD, "Could not init folder upload!");
        ERROR_CODES.put(ACCOUNT_NOT_EXIST, "Account is not exist!");
        ERROR_CODES.put(EMAIL_ALREADY_EXIST, "Email is already exist!");
        ERROR_CODES.put(USERNAME_ALREADY_EXIST, "Username is already exist!");
        ERROR_CODES.put(INVALID_JWT_TOKEN, "JWT token invalid!");
        ERROR_CODES.put(EXPIRED_JWT_TOKEN, "JWT token  is expired!");
        ERROR_CODES.put(ROLE_API_MAP_NOT_EXIST, "Role api map is not exist!");
        ERROR_CODES.put(INVALID_TICKET, "Ticket is invalid!");
        ERROR_CODES.put(SERVICE_NAME_ALREADY_EXIST, "Service name is already existed");
        ERROR_CODES.put(SERVICE_NOT_EXIST, "Service is not existed");
        ERROR_CODES.put(SERVICE_INFO_ALREADY_EXIST, "Service info is already existed");
        ERROR_CODES.put(SERVICE_INFO_NOT_EXIST, "Service info is not existed");
        ERROR_CODES.put(PERMISSION_DENIED, "Permission denied");
        ERROR_CODES.put(IMPORT_PERMISSION_DENIED, "Import permission denied");
        ERROR_CODES.put(CONVERT_TO_CSV_FAIL, "Convert to csv fail");
        ERROR_CODES.put(INPUT_DATA_INVALID, "Input data is invalid");
        ERROR_CODES.put(TOO_MANY_REQUEST, "Too many request");
        ERROR_CODES.put(IMPORT_PERMISSION_ALREADY_EXIST, "Import permission already exist");
        ERROR_CODES.put(ARG_NOT_VALID, "Argument Not Valid");
        ERROR_CODES.put(IMPORT_PERMISSION_NOT_EXIST, "Import permission is not exist");
    }

    private ErrorCodes() {
    }
}
