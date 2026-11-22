package com.vendramel.vendramel.base.domain.Services;

import com.vendramel.vendramel.base.domain.entidades.Pedido;
import com.vendramel.vendramel.base.domain.interfaces.PedidoInterface;

public class PedidoService {

    private final PedidoInterface inter;

    public PedidoService(PedidoInterface inter) {
        this.inter = inter;
    }

    public Pedido efetuarPedido(int idCliente, int idEndereco) {
        return inter.efetuarPedido(idCliente, idEndereco);
    }

    public Pedido confirmarPedido(int idPedido) {
        return inter.confirmarPedido(idPedido);
    }

    public Pedido enviarPedido(int idPedido) {
        return inter.enviarPedido(idPedido);
    }

    public Pedido entregarPedido(int idPedido) {
        return inter.entregarPedido(idPedido);
    }

    public Pedido cancelarPedido(int idPedido) {
        return inter.cancelarPedido(idPedido);
    }

    public Pedido buscarPorId(int id) {
        return inter.buscarPorId(id);
    }
}
