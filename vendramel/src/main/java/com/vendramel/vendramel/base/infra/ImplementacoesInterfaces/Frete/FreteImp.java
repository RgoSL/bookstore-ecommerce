package com.vendramel.vendramel.base.infra.ImplementacoesInterfaces.Frete;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.vendramel.vendramel.base.domain.entidades.Endereco;
import com.vendramel.vendramel.base.domain.entidades.ItemCarrinho;
import com.vendramel.vendramel.base.domain.interfaces.FreteInterface;

/**
 * Sistema de Frete (simulado): tabela por faixa de CEP.
 * Um CEP fora das faixas atendidas gera erro.
 */
@Repository
public class FreteImp implements FreteInterface {

    /** Faixa de prefixo de CEP (5 primeiros dígitos) e o valor do frete. */
    private record Faixa(int inicio, int fim, BigDecimal valor) {
    }

    private static final List<Faixa> FAIXAS = List.of(
            // São Paulo (capital)
            new Faixa(1000, 5999, new BigDecimal("15.00")),
            new Faixa(8000, 8499, new BigDecimal("15.00")),
            // Osasco e região
            new Faixa(6000, 6999, new BigDecimal("18.00")),
            // Guarulhos
            new Faixa(7000, 7399, new BigDecimal("18.00")),
            // Grande ABC
            new Faixa(9000, 9999, new BigDecimal("20.00")),
            // Santos
            new Faixa(11000, 11099, new BigDecimal("35.00")),
            // Campinas
            new Faixa(13000, 13149, new BigDecimal("30.00"))
    );

    @Override
    public BigDecimal calcularFrete(
            Endereco endereco,
            List<ItemCarrinho> itens) {

        if (itens == null || itens.isEmpty()) {
            throw new IllegalStateException(
                    "Adicione ao menos um livro ao carrinho");
        }

        if (endereco == null || endereco.getCep() == null) {
            throw new IllegalArgumentException(
                    "Não foi possível calcular o frete");
        }

        String digitos = endereco.getCep().replaceAll("\\D", "");

        if (digitos.length() != 8) {
            throw new IllegalArgumentException(
                    "Não foi possível calcular o frete");
        }

        int prefixo = Integer.parseInt(digitos.substring(0, 5));

        for (Faixa faixa : FAIXAS) {
            if (prefixo >= faixa.inicio() && prefixo <= faixa.fim()) {
                return faixa.valor();
            }
        }

        throw new IllegalArgumentException("Não foi possível calcular o frete");
    }
}
