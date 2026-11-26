package com.impulso.repository;

import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.model.OfertaFilter;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** CRUD, filtros combinables y estadísticas de ofertas (JOIN con empresa). */
public class OfertaRepository {

    private static final String SELECT =
            "SELECT o.id, o.id_empresa, o.titulo, o.descripcion, o.estado, o.fecha_postulacion, " +
            "o.enlace, o.salario, o.notas, e.nombre AS empresa_nombre, e.sector AS empresa_sector, " +
            "e.web AS empresa_web " +
            "FROM oferta o INNER JOIN empresa e ON e.id = o.id_empresa ";

    private static final String COUNT =
            "SELECT COUNT(*) FROM oferta o INNER JOIN empresa e ON e.id = o.id_empresa ";

    /** Construye el WHERE dinámico; devuelve los parámetros en orden. */
    private String where(OfertaFilter f, List<Object> params) {
        List<String> conds = new ArrayList<>();
        if (f.texto != null && !f.texto.isBlank()) {
            String t = f.texto.trim();
            String like = "%" + t + "%";
            if (t.matches("\\d+")) {
                conds.add("(o.titulo LIKE ? OR e.nombre LIKE ? OR o.id = ?)");
                params.add(like);
                params.add(like);
                params.add(Integer.valueOf(t));
            } else {
                conds.add("(o.titulo LIKE ? OR e.nombre LIKE ?)");
                params.add(like);
                params.add(like);
            }
        }
        if (f.idEmpresa != null) {
            conds.add("o.id_empresa = ?");
            params.add(f.idEmpresa);
        }
        if (f.estado != null) {
            conds.add("o.estado = ?");
            params.add(f.estado.name());
        }
        if (f.desde != null) {
            conds.add("o.fecha_postulacion >= ?");
            params.add(Date.valueOf(f.desde));
        }
        if (f.hasta != null) {
            conds.add("o.fecha_postulacion <= ?");
            params.add(Date.valueOf(f.hasta));
        }
        if (f.salarioMin != null) {
            conds.add("o.salario >= ?");
            params.add(f.salarioMin);
        }
        if (f.salarioMax != null) {
            conds.add("o.salario <= ?");
            params.add(f.salarioMax);
        }
        return conds.isEmpty() ? "" : "WHERE " + String.join(" AND ", conds) + " ";
    }

    private static void bind(PreparedStatement ps, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            ps.setObject(i + 1, params.get(i));
        }
    }

    public int contar(Connection c, OfertaFilter f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = COUNT + where(f, params);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        }
    }

    /** Página de resultados, más recientes primero. */
    public List<Oferta> buscar(Connection c, OfertaFilter f, int limit, int offset) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = SELECT + where(f, params) + "ORDER BY o.fecha_postulacion DESC, o.id DESC LIMIT ? OFFSET ?";
        params.add(limit);
        params.add(offset);
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            List<Oferta> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        }
    }

    /** Indica si la empresa ya tiene una candidatura con ese título y esa fecha de postulación. */
    public boolean existe(Connection c, int idEmpresa, String titulo, LocalDate fechaPostulacion) throws SQLException {
        String sql = "SELECT COUNT(*) FROM oferta WHERE id_empresa = ? AND LOWER(titulo) = LOWER(?) AND fecha_postulacion = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idEmpresa);
            ps.setString(2, titulo.trim());
            ps.setDate(3, Date.valueOf(fechaPostulacion));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1) > 0;
            }
        }
    }

    public Optional<Oferta> buscarPorId(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        }
    }

    public List<Oferta> buscarPorEmpresa(Connection c, int idEmpresa) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(SELECT + "WHERE o.id_empresa = ? ORDER BY o.id DESC")) {
            ps.setInt(1, idEmpresa);
            List<Oferta> out = new ArrayList<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.add(map(rs));
                }
            }
            return out;
        }
    }

    public int insertar(Connection c, Oferta o) throws SQLException {
        String sql = "INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fecha_postulacion, enlace, salario, notas) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bindOferta(ps, o);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                o.setId(keys.getInt(1));
                return o.getId();
            }
        }
    }

    public boolean actualizar(Connection c, Oferta o) throws SQLException {
        String sql = "UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, " +
                     "fecha_postulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bindOferta(ps, o);
            ps.setInt(9, o.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean cambiarEstado(Connection c, int id, Estado estado) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("UPDATE oferta SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM oferta WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    public int eliminarPorEmpresa(Connection c, int idEmpresa) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("DELETE FROM oferta WHERE id_empresa = ?")) {
            ps.setInt(1, idEmpresa);
            return ps.executeUpdate();
        }
    }

    public int total(Connection c) throws SQLException {
        return contar(c, OfertaFilter.vacio());
    }

    // ---------- Estadísticas (GROUP BY + JOIN) ----------

    public Map<String, Integer> contarPorEmpresa(Connection c, OfertaFilter f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT e.nombre AS k, COUNT(*) AS n FROM oferta o INNER JOIN empresa e ON e.id = o.id_empresa " +
                     where(f, params) + "GROUP BY e.id, e.nombre ORDER BY n DESC, e.nombre";
        return agrupar(c, sql, params);
    }

    public Map<String, Integer> contarPorMes(Connection c, OfertaFilter f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT DATE_FORMAT(o.fecha_postulacion, '%Y-%m') AS k, COUNT(*) AS n " +
                     "FROM oferta o INNER JOIN empresa e ON e.id = o.id_empresa " +
                     where(f, params) + "GROUP BY k ORDER BY k";
        return agrupar(c, sql, params);
    }

    public Map<String, Integer> contarPorEstado(Connection c, OfertaFilter f) throws SQLException {
        List<Object> params = new ArrayList<>();
        String sql = "SELECT o.estado AS k, COUNT(*) AS n FROM oferta o INNER JOIN empresa e ON e.id = o.id_empresa " +
                     where(f, params) + "GROUP BY o.estado";
        return agrupar(c, sql, params);
    }

    private static Map<String, Integer> agrupar(Connection c, String sql, List<Object> params) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, params);
            Map<String, Integer> out = new LinkedHashMap<>();
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    out.put(rs.getString("k"), rs.getInt("n"));
                }
            }
            return out;
        }
    }

    // ---------- helpers ----------

    private static void bindOferta(PreparedStatement ps, Oferta o) throws SQLException {
        ps.setInt(1, o.getIdEmpresa());
        ps.setString(2, o.getTitulo().trim());
        ps.setString(3, EmpresaRepository.vacioANull(o.getDescripcion()));
        ps.setString(4, o.getEstado().name());
        ps.setDate(5, Date.valueOf(o.getFechaPostulacion()));
        ps.setString(6, EmpresaRepository.vacioANull(o.getEnlace()));
        if (o.getSalario() == null) {
            ps.setNull(7, Types.DECIMAL);
        } else {
            ps.setDouble(7, o.getSalario());
        }
        ps.setString(8, EmpresaRepository.vacioANull(o.getNotas()));
    }

    private static Oferta map(ResultSet rs) throws SQLException {
        Oferta o = new Oferta();
        o.setId(rs.getInt("id"));
        o.setIdEmpresa(rs.getInt("id_empresa"));
        o.setTitulo(rs.getString("titulo"));
        o.setDescripcion(rs.getString("descripcion"));
        o.setEstado(Estado.valueOf(rs.getString("estado")));
        o.setFechaPostulacion(rs.getDate("fecha_postulacion").toLocalDate());
        o.setEnlace(rs.getString("enlace"));
        double sal = rs.getDouble("salario");
        o.setSalario(rs.wasNull() ? null : sal);
        o.setNotas(rs.getString("notas"));
        o.setEmpresaNombre(rs.getString("empresa_nombre"));
        o.setEmpresaSector(rs.getString("empresa_sector"));
        o.setEmpresaWeb(rs.getString("empresa_web"));
        return o;
    }
}
