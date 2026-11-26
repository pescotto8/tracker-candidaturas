package com.impulso.repository;

import com.impulso.model.Empresa;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** CRUD de empresas con JDBC. Todas las operaciones reciben la conexión (para poder usar transacciones). */
public class EmpresaRepository {

    // LEFT JOIN para contar las candidaturas asociadas a cada empresa.
    private static final String SELECT_CON_CONTEO =
            "SELECT e.id, e.nombre, e.sector, e.web, e.fecha_registro, COUNT(o.id) AS num " +
            "FROM empresa e LEFT JOIN oferta o ON o.id_empresa = e.id ";

    private static final String GROUP_ORDER =
            "GROUP BY e.id, e.nombre, e.sector, e.web, e.fecha_registro ORDER BY e.fecha_registro, e.id";

    public List<Empresa> buscar(Connection c, String texto) throws SQLException {
        boolean hayTexto = texto != null && !texto.isBlank();
        String sql = SELECT_CON_CONTEO +
                (hayTexto ? "WHERE e.nombre LIKE ? OR e.sector LIKE ? OR e.web LIKE ? " : "") +
                GROUP_ORDER;
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            if (hayTexto) {
                String like = "%" + texto.trim() + "%";
                ps.setString(1, like);
                ps.setString(2, like);
                ps.setString(3, like);
            }
            List<Empresa> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        }
    }

    public Optional<Empresa> buscarPorId(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT_CON_CONTEO + "WHERE e.id = ? " + GROUP_ORDER)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public Optional<Empresa> buscarPorNombre(Connection c, String nombre) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT_CON_CONTEO + "WHERE LOWER(e.nombre) = LOWER(?) " + GROUP_ORDER)) {
            ps.setString(1, nombre.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insertar(Connection c, Empresa e) throws SQLException {
        String sql = "INSERT INTO empresa (nombre, sector, web, fecha_registro) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre().trim());
            ps.setString(2, e.getSector().trim());
            ps.setString(3, vacioANull(e.getWeb()));
            ps.setDate(4, Date.valueOf(e.getFechaRegistro()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                int id = keys.getInt(1);
                e.setId(id);
                return id;
            }
        }
    }

    public boolean actualizar(Connection c, Empresa e) throws SQLException {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ?, fecha_registro = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getNombre().trim());
            ps.setString(2, e.getSector().trim());
            ps.setString(3, vacioANull(e.getWeb()));
            ps.setDate(4, Date.valueOf(e.getFechaRegistro()));
            ps.setInt(5, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM empresa WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean existeNombre(Connection c, String nombre, int excluirId) throws SQLException {
        String sql = "SELECT 1 FROM empresa WHERE LOWER(nombre) = LOWER(?) AND id <> ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nombre.trim());
            ps.setInt(2, excluirId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static Empresa map(ResultSet rs) throws SQLException {
        Empresa e = new Empresa(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("sector"),
                rs.getString("web"),
                rs.getDate("fecha_registro").toLocalDate());
        e.setNumCandidaturas(rs.getInt("num"));
        return e;
    }

    static String vacioANull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }
}
