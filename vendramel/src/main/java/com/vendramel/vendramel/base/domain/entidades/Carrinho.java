package com.vendramel.vendramel.base.domain.entidades;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Carrinho {
    
    private int id;
    private LocalDateTime dataCriacao;
    private List<ItemCarrinho> itens;
    private BigDecimal subtotal;


    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }
    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }
    public List<ItemCarrinho> getItens() {
        return itens;
    }
    public void setItens(List<ItemCarrinho> itens) {
        this.itens = itens;
    }
    


    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }
}
