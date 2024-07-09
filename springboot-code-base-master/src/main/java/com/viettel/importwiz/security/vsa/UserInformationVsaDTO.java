package com.viettel.importwiz.security.vsa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import viettel.passport.client.ObjectToken;

import java.io.Serializable;
import java.util.List;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInformationVsaDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private List<ObjectToken> objectTokens;
    private String access_token;
    private String lang;
}
