package com.myproject.busticket.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.myproject.busticket.dto.AccountDTO;
import com.myproject.busticket.models.Account;

@Mapper(componentModel = "spring")
public interface AccountMapper {
    AccountDTO entityToDTO(AccountDTO account);

    AccountDTO dtoToEntity(AccountDTO account);

    List<AccountDTO> map(List<Account> accounts);
}
