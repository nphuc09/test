package com.viettel.importwiz.security.vsa;


import com.viettel.importwiz.entity.Account;
import com.viettel.importwiz.repository.AccountRepository;
import com.viettel.importwiz.security.jwt.AJwtTokenUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@AllArgsConstructor
public class ContextInterceptor implements HandlerInterceptor {

    private AJwtTokenUtil jwtTokenUtil;

    private AccountRepository accountRepository;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        final String requestTokenHeader =
                request.getHeader(HttpHeaders.AUTHORIZATION);

        String jwtToken = null;
        String username = null;

        if (requestTokenHeader != null && requestTokenHeader.startsWith(
                "Bearer ") && !request.getRequestURI().startsWith("/auth" +
                "/refreshToken")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
            Account account =
                    this.accountRepository.findAccountByUsername(username);
            if (account != null) {
                request.setAttribute("account", account);
            }
        }
        return true;
    }
}
