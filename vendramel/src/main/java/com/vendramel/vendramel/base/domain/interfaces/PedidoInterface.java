package com.vendramel.vendramel.base.domain.interfaces;

import com.vendramel.vendramel.base.domain.entidades.Pedido;

public interface PedidoInterface {

    Pedido efetuarPedido(int idCliente, int idEndereco);

    Pedido confirmarPedido(int idPedido);

    Pedido enviarPedido(int idPedido);

    Pedido entregarPedido(int idPedido);

    Pedido cancelarPedido(int idPedido);

    Pedido buscarPorId(int id);
}
