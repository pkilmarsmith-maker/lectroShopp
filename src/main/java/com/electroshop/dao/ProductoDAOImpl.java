package com.electroshop.dao;

import com.electroshop.Main;
import com.electroshop.model.Producto;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class ProductoDAOImpl implements IProductoDAO {
    @Override
    public void save(Producto producto) {
        Session session = Main.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.save(producto);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public Producto findById(int id) {
        Session session = Main.getSessionFactory().openSession();
        try {
            return session.get(Producto.class, id);
        } finally {
            session.close();
        }
    }

    @Override
    public List<Producto> findByNombre(String nombre) {
        Session session = Main.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Producto WHERE nombre LIKE :nombre", Producto.class)
                    .setParameter("nombre", "%" + nombre + "%")
                    .getResultList();
        } finally {
            session.close();
        }
    }

    @Override
    public void update(Producto producto) {
        Session session = Main.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        try {
            session.update(producto);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    @Override
    public List<Producto> getAll() {
        Session session = Main.getSessionFactory().openSession();
        try {
            return session.createQuery("FROM Producto", Producto.class).getResultList();
        } finally {
            session.close();
        }
    }
}