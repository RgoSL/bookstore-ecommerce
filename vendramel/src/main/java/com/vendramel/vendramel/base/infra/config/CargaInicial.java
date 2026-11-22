package com.vendramel.vendramel.base.infra.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.vendramel.vendramel.base.domain.regras.FormatoLivro;
import com.vendramel.vendramel.base.infra.entidadesFrameWork.Cliente;
import com.vendramel.vendramel.base.infra.entidadesFrameWork.Endereco;
import com.vendramel.vendramel.base.infra.entidadesFrameWork.Livro;
import com.vendramel.vendramel.base.infra.repository.Cliente.ClienteJpaRepository;
import com.vendramel.vendramel.base.infra.repository.Endereco.EnderecoJpaRepository;
import com.vendramel.vendramel.base.infra.repository.Livro.LivroJpaRepository;

/**
 * Carga inicial (o H2 em memória é apagado a cada reinício).
 * Cria o cliente 1 (com endereço 1) e quatro livros, todos com estoque 10:
 * 1 capa dura, 2 brochura e 3 digital (R$ 100,00 cada) e
 * 4 capa dura de R$ 1.000,00 (massa de teste da recusa do cartão).
 */
@Configuration
public class CargaInicial {

    @Bean
    public CommandLineRunner carregarDados(
            EnderecoJpaRepository enderecoRepository,
            ClienteJpaRepository clienteRepository,
            LivroJpaRepository livroRepository) {

        return args -> {
            if (clienteRepository.count() > 0) {
                return;
            }

            Endereco endereco = new Endereco();
            endereco.setLogradouro("Rua das Flores");
            endereco.setNumero("100");
            endereco.setBairro("Centro");
            endereco.setCidade("São Paulo");
            endereco.setUf("SP");
            endereco.setCep("01001-000");
            endereco = enderecoRepository.save(endereco);

            Cliente cliente = new Cliente();
            cliente.setNomeCompleto("João Pedro");
            cliente.setCpf("12345678901");
            cliente.setDataNascimento(LocalDateTime.of(2002, 5, 15, 0, 0));
            cliente.setEmail("joao@email.com");
            cliente.setTelefone("11999999999");
            cliente.setSenha("123456");
            cliente.setEndereco(endereco);
            clienteRepository.save(cliente);

            livroRepository.save(livro(
                    "Arquitetura Limpa", FormatoLivro.CAPA_DURA, "100.00"));
            livroRepository.save(livro(
                    "Código Limpo", FormatoLivro.BROCHURA, "100.00"));
            livroRepository.save(livro(
                    "Refatoração", FormatoLivro.DIGITAL, "100.00"));
            livroRepository.save(livro(
                    "Coleção Engenharia de Software",
                    FormatoLivro.CAPA_DURA, "1000.00"));
        };
    }

    private Livro livro(String titulo, String formato, String preco) {
        Livro livro = new Livro();
        livro.setTitulo(titulo);
        livro.setFormato(formato);
        livro.setPrecoVenda(new BigDecimal(preco));
        livro.setQuantidadeEstoque(10);
        livro.setStatus("DISPONIVEL");
        return livro;
    }
}
