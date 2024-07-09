package com.viettel.importwiz.repository;

import com.viettel.importwiz.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {

    @Query(value = "SELECT * FROM account a " +
        "WHERE LOWER(a.username) = LOWER(?1) " +
        "AND deleted = false", nativeQuery = true
    )
    Account findAccountByUsername(String username);

    @Query(value = "SELECT * FROM account a " +
        "WHERE LOWER(a.email) = LOWER(?1)" +
        "AND a.deleted = false", nativeQuery = true
    )
    Account findAccountByEmail(String email);

    @Query(value = "SELECT * FROM account " +
        "WHERE account_id = ?1 AND deleted = false", nativeQuery = true
    )
    Account findByAccountId1(int accountId);

    @Query(value = "UPDATE account SET last_login = ?2 " +
        "WHERE LOWER(username) = LOWER(?1) " +
        "AND deleted = false", nativeQuery = true
    )
    void updateAccountLastLoginByUsername(String username, LocalDateTime lastLogin);

    @Query(value = "UPDATE account SET deleted = true " +
        "WHERE account_id = ?1", nativeQuery = true
    )
    void deleteAccountById(Integer id);

    @Query(value = "SELECT a FROM Account a " +
        "WHERE (LOWER(a.email) LIKE LOWER('%' || ?1 || '%') " +
        "OR LOWER(a.username) LIKE LOWER('%' || ?1 || '%') " +
        "OR LOWER(a.fullName) LIKE LOWER('%' || ?1 || '%'))" +
        "AND a.deleted = false"
    )
    Page<Account> findAllPaging(String keyword, Pageable pageable);

    @Query(value = "SELECT a FROM Account a " +
        "WHERE LOWER(a.username) LIKE LOWER('%' || ?1 || '%') AND a.deleted = false"
    )
    List<Account> findAllByUsername(String keyword);

    @Query(value = "SELECT * FROM account WHERE role = 'KPIC_SYSTEM_ADMIN' AND deleted = false", nativeQuery = true)
    List<Account> findAllAdminAccount();
}

