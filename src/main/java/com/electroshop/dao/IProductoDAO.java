package com.electroshop.dao;

import com.electroshop.model.Producto;
import java.util.List;

public interface IProductoDAO {
    void save(Producto producto);
    Producto findById(int id);
    List<Producto> findByNombre(String nombre);
    void update(Producto producto);
    List<Producto> getAll();
}