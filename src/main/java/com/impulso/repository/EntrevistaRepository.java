package com.impulso.repository;

import com.impulso.model.Entrevista;
import com.impulso.model.ResultadoEntrevista;
import com.impulso.model.TipoEntrevista;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntrevistaRepository {

    private static final String COLS = "id, id_oferta, fecha, hora, tipo, notas, resultado ";

    public List<Entrevista> buscarPorOferta(Connection c, int idOferta) throws SQLException {
        String sql = "SELECT " + COLS + "FROM entrevista WHERE id_oferta = ? ORDER BY fecha, hora";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            List<Entrevista> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        }
    }

    public Optional<Entrevista> buscarPorId(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT " + COLS + "FROM entrevista WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public int insertar(Connection c, Entrevista e) throws SQLException {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, hora, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, e);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                e.setId(keys.getInt(1));
                return e.getId();
            }
        }
    }

    public boolean actualizar(Connection c, Entrevista e) throws SQLException {
        String sql = "UPDATE entrevista SET id_oferta = ?, fecha = ?, hora = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, e);
            ps.setInt(7, e.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM entrevista WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int eliminarPorOferta(Connection c, int idOferta) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM entrevista WHERE id_oferta = ?")) {
            ps.setInt(1, idOferta);
            return ps.executeUpdate();
        }
    }

    public int eliminarPorEmpresa(Connection c, int idEmpresa) throws SQLException {
        String sql = "DELETE FROM entrevista WHERE id_oferta IN (SELECT id FROM (SELECT id FROM oferta WHERE id_empresa = ?) t)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            return ps.executeUpdate();
        }
    }

    private static void bind(PreparedStatement ps, Entrevista e) throws SQLException {
        ps.setInt(1, e.getIdOferta());
        ps.setDate(2, Date.valueOf(e.getFecha()));
        if (e.getHora() == null) {
            ps.setNull(3, Types.TIME);
        } else {
            ps.setTime(3, Time.valueOf(e.getHora()));
        }
        ps.setString(4, e.getTipo().name());
        ps.setString(5, EmpresaRepository.vacioANull(e.getNotas()));
        ps.setString(6, e.getResultado().name());
    }

    private static Entrevista map(ResultSet rs) throws SQLException {
        Entrevista e = new Entrevista();
        e.setId(rs.getInt("id"));
        e.setIdOferta(rs.getInt("id_oferta"));
        e.setFecha(rs.getDate("fecha").toLocalDate());
        Time t = rs.getTime("hora");
        e.setHora(t == null ? null : t.toLocalTime());
        e.setTipo(TipoEntrevista.valueOf(rs.getString("tipo")));
        e.setNotas(rs.getString("notas"));
        e.setResultado(ResultadoEntrevista.valueOf(rs.getString("resultado")));
        return e;
    }
}
