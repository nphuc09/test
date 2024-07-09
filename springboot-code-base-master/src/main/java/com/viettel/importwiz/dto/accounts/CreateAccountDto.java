package com.viettel.importwiz.dto.accounts;

import com.viettel.importwiz.constant.enums.Role;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
public class CreateAccountDto {

    @Email
    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotBlank
    private String fullName;
    @Enumerated(EnumType.STRING)
    private Role role;
}
