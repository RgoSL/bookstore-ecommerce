package com.vendramel.vendramel.base.infra.mapper.ItemCarrinho;

import com.vendramel.vendramel.base.domain.regras.RegraPreco;
import com.vendramel.vendramel.base.infra.mapper.Livro.LivroMapper;

public class ItemCarrinhoMapper {

    public static com.vendramel.vendramel.base.domain.entidades.ItemCarrinho paraDominio(
            com.vendramel.vendramel.base.infra.entidadesFrameWork.ItemCarrinho item) {

        if (item == null) {
            return null;
        }

        var dominio = new com.vendramel.vendramel.base.domain.entidades.ItemCarrinho();
        dominio.setId(item.getId());
        dominio.setQuantidade(item.getQuantidade());

        if (item.getLivro() != null) {
            dominio.setLivro(LivroMapper.paraDominio(item.getLivro()));

            // RN02: o preço exibido depende do formato do livro
            dominio.setPrecoUnitario(RegraPreco.aplicar(
                    item.getLivro().getFormato(),
                    item.getLivro().getPrecoVenda()));
        }

        return dominio;
    }
}
