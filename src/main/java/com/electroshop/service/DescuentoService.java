package com.electroshop.service;

import com.electroshop.dao.IProductoDAO;
import com.electroshop.model.CarritoItem;
import com.electroshop.model.Pedidos;
import com.electroshop.model.Usuario;

import java.math.BigDecimal;

public class DescuentoService extends EcommerceService {  // OCP: Extiende para agregar funcionalidad
    public DescuentoService(IProductoDAO productoDAO) {
        super(productoDAO);  // Llama constructor padre (DIP intacto)
    }

    // Método nuevo: Procesa con descuento (abre para extensión, cierra modificación)
    public Pedidos procesarPedidoConDescuento(Usuario usuario, java.util.List<CarritoItem> carrito, double descuentoPct) {
        if (descuentoPct < 0 || descuentoPct > 1) {
            throw new IllegalArgumentException("Descuento inválido (0-1)");
        }
        Pedidos pedido = procesarPedido(usuario, carrito);  // Reusa lógica padre (SRP/OCP)
        BigDecimal descuento = pedido.getTotal().multiply(BigDecimal.valueOf(descuentoPct));
        pedido.setTotal(pedido.getTotal().subtract(descuento));
        System.out.println("Descuento aplicado: " + String.format("%.2f", descuento) + " (%" + (descuentoPct * 100) + ")");
        return pedido;  // En prod: Actualiza en BD
    }
}