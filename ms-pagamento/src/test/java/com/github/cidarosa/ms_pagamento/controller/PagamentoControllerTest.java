package com.github.cidarosa.ms_pagamento.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.cidarosa.ms_pagamento.dto.PagamentoDTO;
import com.github.cidarosa.ms_pagamento.service.PagamentoService;
import com.github.cidarosa.ms_pagamento.service.exceptions.ResourceNotFoundException;
import com.github.cidarosa.ms_pagamento.tests.Factory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class PagamentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoService service;

    private PagamentoDTO dto;

    private Long existingId;
    private Long nonExistingId;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() throws Exception {
        existingId = 1L;
        nonExistingId = 100L;

        dto = Factory.createPagamentoDTO();

        List<PagamentoDTO> list = List.of(dto);

        // simulando comportamento do service - getAll
        Mockito.when(service.getAll()).thenReturn(list);

        // simulando o comportamento do service - getById
        // id existe
        Mockito.when(service.getById(existingId)).thenReturn(dto);
        // id não existe - lança exception
        Mockito.when(service.getById(nonExistingId))
                .thenThrow(ResourceNotFoundException.class);

        // simulando o comportamento do service - createPagamento
        // any() simula o comportamento de qualquer objeto
        Mockito.when(service.createPagamento(any())).thenReturn(dto);

        //simulando o comportamento do service - updatePagamento
        // id existe
        Mockito.when(service.updatePagamento(eq(existingId), any())).thenReturn(dto);
        // id não existe
        Mockito.when(service.updatePagamento(eq(nonExistingId), any()))
                .thenThrow(ResourceNotFoundException.class);

    }

    @Test
    public void getAllShouldReturnListPagamentoDTO() throws Exception {

        //chamando a requisição com o método GET no endpoint /pagamentos
        ResultActions result = mockMvc.perform(get("/pagamentos")
                .accept(MediaType.APPLICATION_JSON)
        );
        result.andExpect(status().isOk());
    }

    @Test
    public void getByIdShouldReturnPagamentoDTOWhenIdExists() throws Exception {
        ResultActions result = mockMvc.perform(get("/pagamentos/{id}", existingId)
                .accept(MediaType.APPLICATION_JSON)
        );

        // Assertions
        result.andExpect(status().isOk());
        // verfica e tem os campos em result
        // $ - acessar o objeto result
        result.andExpect(jsonPath("$.id").exists());
        result.andExpect(jsonPath("$.valor").exists());
        result.andExpect(jsonPath("$.status").exists());
    }

    @Test
    public void getByIdShouldReturnResourceNotFoundExceptionWhenIdDoesNotExist() throws Exception {

        ResultActions result = mockMvc.perform(get("/pagamentos/{id}", nonExistingId)
                .accept(MediaType.APPLICATION_JSON)
        );

        // Assertions
        result.andExpect(status().isNotFound());
    }

    @Test
    public void createPagamentoShouldReturnPagamentoDTOCreated() throws Exception {

        PagamentoDTO newPagamentoDTO = Factory.createNewPagamentoDTO();

        // Bean - objectMapper - usado para converter JAVA para JSON
        String jsonRequestBody = objectMapper.writeValueAsString(newPagamentoDTO);

        // POST - tem corpo na requisição (RequestBody) - JSON

        mockMvc.perform(post("/pagamentos")
                        .content(jsonRequestBody) //Request body
                        .contentType(MediaType.APPLICATION_JSON) //request
                        .accept(MediaType.APPLICATION_JSON))  //response
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.valor").exists())
                .andExpect(jsonPath("$.valor").value(32.25))
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.status").isString())
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.pedidoId").exists())
                .andExpect(jsonPath("$.formaDePagamentoId").exists());
    }

    @Test
    public void updatePagamentoShouldReturnPagamentoDTOWhenIdExists() throws Exception {

        String jsonRequestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/pagamentos/{id}", existingId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.id").value(existingId))
                .andExpect(jsonPath("status").exists())
                .andExpect(jsonPath("$.pedidoId").exists())
                .andExpect(jsonPath("$.pedidoId").value(1))
                .andExpect(jsonPath("$.formaDePagamentoId").exists())
                .andExpect(jsonPath("$.formaDePagamentoId").value(2));
    }

    @Test
    public void updatePagamentoShouldThrowResourceNotFoundExceptionWhenIdDoesNotExist() throws Exception {

        String jsonRequestBody = objectMapper.writeValueAsString(dto);

        mockMvc.perform(put("/pagamentos/{id}", nonExistingId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

}
