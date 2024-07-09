package com.viettel.importwiz.security;

import com.viettel.importwiz.entity.Account;
import com.viettel.importwiz.entity.Token;
import com.viettel.importwiz.exception.custom.UnauthorizedException;
import com.viettel.importwiz.repository.AccountRepository;
import com.viettel.importwiz.repository.cache.TicketRepository;
import com.viettel.importwiz.repository.cache.TokenRepository;
import com.viettel.importwiz.security.jwt.AJwtTokenUtil;
import com.viettel.importwiz.security.vsa.UserInformationVsaDTO;
import com.viettel.importwiz.service.AccountService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;
import viettel.passport.client.ObjectToken;
import viettel.passport.client.UserToken;

import javax.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.viettel.importwiz.constant.error.ErrorCodes.INVALID_TICKET;

@RestController
@EnableScheduling
@Slf4j
@Tag(name = "Auth")
@RequestMapping("/auth")
public class AAuthenticationController {

    @Autowired
    private AJwtTokenUtil jwtTokenUtil;

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Value("${sso.redirectUrl}")
    private String redirectUrl;

    @Value("${sso.errorUrl}")
    private String errorUrl;

    @Value("${sso.logoutUrl}")
    private String logoutUrl;

    @Value("${sso.service}")
    private String service;

    @Value("${sso.appCode}")
    private String appCode;

    @Value("${tokenExpiredTime}")
    private Long tokenExpiredTime;

    @GetMapping("/oauthVsa/home")
    public RedirectView createAuthentication(HttpServletRequest request,
                                             @RequestParam String ticket) {
        log.info("ticket: " + ticket);
        log.info("/oauthVsa/home");
        UserToken vsaUserToken =
            (UserToken) request.getSession().getAttribute("vsaUserToken");
        List<ObjectToken> objectTokens = vsaUserToken.getParentMenu();
        RedirectView redirectView = new RedirectView();
//        String username = vsaUserToken.getUserName();
        String email = vsaUserToken.getEmail();
        String username = email.substring(0, email.indexOf('@'));
        log.info("vsaUserToken.username: " + username);
        log.info("vsaUserToken.email: " + vsaUserToken.getEmail());
        try {
            Account account = accountService.findByUsername(username);

            log.info("account: " + account);
            log.info("username: " + account.getUsername());

//            if (objectTokens.isEmpty()) {
//                log.info("objectTokens empty");
//                return new RedirectView(errorUrl);
//            }
            String token = jwtTokenUtil.generateToken(account, ticket,
                tokenExpiredTime);

            log.info("token: " + token);

            UserInformationVsaDTO userInformationVSA =
                new UserInformationVsaDTO(username, objectTokens, token,
                    account.getLanguage());
            ticketRepository.save(ticket, userInformationVSA);
            tokenRepository.save(token, account);
        } catch (Exception e){
            e.printStackTrace();
            return new RedirectView(errorUrl);
        }
        LocalDateTime lastLogin = LocalDateTime.now();
        accountRepository.updateAccountLastLoginByUsername(username, lastLogin);


        if (ticket != null) {
            redirectView.setUrl(redirectUrl + "?ticket=" + ticket);
        } else {
            redirectView.setUrl(redirectUrl);
        }
        return redirectView;
    }

    @GetMapping("/info")
    public ResponseEntity<UserInformationVsaDTO> getUserInformation(@RequestParam String ticket) {
        if (ticketRepository.find(ticket).isPresent()) {
            UserInformationVsaDTO userInformationVsaDTO =
                (UserInformationVsaDTO) ticketRepository.find(ticket).get();
            return ResponseEntity.ok().body(userInformationVsaDTO);
        } else return ResponseEntity.noContent().build();
    }

    @SneakyThrows
    @GetMapping("/logoutVsa")
    public RedirectView logout(HttpServletRequest request,
                               @RequestParam String ticket) {
        Optional<Object> userInfo = ticketRepository.find(ticket);
        if (userInfo.isPresent()) {
            tokenRepository.delete(((UserInformationVsaDTO) userInfo.get()).getAccess_token());
            ticketRepository.delete(ticket);
        }
        request.getSession().invalidate();
        String logoutUrlRedirect =
            logoutUrl + "?service=" + URLEncoder.encode(service,
                String.valueOf(StandardCharsets.UTF_8)) + "&appCode=" + appCode;
        return new RedirectView(logoutUrlRedirect);
    }

    @GetMapping("/refreshToken")
    public ResponseEntity<Object> refreshToken(@RequestParam String ticket) {
        if (!ticketRepository.find(ticket).isPresent()) {
            throw new UnauthorizedException(INVALID_TICKET);
        } else {
            UserInformationVsaDTO userInformationVsaDTO = (UserInformationVsaDTO) ticketRepository.find(ticket).get();
            String username = userInformationVsaDTO.getUsername();
            Account account = accountService.findByUsername(username);
            String token = jwtTokenUtil.generateToken(account, ticket,
                tokenExpiredTime);
            userInformationVsaDTO.setAccess_token(token);
            ticketRepository.save(ticket, userInformationVsaDTO);
            tokenRepository.save(token, accountRepository.findAccountByUsername(username));
            return new ResponseEntity<>(new Token(token), HttpStatus.OK);
        }
    }
}
