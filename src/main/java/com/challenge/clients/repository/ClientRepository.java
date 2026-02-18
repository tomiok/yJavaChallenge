package com.challenge.clients.repository;

import com.challenge.clients.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    Optional<Client> findByTaxId(String taxId);

    Optional<Client> findByEmail(String email);

    @Query(value = "SELECT * FROM search_clients_by_name(:name)", nativeQuery = true)
    List<Client> searchByName(@Param("name") String name);
}
