package com.challenge.clients.service;

import com.challenge.clients.dto.ClientRequestDTO;
import com.challenge.clients.dto.ClientResponseDTO;
import com.challenge.clients.exception.DuplicateResourceException;
import com.challenge.clients.exception.ResourceNotFoundException;
import com.challenge.clients.model.Client;
import com.challenge.clients.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClientServiceImpl implements ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

    private final ClientRepository clientRepository;

    @Override
    public List<ClientResponseDTO> findAll() {
        log.info("Fetching all clients");
        return clientRepository.findAll().stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    public ClientResponseDTO findById(Long id) {
        log.info("Fetching client with id={}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        return toResponseDTO(client);
    }

    @Override
    public List<ClientResponseDTO> searchByName(String name) {
        log.info("Searching clients by name={}", name);
        return clientRepository.searchByName(name).stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public ClientResponseDTO create(ClientRequestDTO request) {
        log.info("Creating client with taxId={}", request.getTaxId());
        checkForDuplicates(request.getTaxId(), request.getEmail(), null);

        Client client = toEntity(request);
        Client saved = clientRepository.save(client);
        log.info("Client created with id={}", saved.getId());
        return toResponseDTO(saved);
    }

    @Override
    @Transactional
    public ClientResponseDTO update(Long id, ClientRequestDTO request) {
        log.info("Updating client with id={}", id);
        Client existing = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));

        checkForDuplicates(request.getTaxId(), request.getEmail(), id);

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setCompanyName(request.getCompanyName());
        existing.setTaxId(request.getTaxId());
        existing.setBirthDate(request.getBirthDate());
        existing.setPhoneNumber(request.getPhoneNumber());
        existing.setEmail(request.getEmail());

        Client updated = clientRepository.save(existing);
        log.info("Client updated with id={}", updated.getId());
        return toResponseDTO(updated);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        log.info("Deleting client with id={}", id);
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
        clientRepository.delete(client);
        log.info("Client deleted with id={}", id);
    }

    private void checkForDuplicates(String taxId, String email, Long excludeId) {
        clientRepository.findByTaxId(taxId).ifPresent(c -> {
            if (!c.getId().equals(excludeId)) {
                throw new DuplicateResourceException("A client with tax ID " + taxId + " already exists");
            }
        });

        clientRepository.findByEmail(email).ifPresent(c -> {
            if (!c.getId().equals(excludeId)) {
                throw new DuplicateResourceException("A client with email " + email + " already exists");
            }
        });
    }

    private ClientResponseDTO toResponseDTO(Client client) {
        return ClientResponseDTO.builder()
                .id(client.getId())
                .firstName(client.getFirstName())
                .lastName(client.getLastName())
                .companyName(client.getCompanyName())
                .taxId(client.getTaxId())
                .birthDate(client.getBirthDate())
                .phoneNumber(client.getPhoneNumber())
                .email(client.getEmail())
                .createdAt(client.getCreatedAt())
                .updatedAt(client.getUpdatedAt())
                .build();
    }

    private Client toEntity(ClientRequestDTO dto) {
        return Client.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .companyName(dto.getCompanyName())
                .taxId(dto.getTaxId())
                .birthDate(dto.getBirthDate())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .build();
    }
}
