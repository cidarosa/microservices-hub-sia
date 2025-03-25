package com.github.cidarosa.ms_pagamento.repository;

import com.github.cidarosa.ms_pagamento.entity.Pagamento;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

@DataJpaTest
public class PagamentoRepositoryTest {

    @Autowired
    private PagamentoRepository repository;

    private Long existingId;
    private Long nonExistingId;

    @BeforeEach
    void setup() throws Exception{
        existingId = 1L;
        nonExistingId = 100L;
    }

    @Test
    public void deleteShouldDeleteObjectWhenIdExists(){

        //ACT
        repository.deleteById(existingId);
        //Assertions
        Optional<Pagamento> result = repository.findById(existingId);
        Assertions.assertFalse(result.isPresent());
    }


}
