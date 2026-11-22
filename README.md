# Livraria Vendramel — Efetuar Pedido

Aplicação de e-commerce de livraria. O caso de uso implementado é o **CSU02 – Efetuar Pedido**.

- **Back-end:** Java 21, Spring Boot, Spring Data JPA e H2 em memória (pasta `vendramel/`).
- **Front-end:** React + Vite, tela única (pasta `frontend/`).

## Como executar

**Back-end** (porta 8080):

```bash
cd vendramel
./mvnw spring-boot:run       
```

**Front-end** (porta 5173), em outro terminal:

```bash
cd frontend
npm install
npm run dev
```

Abra http://localhost:5173. O Vite encaminha as chamadas `/api/...` para o back-end (proxy), sem problema de CORS.

> O H2 é apagado a cada reinício (`create-drop`). **Reinicie o back-end antes de cada caso de teste**: a carga inicial devolve o estoque a 10 e o carrinho ao estado inicial.

## Carga inicial

| Item | Dados |
|---|---|
| Cliente 1 | João Pedro, endereço 1 (CEP 01001-000, São Paulo) |
| Livro 1 | Arquitetura Limpa, **capa dura**, R$ 100,00, estoque 10 |
| Livro 2 | Código Limpo, **brochura**, R$ 100,00, estoque 10 |
| Livro 3 | Refatoração, **digital**, R$ 100,00, estoque 10 |
| Livro 4 | Coleção Engenharia de Software, capa dura, R$ 1.000,00, estoque 10 (massa B: recusa do cartão) |

## Regras de negócio implementadas

- **RN02 (preço por formato):** capa dura sem desconto, brochura -5%, digital -10%. Livro de R$ 100,00 custa R$ 100,00, R$ 95,00 e R$ 90,00. Usado no carrinho e no item do pedido.
- **RN04 (pagamento):**
  - Cartão à vista: 3% de desconto. Cartão em 2x ou 3x: sem juros. **Acima de 3x: erro.**
  - Pix: somente à vista, 8% de desconto.
  - Os descontos incidem sobre subtotal + frete.
- **RN05 (status do pedido):** `EM_PROCESSAMENTO` → `CONFIRMADO` → `EM_TRANSPORTE` → `FINALIZADO`, ou `CANCELADO` (só a partir de `EM_PROCESSAMENTO`). O pagamento aprovado leva a `CONFIRMADO`, atualiza o estoque e destrói os itens do carrinho.

### Integrações simuladas

- **Sistema de Frete (`FreteImp`):** tabela por faixa de CEP. CEP fora das faixas gera o erro "Não foi possível calcular o frete". Faixas: 01000-05999 e 08000-08499 (R$ 15,00), 06000-06999 e 07000-07399 (R$ 18,00), 09000-09999 (R$ 20,00), 13000-13149 (R$ 30,00) e 11000-11099 (R$ 35,00). Exemplo válido: `01001-000`. Exemplo inválido: `00000-000`.
- **Sistema de Cartão (`PagamentoCartao`):** recusa a **1ª tentativa** de pagamento de um pedido com total **>= R$ 1.000,00**. As seguintes são aprovadas. Com isso, o livro 4 (total R$ 1.015,00) serve para testar a recusa, a desistência e a nova tentativa.
- **Sistema de Banco (`PagamentoPix`):** aprova sempre, com confirmação imediata.

### Mensagens do sistema

`Adicione ao menos um livro ao carrinho` · `Não foi possível calcular o frete` · `Pagamento não autorizado` · `Parcelamento acima de 3x não é permitido (RN04).`

As respostas de erro têm o formato `{"mensagem": "..."}` (veja `ApiExceptionHandler`).

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/livros` | Lista os livros (com estoque) |
| GET | `/carrinhos/{idCliente}` | Carrinho com livro, preço do formato (RN02) e subtotal |
| POST | `/carrinhos/itens` | Adiciona item: `{"idCliente":1,"idLivro":1,"quantidade":1}` |
| PATCH | `/carrinhos/itens/{idItem}/quantidade` | Altera a quantidade: `{"quantidade":2}` |
| DELETE | `/carrinhos/itens/{idItem}` | Remove o item |
| POST | `/enderecos` | Cadastra endereço de entrega |
| POST | `/pedidos?idCliente=1&idEndereco=2` | Efetua o pedido (calcula o frete; status `EM_PROCESSAMENTO`) |
| GET | `/pedidos/{id}` | Consulta o pedido |
| PATCH | `/pedidos/{id}/cancelar` | Desistência (só `EM_PROCESSAMENTO`) |
| PATCH | `/pedidos/{id}/enviar` | `CONFIRMADO` → `EM_TRANSPORTE` (fora do CSU02) |
| PATCH | `/pedidos/{id}/entregar` | `EM_TRANSPORTE` → `FINALIZADO` (fora do CSU02) |
| GET | `/pagamentos/simulacao?valor=300&tipo=CARTAO&parcelas=1` | Valor final (RN04), sem gravar nada |
| POST | `/pagamentos` | Paga: `{"pedidoId":1,"tipo":"CARTAO","parcelas":1}` (`tipo`: `CARTAO` ou `PIX`) |

Não existe endpoint para "finalizar" o pedido sem pagamento: a confirmação só ocorre pelo pagamento aprovado.

## Premissas e limitações

- Autenticação fora do escopo: o cliente 1 é fixo na tela e o Spring Security libera todas as rotas.
- O cliente tem **um** endereço cadastrado. O endereço de entrega é informado em cada pedido.
- O carrinho vazio é verificado na tela (ao confirmar o pedido) e também no back-end (ao registrar o pedido).