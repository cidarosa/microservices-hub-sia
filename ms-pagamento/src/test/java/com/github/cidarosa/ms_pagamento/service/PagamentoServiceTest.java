package com.github.cidarosa.ms_pagamento.service;

import com.github.cidarosa.ms_pagamento.dto.PagamentoDTO;
import com.github.cidarosa.ms_pagamento.entity.Pagamento;
import com.github.cidarosa.ms_pagamento.repository.PagamentoRepository;
import com.github.cidarosa.ms_pagamento.service.exceptions.ResourceNotFoundException;
import com.github.cidarosa.ms_pagamento.tests.Factory;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class PagamentoServiceTest {

    @InjectMocks
    private PagamentoService service;

    @Mock
    private PagamentoRepository repository;

    private Long existingId;
    private Long nonExistingId;

    private Pagamento pagamento;
    private PagamentoDTO dto;

    @BeforeEach
    void setup() throws Exception {
        existingId = 1L;
        nonExistingId = 10L;

        Mockito.when(repository.existsById(existingId)).thenReturn(true);
        Mockito.when(repository.existsById(nonExistingId)).thenReturn(false);
        Mockito.doNothing().when(repository).deleteById(existingId);

        pagamento = Factory.createPagamento();
        dto = new PagamentoDTO(pagamento);

        // getById (findById)
        Mockito.when(repository.findById(existingId)).thenReturn(Optional.of(pagamento));
        Mockito.when(repository.findById(nonExistingId)).thenReturn(Optional.empty());

        // createPagamento (insert)
        Mockito.when(repository.save(any())).thenReturn(pagamento);

        //updatePagamento (update) - id existe
        Mockito.when(repository.getReferenceById(existingId)).thenReturn(pagamento);
        //updatePagamento (update) - id não existe
        Mockito.when(repository.getReferenceById(nonExistingId)).thenThrow(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("delete Deveria não fazer nada quando Id existe")
    public void deleteShouldDoNothingWhenIdExists() {

        Assertions.assertDoesNotThrow(
                () -> {
                    service.deletePagamento(existingId);
                }
        );

    }

    @Test
    @DisplayName("delete Deveria lançar exceção ResourceNotFoundException quando Id não existe")
    public void deleteShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> {
                    service.deletePagamento(nonExistingId);
                });
    }

    @Test
    public void getByIdShouldReturnPagamentoDTOWhenIdExists() {
        dto = service.getById(existingId);

        Assertions.assertNotNull(dto);
        Assertions.assertEquals(dto.getId(), existingId);
        Assertions.assertEquals(dto.getValor(), pagamento.getValor());
    }

    @Test
    public void getByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {
        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> {
                    service.getById(nonExistingId);
                }
        );
    }

    @Test
    public void createPagamentoShouldReturnPagamentoDTOWhenPagamentoIsCreated() {

        dto = service.createPagamento(dto);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(dto.getId(), pagamento.getId());
        Assertions.assertEquals(dto.getNome(), pagamento.getNome());
    }

    @Test
    public void updatePagamentoShouldReturnPagamentoDTOWhenIdExists() {

        service.updatePagamento(existingId, dto);
        Assertions.assertNotNull(dto);
        Assertions.assertEquals(dto.getId(), existingId);
        Assertions.assertEquals(dto.getValor(), pagamento.getValor());
    }

    @Test
    public void updatePagamentoShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() {

        Assertions.assertThrows(ResourceNotFoundException.class,
                () -> {
                    service.updatePagamento(nonExistingId, dto);
                });
    }
}

