package com.viettel.importwiz.security.jwt;

import com.viettel.importwiz.entity.Account;
import com.viettel.importwiz.entity.RoleApiMap;
import com.viettel.importwiz.exception.custom.CorsUnauthorizedException;
import com.viettel.importwiz.repository.AccountRepository;
import com.viettel.importwiz.repository.RoleApiMapRepository;
import com.viettel.importwiz.repository.cache.TokenRepository;
import com.viettel.importwiz.util.LogUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.annotation.Nonnull;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.viettel.importwiz.constant.error.ErrorCodes.EXPIRED_JWT_TOKEN;
import static com.viettel.importwiz.constant.error.ErrorCodes.INVALID_JWT_TOKEN;

@Component
public class AJwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private RoleApiMapRepository roleApiMapRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private AJwtTokenUtil jwtTokenUtil;

    @Value("${isOnlyUsingVsa}")
    private String isOnlyUsingVsa;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain chain) throws ServletException, IOException {
        ContentCachingRequestWrapper contentCachingRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper contentCachingResponse = new ContentCachingResponseWrapper(response);

        String[] authenApiWhitelist;
        if (isOnlyUsingVsa.equals("true"))

            authenApiWhitelist = new String[]{"/auth/oauthVsa/home", "/auth/logoutVsa", "/auth/refreshToken", "/auth/refreshToken2"};
        else
            authenApiWhitelist = new String[]{"/auth/oauthVsa/home", "/auth/logoutVsa", "/auth/refreshToken", "/auth/refreshToken2", "/auth/loginSso", "/auth/logoutSso"};


        final String requestTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        String username = null;
        String jwtToken = null;

        // Skip getting JWT token for authentication api
        if (Arrays.stream(authenApiWhitelist).noneMatch(api -> request.getRequestURI().substring(request.getContextPath().length()).equals(api))) {
            // JWT Token is in the form "Bearer token". Remove Bearer word
            // and get only the Token
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                try {
                    username = jwtTokenUtil.getUsernameFromToken(jwtToken);
//                    if (!tokenRepository.find(requestTokenHeader).isPresent()) {
//                        logger.error("JWT Token has been disabled");
//                        throw new CorsUnauthorizedException(INVALID_JWT_TOKEN);
//                    }
                } catch (IllegalArgumentException e) {
                    logger.error("Unable to get username from JWT Token");
                    throw new CorsUnauthorizedException(INVALID_JWT_TOKEN);
                } catch (ExpiredJwtException e) {
                    logger.error("JWT Token has expired");
                    throw new CorsUnauthorizedException(EXPIRED_JWT_TOKEN);
                } catch (Exception e) {
                    logger.error("JWT Token invalid");
                    throw new CorsUnauthorizedException(INVALID_JWT_TOKEN);
                }
            } else {
                logger.warn("JWT Token does not begin with Bearer String");
            }
        }
        //Once we get the token validate it.
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            Account account = accountRepository.findAccountByUsername(username);
            if (account != null) {
                // if token is valid configure Spring Security to manually
                // set authentication
                boolean jwtValid = jwtTokenUtil.validateToken(jwtToken, account);
                if (jwtValid) {
                    boolean isValid = false;
                    List<RoleApiMap> roleApiMap = roleApiMapRepository.getRoleApiMapByRole(account.getRole().toString());
                    if (roleApiMap.stream().anyMatch(item -> {
                        AntPathMatcher antPathMatcher = new AntPathMatcher();
                        return antPathMatcher.match(item.getApi(), request.getRequestURI()) && request.getMethod().equals(item.getMethod());
                    })) {
                        isValid = true;
                    }

                    if (isValid) {
                        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(account, null, null);
                        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(contentCachingRequest));
                        // After setting the Authentication in the context,
                        // we specify
                        // that the current user is authenticated. So it
                        // passes the Spring Security Configurations
                        // successfully.
                        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                    }
                }
            }
        }
        request.setAttribute("username", username);
        chain.doFilter(contentCachingRequest, contentCachingResponse);
        logging(request, contentCachingRequest, contentCachingResponse);
    }

    private void logging(HttpServletRequest request, ContentCachingRequestWrapper contentCachingRequest, ContentCachingResponseWrapper contentCachingResponse) {

        try {
            String requestBody = getStringValue(contentCachingRequest.getContentAsByteArray(), contentCachingRequest.getCharacterEncoding());
            if ("GET".equalsIgnoreCase(contentCachingRequest.getMethod())) {
                requestBody = contentCachingRequest.getParameterMap().entrySet().stream().map(stringEntry -> stringEntry.getKey() + ":" + Arrays.toString(stringEntry.getValue())).collect(Collectors.joining(","));
            }
            if ("POST".equalsIgnoreCase(contentCachingRequest.getMethod())) {
                JSONParser parser = new JSONParser();
                if (!requestBody.isEmpty()) {
                    JSONObject requestBodyJson = (JSONObject) parser.parse(requestBody);
                    if (requestBodyJson.containsKey("password")) requestBodyJson.put("password", "******");
                    requestBody = requestBodyJson.toJSONString();
                }
            }

            long requestStartTime = contentCachingRequest.getSession().getCreationTime();
            int numberOfRecord = contentCachingResponse.getContentSize();
            String curClass = (contentCachingRequest.getSession().getAttribute("BEAN_CLASS") + "");
            String curMethod = (contentCachingRequest.getSession().getAttribute("BEAN_METHOD") + "");
            String vsa = "IMPORTWIZ_" + curClass + "_" + curMethod;
            String dataType = "0000000000";
            LogUtil.info(request, curClass, new Date(requestStartTime), new Date(), curMethod, "", requestBody, null, requestBody, contentCachingResponse.getStatus() + "", dataType, numberOfRecord, vsa, null);
            contentCachingResponse.copyBodyToResponse();
        } catch (IllegalStateException e) {
            logger.debug(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getStringValue(byte[] contentAsByteArray, String characterEncoding) {
        try {
            return new String(contentAsByteArray, 0, contentAsByteArray.length, characterEncoding);
        } catch (UnsupportedEncodingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
