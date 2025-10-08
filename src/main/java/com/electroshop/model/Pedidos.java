package com.electroshop.model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "Pedidos")
public class Pedidos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "total", nullable = false, precision = 10, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private Estado estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idUsuario", nullable = false)
    private Usuario usuario;

    // Constructor vacío REQUERIDO por Hibernate
    public Pedidos() {
    }

    // Constructor con params (usado en Main/Service)
    public Pedidos(BigDecimal total, Estado estado, Usuario usuario) {
        this.total = total;
        this.estado = estado != null ? estado : Estado.PENDIENTE;
        this.usuario = usuario;
    }

    // Constructor alternativo (de Main: Pedidos(usuario, total))
    public Pedidos(Usuario usuario, BigDecimal total) {
        this(total, Estado.PENDIENTE, usuario);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }

    public Estado getEstado() { return estado; }
    public void setEstado(Estado estado) { this.estado = estado; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    // Enum Estado (si no está en clase separada)
    public enum Estado {
        PENDIENTE, ENVIADO, ENTREGADO, CANCELADO
    }

    @Override
    public String toString() {
        return "Pedidos{id=" + id + ", total=" + total + ", estado=" + estado + ", usuarioId=" + (usuario != null ? usuario.getId() : null) + "}";
    }
}