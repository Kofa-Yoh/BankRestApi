package com.kotkina.bankrestapi.repositories;

import com.kotkina.bankrestapi.entities.Client;
import com.kotkina.bankrestapi.web.models.requests.ClientFilter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public interface ClientSpecification {

    static Specification<Client> withFilter(ClientFilter clientFilter) {
        return Specification.where(byEmailIgnoreCase(clientFilter.getEmail()))
                .and(byPhone(clientFilter.getPhone()))
                .and(byBirthdateAfter(clientFilter.getBirthdate()))
                .and(byLastnameLikeIgnoreCase(clientFilter.getLastname()))
                .and(byFirstnameLikeIgnoreCase(clientFilter.getFirstname()))
                .and(byPatronymicLikeIgnoreCase(clientFilter.getPatronymic()));
    }

    static Specification<Client> byLastnameLikeIgnoreCase(String lastname) {
        return (root, query, criteriaBuilder) -> {
            if (lastname == null) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("lastname")),
                    lastname.toLowerCase() + "%");
        };
    }

    static Specification<Client> byFirstnameLikeIgnoreCase(String firstname) {
        return (root, query, criteriaBuilder) -> {
            if (firstname == null) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("firstname")),
                    firstname.toLowerCase() + "%");
        };
    }

    static Specification<Client> byPatronymicLikeIgnoreCase(String patronymic) {
        return (root, query, criteriaBuilder) -> {
            if (patronymic == null) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("patronymic")),
                    patronymic.toLowerCase() + "%");
        };
    }

    static Specification<Client> byEmailIgnoreCase(String email) {
        return (root, query, criteriaBuilder) -> {
            if (email == null) {
                return null;
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("email")), email.toLowerCase());
        };
    }

    static Specification<Client> byPhone(String phone) {
        return (root, query, criteriaBuilder) -> {
            if (phone == null) {
                return null;
            }
            return criteriaBuilder.equal(root.get("phone"), phone);
        };
    }

    static Specification<Client> byBirthdateAfter(LocalDate birthdate) {
        return (root, query, criteriaBuilder) -> {
            if (birthdate == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("birthdate"), birthdate);
        };
    }
}
