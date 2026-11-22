// Todas as chamadas passam pelo proxy do Vite (/api -> http://localhost:8080)
const BASE = '/api'

async function request(path, options = {}) {
  const resposta = await fetch(BASE + path, {
    headers: { 'Content-Type': 'application/json' },
    ...options,
  })

  const texto = await resposta.text()
  let dados = null
  try {
    dados = texto ? JSON.parse(texto) : null
  } catch {
    dados = null
  }

  if (!resposta.ok) {
    const mensagem =
      (dados && (dados.mensagem || dados.message)) ||
      `Erro inesperado (${resposta.status})`
    const erro = new Error(mensagem)
    erro.status = resposta.status
    throw erro
  }

  return dados
}

export const api = {
  listarLivros: () => request('/livros'),

  visualizarCarrinho: (idCliente) => request(`/carrinhos/${idCliente}`),

  adicionarItem: (idCliente, idLivro, quantidade) =>
    request('/carrinhos/itens', {
      method: 'POST',
      body: JSON.stringify({ idCliente, idLivro, quantidade }),
    }),

  alterarQuantidade: (idItem, quantidade) =>
    request(`/carrinhos/itens/${idItem}/quantidade`, {
      method: 'PATCH',
      body: JSON.stringify({ quantidade }),
    }),

  removerItem: (idItem) =>
    request(`/carrinhos/itens/${idItem}`, { method: 'DELETE' }),

  cadastrarEndereco: (endereco) =>
    request('/enderecos', { method: 'POST', body: JSON.stringify(endereco) }),

  efetuarPedido: (idCliente, idEndereco) =>
    request(`/pedidos?idCliente=${idCliente}&idEndereco=${idEndereco}`, {
      method: 'POST',
    }),

  buscarPedido: (id) => request(`/pedidos/${id}`),

  cancelarPedido: (id) => request(`/pedidos/${id}/cancelar`, { method: 'PATCH' }),

  simularPagamento: (valor, tipo, parcelas) =>
    request(
      `/pagamentos/simulacao?valor=${valor}&tipo=${tipo}&parcelas=${parcelas}`
    ),

  pagar: (pedidoId, tipo, parcelas) =>
    request('/pagamentos', {
      method: 'POST',
      body: JSON.stringify({ pedidoId, tipo, parcelas }),
    }),
}
