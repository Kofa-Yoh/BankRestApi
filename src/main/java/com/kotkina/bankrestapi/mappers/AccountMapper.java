package com.kotkina.bankrestapi.mappers;

import com.kotkina.bankrestapi.entities.Account;
import com.kotkina.bankrestapi.web.models.responses.AccountResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AccountMapper {
    AccountResponse accountToResponse(Account account);
}
