package com.challenge.clients.service;

import com.challenge.clients.dto.ClientRequestDTO;
import com.challenge.clients.dto.ClientResponseDTO;

import java.util.List;

public interface ClientService {

    List<ClientResponseDTO> findAll();

    ClientResponseDTO findById(Long id);

    List<ClientResponseDTO> searchByName(String name);

    ClientResponseDTO create(ClientRequestDTO request);

    ClientResponseDTO update(Long id, ClientRequestDTO request);

    void delete(Long id);
}
