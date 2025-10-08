package com.electroshop;

import com.electroshop.dao.IProductoDAO;
import com.electroshop.dao.ProductoDAOImpl;
import com.electroshop.model.*;
import com.electroshop.service.DescuentoService;  // Nueva para OCP (opcional)
import com.electroshop.service.EcommerceService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;  // Nueva: Para input interactivo en Fase III

public class Main {
    private static SessionFactory sessionFactory;

    // Método estático para SessionFactory (reutilizable en DAOs, con todas entidades)
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            sessionFactory = new Configuration()
                    .configure("hibernate.cfg.xml")
                    .addAnnotatedClass(Categoria.class)
                    .addAnnotatedClass(Producto.class)
                    .addAnnotatedClass(Usuario.class)
                    .addAnnotatedClass(Pedidos.class)
                    .addAnnotatedClass(CarritoItem.class)  // Nueva para Fase III
                    .buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void main(String[] args) {
        try {
            // Inicialización
            System.out.println("¡ElectroShopp: E-commerce Java con Hibernate, SOLID y API Externa!");

            // Fase II: CRUD Básico (Crea datos base para Fase III)
            ejecutarFaseII();

            // Fase III: Lógica Negocio, SOLID y API (Usa datos de Fase II)
            ejecutarFaseIII();

            System.out.println("\n¡Ejecución completa exitosa! Verifica BD en phpMyAdmin.");
        } catch (Exception e) {
            System.err.println("Error general en Main: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cierre limpio
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        }
    }

    // Fase II: CRUD para Producto, Usuario, Pedidos (Mejorado: Manejo duplicates con query)
    private static void ejecutarFaseII() {
        System.out.println("\n--- Fase II: Prueba CRUD para Producto, Usuario y Pedidos ---");

        Session session = getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();

            // 1. CRUD Producto (sin cambios – siempre crea nuevo para demo)
            System.out.println("\n--- CRUD Producto ---");
            Categoria cat = new Categoria("Electrónicos", "Productos electrónicos");
            session.save(cat);

            Producto prod = new Producto("Televisor LED", "TV 55 pulgadas", new BigDecimal("599.99"), 10, cat);
            session.save(prod);  // Crear
            System.out.println("Producto creado: " + prod.getNombre() + " (ID: " + prod.getId() + ")");

            Producto foundProd = session.get(Producto.class, prod.getId());  // Leer
            System.out.println("Producto leído: " + foundProd.getNombre() + ", Precio: " + foundProd.getPrecio());

            foundProd.setPrecio(new BigDecimal("549.99"));  // Actualizar
            session.update(foundProd);
            System.out.println("Producto actualizado: Nuevo precio " + foundProd.getPrecio());

            // 2. CRUD Usuario (Mejora: Verifica existencia para evitar duplicate email)
            System.out.println("\n--- CRUD Usuario ---");
            String email = "juan3@email.com";  // Fijo para demo; haz dinámico si quieres
            Usuario user = session.createQuery("FROM Usuario WHERE email = :email", Usuario.class)
                    .setParameter("email", email)
                    .uniqueResult();
            if (user == null) {
                user = new Usuario("Juan Pérez", email, "pass123");
                user.setTelefono("555-0001");
                session.save(user);  // Crear solo si no existe
                System.out.println("Usuario creado: " + user.getNombre() + " (ID: " + user.getId() + ")");
            } else {
                System.out.println("Usuario existente cargado: " + user.getNombre() + " (ID: " + user.getId() + ")");
            }

            Usuario foundUser  = session.get(Usuario.class, user.getId());  // Leer
            System.out.println("Usuario leído: " + foundUser .getEmail() + ", Rol: " + foundUser .getRol());

            foundUser .setRol(Usuario.Rol.ADMIN);  // Actualizar
            session.update(foundUser );
            System.out.println("Usuario actualizado: Nuevo rol " + foundUser .getRol());

            // 3. CRUD Pedidos (sin cambios)
            System.out.println("\n--- CRUD Pedidos ---");
            Pedidos pedido = new Pedidos(foundUser , new BigDecimal("300.00"));
            pedido.setEstado(Pedidos.Estado.ENVIADO);
            session.save(pedido);  // Crear
            System.out.println("Pedido creado: Total " + pedido.getTotal() + " (ID: " + pedido.getId() + ")");

            Pedidos foundPedido = session.get(Pedidos.class, pedido.getId());  // Leer
            System.out.println("Pedido leído: Estado " + foundPedido.getEstado() + ", Usuario ID: " + foundPedido.getUsuario().getId());

            foundPedido.setTotal(new BigDecimal("350.00"));  // Actualizar
            session.update(foundPedido);
            System.out.println("Pedido actualizado: Nuevo total " + foundPedido.getTotal());

            // Prueba relación: Contar pedidos del usuario (HQL)
            Query<Pedidos> query = session.createQuery("FROM Pedidos WHERE usuario.id = :userId", Pedidos.class);
            query.setParameter("userId", user.getId());
            System.out.println("Número de pedidos del usuario: " + query.getResultList().size());

            tx.commit();
            System.out.println("¡CRUD Fase II exitoso!");
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            System.err.println("Error en Fase II: " + e.getMessage());
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // Fase III: SOLID (SRP/DIP), API Externa y Flujo E-commerce (Mejorado: Interactivo + OCP)
    private static void ejecutarFaseIII() {
        System.out.println("\n--- Fase III: Simulación E-commerce con SOLID y API Externa ---");

        // DIP: Crea DAO impl y inyecta en Service (inversión de dependencias)
        IProductoDAO productoDAO = new ProductoDAOImpl();
        EcommerceService service = new EcommerceService(productoDAO);

        // OCP: Crea subclase para descuentos (extiende sin modificar Service original)
        DescuentoService descuentoService = new DescuentoService(productoDAO);  // Opcional: Comenta si no tienes la clase

        // Carga dinámica: Usuario de Fase II (por email, con fallback)
        Session sessionTemp = getSessionFactory().openSession();
        Usuario user = sessionTemp.createQuery("FROM Usuario WHERE email = :email", Usuario.class)
                .setParameter("email", "juan3@email.com")
                .uniqueResult();
        sessionTemp.close();

        if (user == null) {
            System.err.println("Usuario no encontrado – ejecuta Fase II primero o verifica BD.");
            return;
        }
        System.out.println("Usuario logueado: " + user.getNombre() + " (ID: " + user.getId() + ")");

        // Mejora: Input interactivo con Scanner
        Scanner sc = new Scanner(System.in);
        System.out.print("Ingresa término de búsqueda (ej: Televisor): ");
        String queryTerm = sc.nextLine().trim();
        List<Producto> busqueda = service.buscarProductos(queryTerm);  // Búsqueda HQL en DAO
        System.out.println("Búsqueda '" + queryTerm + "': " + busqueda.size() + " productos encontrados.");

        if (!busqueda.isEmpty()) {
            Producto prod = busqueda.get(0);  // Toma el primero; en prod, lista menú
            System.out.println("Producto seleccionado: " + prod.getNombre() + " (Precio: " + prod.getPrecio() + ", Stock: " + prod.getStock() + ")");

            System.out.print("Ingresa cantidad para carrito (1-" + prod.getStock() + "): ");
            int cantidad = sc.nextInt();
            if (cantidad <= 0 || cantidad > prod.getStock()) {
                System.out.println("Cantidad inválida – usando 1 por defecto.");
                cantidad = 1;
            }

            // 2. Añadir a carrito (lógica en Service)
            service.añadirAlCarrito(user, prod, cantidad);

            // Simula carrito en memoria (mejora futura: Persistir con ICarritoDAO)
            List<CarritoItem> carrito = new ArrayList<>();
            carrito.add(new CarritoItem(cantidad, prod, user));

            // 3. Procesar pedido normal (actualiza stock via DAO, integra API divisas en Service)
            Pedidos pedido = service.procesarPedido(user, carrito);
            System.out.println("Pedido procesado: ID " + pedido.getId() + ", Estado: " + pedido.getEstado() + ", Total USD: " + pedido.getTotal());

            // Mejora OCP: Procesa con descuento (extensión sin modificar Service)
            try {
                Pedidos pedidoDescuento = descuentoService.procesarPedidoConDescuento(user, carrito, 0.10);  // 10% off
                System.out.println("Pedido con descuento (OCP): Total " + pedidoDescuento.getTotal() + " (ahorro 10%)");
            } catch (Exception e) {
                System.out.println("OCP Demo: Extensión descuento no disponible – " + e.getMessage());
            }

            // 4. Gestionar inventario (query en DAO)
            List<Producto> bajoStock = service.getProductosBajoStock(5);
            System.out.println("Productos bajo stock: " + bajoStock.size() + " items.");
            if (!bajoStock.isEmpty()) {
                System.out.println("Alertas: Revisa stock para " + bajoStock.get(0).getNombre());  // Ejemplo simple
            }

            System.out.println("¡Flujo E-commerce completado con API!");
        } else {
            System.out.println("No hay productos para '" + queryTerm + "' – agrega más en Fase II.");
        }
        sc.close();  // Limpia recursos
    }
}