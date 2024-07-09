package com.viettel.importwiz.service;

import com.viettel.importwiz.dto.accounts.CreateAccountDto;
import com.viettel.importwiz.dto.accounts.UpdateAccountDto;
import com.viettel.importwiz.entity.Account;
import com.viettel.importwiz.exception.custom.BusinessException;
import com.viettel.importwiz.exception.custom.RecordNotFoundException;
import com.viettel.importwiz.repository.AccountRepository;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.viettel.importwiz.constant.error.ErrorCodes.*;

@Service
@AllArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    private final ModelMapper mapper;


    public Page<Account> findAllPaging(Pageable pageable, String keyword) {
        return this.accountRepository.findAllPaging(keyword, pageable);
    }

    public List<Account> findAll() {
        return this.accountRepository.findAll();
    }

    public Account findByUsername(String username) {
        Account account = this.accountRepository.findAccountByUsername(username);
        if (account == null) {
            throw new BusinessException(ACCOUNT_NOT_EXIST);
        }
        return account;
    }

    public List<Account> findAllByUsername(String keyword) {
        List<Account> listAccount;
        listAccount = this.accountRepository.findAllByUsername(keyword);

        return listAccount;
    }

    public Account createAccount(CreateAccountDto createAccountDto) {
        boolean isUsernameAlreadyExist = isUsernameAlreadyExist(createAccountDto.getUsername());
        boolean isEmailAlreadyExist = isEmailAlreadyExist(createAccountDto.getEmail());
        if (isUsernameAlreadyExist) {
            throw new BusinessException(USERNAME_ALREADY_EXIST);
        } else if (isEmailAlreadyExist) {
            throw new BusinessException(EMAIL_ALREADY_EXIST);
        }

        Account acc = this.mapper.map(createAccountDto, Account.class);

        return this.accountRepository.save(acc);
    }

    public Account updateAccountForAdmin(Integer accountId, UpdateAccountDto updateAccountDto) {
        Account account = this.accountRepository.findByAccountId1(accountId);

        if (account == null) {
            throw new RecordNotFoundException(ACCOUNT_NOT_EXIST);
        }

        account.setRole(updateAccountDto.getRole());

        return this.accountRepository.save(account);
    }

    public List<Account> findAllAdmin() {
        List<Account> adminAccList;
        adminAccList = this.accountRepository.findAllAdminAccount();
        return adminAccList;
    }

    public void deleteAccount(Integer id, Integer myId) {
        Account account = this.accountRepository.findByAccountId1(id);

        if (account == null || account.getAccountId() == myId) {
            throw new RecordNotFoundException(ACCOUNT_NOT_EXIST);
        }
        this.accountRepository.deleteAccountById(id);
    }

    public Boolean isUsernameAlreadyExist(String username) {
        Account account = this.accountRepository.findAccountByUsername(username);

        return account != null;
    }

    public Boolean isEmailAlreadyExist(String email) {
        Account account = this.accountRepository.findAccountByEmail(email);

        return account != null;
    }
}
