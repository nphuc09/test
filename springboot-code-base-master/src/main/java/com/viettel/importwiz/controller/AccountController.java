package com.viettel.importwiz.controller;

import com.viettel.importwiz.entity.Account;
import com.viettel.importwiz.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@Data
@AllArgsConstructor
@RestController
@Tag(name = "Accounts")
@RequestMapping("/accounts")
@SecurityRequirement(name = "Bearer Authentication")
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/fetch-me")
    @Operation(summary = "fetch me")
    public ResponseEntity<Account> fetchMe(HttpServletRequest request) {
        Account account = (Account) request.getAttribute("account");
        return new ResponseEntity<>(account, HttpStatus.OK);
    }
}
