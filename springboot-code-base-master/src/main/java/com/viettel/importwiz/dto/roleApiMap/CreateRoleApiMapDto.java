package com.viettel.importwiz.dto.roleApiMap;

import com.viettel.importwiz.constant.enums.ApiMethods;
import com.viettel.importwiz.constant.enums.Role;
import lombok.Data;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotBlank;


@Data
public class CreateRoleApiMapDto {

    @Enumerated(EnumType.STRING)
    private Role role;

    @Enumerated(EnumType.STRING)
    private ApiMethods method;

    @NotBlank
    private String api;
}
