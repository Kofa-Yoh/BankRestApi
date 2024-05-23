package com.kotkina.bankrestapi.mappers;

import com.kotkina.bankrestapi.entities.Transaction;
import com.kotkina.bankrestapi.web.models.responses.TransactionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TransactionMapper {
    TransactionResponse transactionToResponse(Transaction transaction);
}
