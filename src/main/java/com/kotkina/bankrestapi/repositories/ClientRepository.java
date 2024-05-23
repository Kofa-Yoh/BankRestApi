package com.kotkina.bankrestapi.repositories;

import com.kotkina.bankrestapi.entities.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    @Override
    @EntityGraph("client-with-account")
    Optional<Client> findById(Long aLong);

    @EntityGraph("client-with-account")
    Page<Client> findAll(Specification<Client> spec, Pageable pageable);

    Boolean existsByPhoneIgnoreCase(String phone);

    Boolean existsByEmailIgnoreCase(String email);
}
