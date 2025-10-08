package com.electroshop.service;

import com.electroshop.api.ExchangeRatesClient;
import com.electroshop.dao.IProductoDAO;
import com.electroshop.model.CarritoItem;
import com.electroshop.model.Pedidos;
import com.electroshop.model.Producto;
import com.electroshop.model.Usuario;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EcommerceService {
    private final IProductoDAO productoDAO;  // DIP: Inyecta interface (no impl concreta)
    private final ExchangeRatesClient apiClient;  // Para API externa

    // Constructor: Inyección de dependencias (DIP)
    public EcommerceService(IProductoDAO productoDAO) {
        this.productoDAO = productoDAO;
        this.apiClient = new ExchangeRatesClient();  // Crea API client (SRP: Service maneja negocio + API simple)
    }

    // 1. Búsqueda de productos (delega a DAO para SRP: Service no toca BD)
    public List<Producto> buscarProductos(String query) {
        return productoDAO.findByNombre(query);
    }

    // 2. Añadir al carrito (lógica negocio: simula en memoria, opcional persistir)
    public void añadirAlCarrito(Usuario usuario, Producto producto, int cantidad) {
        if (cantidad <= 0 || cantidad > producto.getStock()) {
            throw new IllegalArgumentException("Cantidad inválida o stock insuficiente");
        }
        // Simula carrito (en prod, usa CarritoDAO.save(new CarritoItem(cantidad, producto, usuario)))
        System.out.println("Añadido al carrito: " + producto.getNombre() + " x" + cantidad +
                " (Total parcial: " + producto.getPrecio().multiply(BigDecimal.valueOf(cantidad)) + ")");
    }

    // 3. Procesar pedido (lógica central: actualiza stock, crea Pedido, integra API divisas)
    public Pedidos procesarPedido(Usuario usuario, List<CarritoItem> carrito) {
        if (carrito.isEmpty()) {
            throw new IllegalArgumentException("Carrito vacío");
        }

        BigDecimal total = BigDecimal.ZERO;
        for (CarritoItem item : carrito) {
            Producto p = item.getProducto();
            int cant = item.getCantidad();

            // Validar y reducir stock (gestiona inventario)
            if (p.getStock() < cant) {
                throw new RuntimeException("Stock insuficiente para " + p.getNombre());
            }
            p.setStock(p.getStock() - cant);
            productoDAO.update(p);  // Actualiza en BD via DAO

            // Calcular total
            total = total.add(p.getPrecio().multiply(BigDecimal.valueOf(cant)));
        }

        // Crea Pedido
        Pedidos pedido = new Pedidos(total, Pedidos.Estado.PENDIENTE, usuario);
        // En prod: pedidoDAO.save(pedido); – aquí simula ID (ajusta si tienes IPedidoDAO)
        pedido.setId(999L);  // Simulado; usa session.save en DAO si extiendes
        System.out.println("Pedido procesado: Total USD " + total);

        // Integra API Externa: Convierte a EUR (relevante para E-commerce global)
        double totalEUR = apiClient.convertirADivisa(total.doubleValue(), "EUR");
        System.out.println("Total en EUR (via API Exchange Rates): €" + String.format("%.2f", totalEUR));

        return pedido;
    }

    // 4. Gestionar inventario: Productos bajo stock (delega query a DAO)
    public List<Producto> getProductosBajoStock(int minStock) {
        List<Producto> all = productoDAO.getAll();
        List<Producto> bajoStock = new ArrayList<>();
        for (Producto p : all) {
            if (p.getStock() < minStock) {
                bajoStock.add(p);
            }
        }
        return bajoStock;
    }
}