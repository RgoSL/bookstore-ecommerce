import { useCallback, useEffect, useState } from 'react'
import { api } from './api.js'

const CLIENTE_ID = 1

const ENDERECO_PADRAO = {
  cep: '01001-000',
  logradouro: 'Rua das Flores',
  numero: '100',
  bairro: 'Centro',
  cidade: 'São Paulo',
  uf: 'SP',
}

const ROTULO_STATUS = {
  EM_PROCESSAMENTO: 'Em processamento',
  CONFIRMADO: 'Confirmado',
  EM_TRANSPORTE: 'Em transporte',
  FINALIZADO: 'Finalizado',
  CANCELADO: 'Cancelado',
}

const ROTULO_FORMATO = {
  CAPA_DURA: 'Capa dura',
  BROCHURA: 'Brochura',
  DIGITAL: 'Digital',
}

const MSG_CARRINHO_VAZIO = 'Adicione ao menos um livro ao carrinho'

const brl = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const dinheiro = (valor) => brl.format(Number(valor ?? 0))

function Status({ valor }) {
  return (
    <span className={`status status-${valor}`}>{ROTULO_STATUS[valor] ?? valor}</span>
  )
}

function Passos({ atual }) {
  const passos = ['Carrinho', 'Endereço e frete', 'Pagamento', 'Resultado']
  return (
    <ol className="passos" aria-label="Etapas do pedido">
      {passos.map((nome, i) => {
        const numero = i + 1
        const classe =
          numero < atual ? 'feito' : numero === atual ? 'atual' : 'pendente'
        return (
          <li key={nome} className={`passo ${classe}`}>
            <span className="passo-numero">{numero}</span>
            <span className="passo-nome">{nome}</span>
          </li>
        )
      })}
    </ol>
  )
}

export default function App() {
  const [livros, setLivros] = useState([])
  const [carrinho, setCarrinho] = useState({ itens: [], subtotal: 0 })
  const [quantidades, setQuantidades] = useState({})
  const [confirmouCarrinho, setConfirmouCarrinho] = useState(false)
  const [endereco, setEndereco] = useState(ENDERECO_PADRAO)
  const [pedido, setPedido] = useState(null)
  const [forma, setForma] = useState('CARTAO')
  const [condicao, setCondicao] = useState('VISTA')
  const [parcelas, setParcelas] = useState(2)
  const [simulacao, setSimulacao] = useState(null)
  const [erroSimulacao, setErroSimulacao] = useState('')
  const [pagamento, setPagamento] = useState(null)
  const [mensagem, setMensagem] = useState(null)
  const [ocupado, setOcupado] = useState(false)

  const itens = carrinho.itens ?? []
  const pedidoAberto = pedido !== null && pedido.status === 'EM_PROCESSAMENTO'
  const pedidoEncerrado =
    pedido !== null &&
    (pedido.status === 'CONFIRMADO' || pedido.status === 'CANCELADO')
  const carrinhoTravado = pedido !== null
  const parcelasEfetivas =
    forma === 'PIX' || condicao === 'VISTA' ? 1 : Number(parcelas)

  let etapa = 1
  if (pedidoEncerrado) etapa = 4
  else if (pedidoAberto) etapa = 3
  else if (confirmouCarrinho) etapa = 2

  const recarregar = useCallback(async () => {
    try {
      const [listaLivros, carrinhoAtual] = await Promise.all([
        api.listarLivros(),
        api.visualizarCarrinho(CLIENTE_ID),
      ])
      setLivros(listaLivros)
      setCarrinho(carrinhoAtual)
    } catch (e) {
      setMensagem({ tipo: 'erro', texto: e.message })
    }
  }, [])

  useEffect(() => {
    recarregar()
  }, [recarregar])

  // Mostra o valor final (RN04) sempre que a forma de pagamento muda
  useEffect(() => {
    if (!pedidoAberto) {
      setSimulacao(null)
      setErroSimulacao('')
      return undefined
    }
    let cancelado = false
    api
      .simularPagamento(pedido.valorTotal, forma, parcelasEfetivas)
      .then((resultado) => {
        if (!cancelado) {
          setSimulacao(resultado)
          setErroSimulacao('')
        }
      })
      .catch((e) => {
        if (!cancelado) {
          setSimulacao(null)
          setErroSimulacao(e.message)
        }
      })
    return () => {
      cancelado = true
    }
  }, [pedido, pedidoAberto, forma, parcelasEfetivas])

  async function executar(acao) {
    setOcupado(true)
    setMensagem(null)
    try {
      return await acao()
    } catch (e) {
      setMensagem({ tipo: 'erro', texto: e.message })
      return undefined
    } finally {
      setOcupado(false)
    }
  }

  async function adicionar(livro) {
    const quantidade = Number(quantidades[livro.id] ?? 1)
    const resposta = await executar(() =>
      api.adicionarItem(CLIENTE_ID, livro.id, quantidade)
    )
    if (resposta !== undefined) await recarregar()
  }

  async function alterar(item, delta) {
    const nova = item.quantidade + delta
    if (nova < 1) return
    const resposta = await executar(() => api.alterarQuantidade(item.id, nova))
    if (resposta !== undefined) await recarregar()
  }

  async function remover(item) {
    await executar(() => api.removerItem(item.id))
    await recarregar()
  }

  function confirmarCarrinho() {
    setMensagem(null)
    if (itens.length === 0) {
      setMensagem({ tipo: 'erro', texto: MSG_CARRINHO_VAZIO })
      return
    }
    setConfirmouCarrinho(true)
  }

  async function registrarPedido() {
    const criado = await executar(async () => {
      const enderecoSalvo = await api.cadastrarEndereco(endereco)
      return api.efetuarPedido(CLIENTE_ID, enderecoSalvo.id)
    })
    if (criado) {
      setPedido(criado)
      setMensagem({
        tipo: 'info',
        texto: `Pedido #${criado.id} registrado. Escolha a forma de pagamento.`,
      })
    }
  }

  async function pagar() {
    const resultado = await executar(() =>
      api.pagar(pedido.id, forma, parcelasEfetivas)
    )
    if (resultado) {
      setPagamento(resultado)
      const atualizado = await executar(() => api.buscarPedido(pedido.id))
      if (atualizado) setPedido(atualizado)
      await recarregar()
      setMensagem({ tipo: 'ok', texto: 'Pagamento aprovado. Pedido confirmado.' })
    }
  }

  async function desistir() {
    const cancelado = await executar(() => api.cancelarPedido(pedido.id))
    if (cancelado) {
      setPedido(cancelado)
      setMensagem({ tipo: 'info', texto: 'Compra cancelada. O estoque não foi alterado.' })
    }
  }

  function novoPedido() {
    setPedido(null)
    setPagamento(null)
    setConfirmouCarrinho(false)
    setEndereco(ENDERECO_PADRAO)
    setForma('CARTAO')
    setCondicao('VISTA')
    setParcelas(2)
    setMensagem(null)
    recarregar()
  }

  function campoEndereco(campo) {
    return (evento) =>
      setEndereco((atual) => ({ ...atual, [campo]: evento.target.value }))
  }

  return (
    <div className="app">
      <header className="topo">
        <div>
          <h1>Livraria Vendramel</h1>
          <p className="subtitulo">Efetuar pedido</p>
        </div>
        <div className="cliente">Cliente: João Pedro (id {CLIENTE_ID})</div>
      </header>

      <Passos atual={etapa} />

      {mensagem && (
        <div className={`aviso aviso-${mensagem.tipo}`} role="alert">
          {mensagem.texto}
        </div>
      )}

      <main className="grade">
        <div className="coluna">
          <section className="painel" aria-labelledby="titulo-livros">
            <h2 id="titulo-livros">1. Livros</h2>
            <table className="tabela">
              <thead>
                <tr>
                  <th>Livro</th>
                  <th>Formato</th>
                  <th className="num">Preço base</th>
                  <th className="num">Estoque</th>
                  <th>Qtd</th>
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {livros.map((livro) => (
                  <tr key={livro.id}>
                    <td>{livro.titulo}</td>
                    <td>{ROTULO_FORMATO[livro.formato] ?? livro.formato}</td>
                    <td className="num">{dinheiro(livro.precoVenda)}</td>
                    <td className="num" data-testid={`estoque-${livro.id}`}>
                      {livro.quantidadeEstoque}
                    </td>
                    <td>
                      <input
                        className="qtd"
                        type="number"
                        min="1"
                        aria-label={`Quantidade de ${livro.titulo}`}
                        value={quantidades[livro.id] ?? 1}
                        disabled={carrinhoTravado}
                        onChange={(e) =>
                          setQuantidades((atual) => ({
                            ...atual,
                            [livro.id]: e.target.value,
                          }))
                        }
                      />
                    </td>
                    <td>
                      <button
                        className="botao secundario"
                        disabled={ocupado || carrinhoTravado}
                        onClick={() => adicionar(livro)}
                      >
                        Adicionar
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
            <p className="legenda">
              RN02: capa dura sem desconto, brochura -5% e digital -10%. O preço
              com o desconto do formato aparece no carrinho.
            </p>
          </section>

          <section className="painel" aria-labelledby="titulo-carrinho">
            <h2 id="titulo-carrinho">Carrinho</h2>
            {itens.length === 0 ? (
              <p className="vazio" data-testid="carrinho-vazio">
                Carrinho vazio.
              </p>
            ) : (
              <ul className="itens">
                {itens.map((item) => (
                  <li key={item.id} className="item">
                    <div className="item-info">
                      <strong>{item.livro?.titulo}</strong>
                      <span className="pequeno">
                        {ROTULO_FORMATO[item.livro?.formato] ?? item.livro?.formato}
                        {' · '}
                        {dinheiro(item.precoUnitario)} cada
                      </span>
                    </div>
                    <div className="item-qtd">
                      <button
                        className="botao secundario mini"
                        aria-label={`Diminuir ${item.livro?.titulo}`}
                        disabled={ocupado || carrinhoTravado || item.quantidade <= 1}
                        onClick={() => alterar(item, -1)}
                      >
                        -
                      </button>
                      <span className="qtd-valor" aria-label="Quantidade">
                        {item.quantidade}
                      </span>
                      <button
                        className="botao secundario mini"
                        aria-label={`Aumentar ${item.livro?.titulo}`}
                        disabled={ocupado || carrinhoTravado}
                        onClick={() => alterar(item, 1)}
                      >
                        +
                      </button>
                    </div>
                    <div className="item-total">
                      {dinheiro(Number(item.precoUnitario) * item.quantidade)}
                    </div>
                    <button
                      className="botao link"
                      disabled={ocupado || carrinhoTravado}
                      onClick={() => remover(item)}
                    >
                      Remover {item.livro?.titulo}
                    </button>
                  </li>
                ))}
              </ul>
            )}
            <div className="linha total">
              <span>Subtotal</span>
              <strong data-testid="subtotal">{dinheiro(carrinho.subtotal)}</strong>
            </div>
            <button
              className="botao primario"
              disabled={ocupado || carrinhoTravado}
              onClick={confirmarCarrinho}
            >
              Confirmar pedido
            </button>
          </section>
        </div>

        <div className="coluna">
          <section
            className={`painel ${etapa < 2 ? 'inativo' : ''}`}
            aria-labelledby="titulo-endereco"
          >
            <h2 id="titulo-endereco">2. Endereço de entrega e frete</h2>
            <div className="formulario">
              <label>
                CEP
                <input
                  value={endereco.cep}
                  onChange={campoEndereco('cep')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
              <label>
                Logradouro
                <input
                  value={endereco.logradouro}
                  onChange={campoEndereco('logradouro')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
              <label>
                Número
                <input
                  value={endereco.numero}
                  onChange={campoEndereco('numero')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
              <label>
                Bairro
                <input
                  value={endereco.bairro}
                  onChange={campoEndereco('bairro')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
              <label>
                Cidade
                <input
                  value={endereco.cidade}
                  onChange={campoEndereco('cidade')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
              <label>
                UF
                <input
                  value={endereco.uf}
                  onChange={campoEndereco('uf')}
                  disabled={!confirmouCarrinho || carrinhoTravado}
                />
              </label>
            </div>
            <button
              className="botao primario"
              disabled={ocupado || !confirmouCarrinho || carrinhoTravado}
              onClick={registrarPedido}
            >
              Calcular frete e registrar pedido
            </button>

            {pedido && (
              <div className="resumo" data-testid="resumo-pedido">
                <div className="linha">
                  <span>Pedido</span>
                  <strong>#{pedido.id}</strong>
                </div>
                <div className="linha">
                  <span>Status</span>
                  <Status valor={pedido.status} />
                </div>
                <div className="linha">
                  <span>Subtotal</span>
                  <span>{dinheiro(pedido.subtotal)}</span>
                </div>
                <div className="linha">
                  <span>Frete</span>
                  <span data-testid="frete">{dinheiro(pedido.valorFrete)}</span>
                </div>
                <div className="linha total">
                  <span>Total do pedido</span>
                  <strong data-testid="total-pedido">{dinheiro(pedido.valorTotal)}</strong>
                </div>
              </div>
            )}
          </section>

          <section
            className={`painel ${!pedidoAberto ? 'inativo' : ''}`}
            aria-labelledby="titulo-pagamento"
          >
            <h2 id="titulo-pagamento">3. Pagamento</h2>

            <fieldset className="grupo" disabled={!pedidoAberto || ocupado}>
              <legend>Forma de pagamento</legend>
              <label className="opcao">
                <input
                  type="radio"
                  name="forma"
                  checked={forma === 'CARTAO'}
                  onChange={() => setForma('CARTAO')}
                />
                Cartão de crédito
              </label>
              <label className="opcao">
                <input
                  type="radio"
                  name="forma"
                  checked={forma === 'PIX'}
                  onChange={() => setForma('PIX')}
                />
                Pix (somente à vista)
              </label>
            </fieldset>

            {forma === 'CARTAO' && (
              <fieldset className="grupo" disabled={!pedidoAberto || ocupado}>
                <legend>Condição no cartão</legend>
                <label className="opcao">
                  <input
                    type="radio"
                    name="condicao"
                    checked={condicao === 'VISTA'}
                    onChange={() => setCondicao('VISTA')}
                  />
                  À vista
                </label>
                <label className="opcao">
                  <input
                    type="radio"
                    name="condicao"
                    checked={condicao === 'PARCELADO'}
                    onChange={() => setCondicao('PARCELADO')}
                  />
                  Parcelado
                </label>
                {condicao === 'PARCELADO' && (
                  <label className="parcelas">
                    Parcelas
                    <select
                      value={parcelas}
                      onChange={(e) => setParcelas(e.target.value)}
                    >
                      {[2, 3, 4, 5, 6].map((n) => (
                        <option key={n} value={n}>
                          {n}x
                        </option>
                      ))}
                    </select>
                  </label>
                )}
              </fieldset>
            )}

            {pedidoAberto && (
              <div className="resumo" data-testid="simulacao">
                {erroSimulacao ? (
                  <p className="erro-inline">{erroSimulacao}</p>
                ) : simulacao ? (
                  <>
                    <div className="linha total">
                      <span>Valor a pagar</span>
                      <strong data-testid="valor-final">
                        {dinheiro(simulacao.valorFinal)}
                      </strong>
                    </div>
                    {simulacao.parcelas > 1 && (
                      <div className="linha">
                        <span>{simulacao.parcelas} parcelas de</span>
                        <span data-testid="valor-parcela">
                          {dinheiro(simulacao.valorParcela)}
                        </span>
                      </div>
                    )}
                  </>
                ) : (
                  <p className="pequeno">Calculando...</p>
                )}
              </div>
            )}

            <div className="acoes">
              <button
                className="botao primario"
                disabled={!pedidoAberto || ocupado}
                onClick={pagar}
              >
                Pagar
              </button>
              <button
                className="botao secundario"
                disabled={!pedidoAberto || ocupado}
                onClick={desistir}
              >
                Desistir da compra
              </button>
            </div>
          </section>

          <section
            className={`painel ${!pedidoEncerrado ? 'inativo' : ''}`}
            aria-labelledby="titulo-resultado"
          >
            <h2 id="titulo-resultado">4. Resultado</h2>
            {!pedidoEncerrado && (
              <p className="pequeno">Aguardando a conclusão do pedido.</p>
            )}

            {pedido && pedido.status === 'CONFIRMADO' && (
              <div className="resumo" data-testid="resultado-confirmado">
                <div className="linha">
                  <span>Pedido</span>
                  <strong>#{pedido.id}</strong>
                </div>
                <div className="linha">
                  <span>Status</span>
                  <Status valor={pedido.status} />
                </div>
                {pagamento && (
                  <>
                    <div className="linha">
                      <span>Pagamento</span>
                      <span>
                        {pagamento.formaPagamento === 'PIX' ? 'Pix' : 'Cartão'}
                        {pagamento.parcelas > 1 ? ` em ${pagamento.parcelas}x` : ' à vista'}
                      </span>
                    </div>
                    <div className="linha total">
                      <span>Valor pago</span>
                      <strong data-testid="valor-pago">{dinheiro(pagamento.valor)}</strong>
                    </div>
                  </>
                )}
                <p className="pequeno">
                  Itens no carrinho: {itens.length}. O estoque foi atualizado.
                </p>
              </div>
            )}

            {pedido && pedido.status === 'CANCELADO' && (
              <div className="resumo" data-testid="resultado-cancelado">
                <div className="linha">
                  <span>Pedido</span>
                  <strong>#{pedido.id}</strong>
                </div>
                <div className="linha">
                  <span>Status</span>
                  <Status valor={pedido.status} />
                </div>
                <p className="pequeno">O estoque não foi alterado.</p>
              </div>
            )}

            {pedidoEncerrado && (
              <button className="botao primario" onClick={novoPedido}>
                Novo pedido
              </button>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}
