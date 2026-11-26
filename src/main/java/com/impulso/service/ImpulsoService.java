package com.impulso.service;

import com.impulso.db.DataAccessException;
import com.impulso.db.Database;
import com.impulso.model.Empresa;
import com.impulso.model.Entrevista;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.model.OfertaFilter;
import com.impulso.repository.EmpresaRepository;
import com.impulso.repository.EntrevistaRepository;
import com.impulso.repository.OfertaRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Capa de servicio: valida, abre conexiones y define los límites transaccionales.
 * Los controladores solo hablan con esta clase.
 */
public class ImpulsoService {

    private static final ImpulsoService INSTANCE = new ImpulsoService();

    private final EmpresaRepository empresas = new EmpresaRepository();
    private final OfertaRepository ofertas = new OfertaRepository();
    private final EntrevistaRepository entrevistas = new EntrevistaRepository();

    public static ImpulsoService get() {
        return INSTANCE;
    }

    private static void exigir(List<String> errores) {
        if (!errores.isEmpty()) {
            throw new DataAccessException(String.join("\n", errores));
        }
    }

    // ---------------- Empresas ----------------

    public List<Empresa> listarEmpresas(String texto) {
        return Database.query(c -> empresas.buscar(c, texto));
    }

    public Optional<Empresa> empresa(int id) {
        return Database.query(c -> empresas.buscarPorId(c, id));
    }

    public void guardarEmpresa(Empresa e) {
        exigir(Validador.validar(e));
        Database.transactionVoid(c -> {
            if (empresas.existeNombre(c, e.getNombre(), e.getId())) {
                throw new DataAccessException("Ya existe una empresa con el nombre «" + e.getNombre().trim() + "».");
            }
            if (e.getId() == 0) {
                empresas.insertar(c, e);
            } else {
                empresas.actualizar(c, e);
            }
        });
    }

    /** Transacción: borra entrevistas, candidaturas y empresa; todo o nada. */
    public void eliminarEmpresa(int id) {
        Database.transactionVoid(c -> {
            entrevistas.eliminarPorEmpresa(c, id);
            ofertas.eliminarPorEmpresa(c, id);
            empresas.eliminar(c, id);
        });
    }

    public List<Oferta> candidaturasDeEmpresa(int idEmpresa) {
        return Database.query(c -> ofertas.buscarPorEmpresa(c, idEmpresa));
    }

    // ---------------- Ofertas ----------------

    public int contarOfertas(OfertaFilter f) {
        return Database.query(c -> ofertas.contar(c, f));
    }

    public List<Oferta> buscarOfertas(OfertaFilter f, int limit, int offset) {
        return Database.query(c -> ofertas.buscar(c, f, limit, offset));
    }

    public int totalOfertas() {
        return Database.query(ofertas::total);
    }

    public Optional<Oferta> oferta(int id) {
        return Database.query(c -> ofertas.buscarPorId(c, id));
    }

    public void guardarOferta(Oferta o) {
        exigir(Validador.validar(o));
        Database.transactionVoid(c -> {
            if (empresas.buscarPorId(c, o.getIdEmpresa()).isEmpty()) {
                throw new DataAccessException("La empresa seleccionada ya no existe.");
            }
            if (o.getId() == 0) {
                ofertas.insertar(c, o);
            } else {
                ofertas.actualizar(c, o);
            }
        });
    }

    public void cambiarEstado(int idOferta, Estado estado) {
        Database.transactionVoid(c -> ofertas.cambiarEstado(c, idOferta, estado));
    }

    /** Transacción: borra sus entrevistas y la oferta. */
    public void eliminarOferta(int id) {
        Database.transactionVoid(c -> {
            entrevistas.eliminarPorOferta(c, id);
            ofertas.eliminar(c, id);
        });
    }

    // ---------------- Entrevistas ----------------

    public List<Entrevista> entrevistasDe(int idOferta) {
        return Database.query(c -> entrevistas.buscarPorOferta(c, idOferta));
    }

    public Optional<Entrevista> entrevista(int id) {
        return Database.query(c -> entrevistas.buscarPorId(c, id));
    }

    public void guardarEntrevista(Entrevista e) {
        exigir(Validador.validar(e));
        Database.transactionVoid(c -> {
            if (e.getId() == 0) {
                entrevistas.insertar(c, e);
            } else {
                entrevistas.actualizar(c, e);
            }
        });
    }

    /**
     * Guarda la entrevista y, si la candidatura seguía "Pendiente", la pasa a "Entrevista"
     * (como en el historial del prototipo): dos tablas, una transacción.
     */
    public void guardarEntrevistaYAvanzarEstado(Entrevista e) {
        exigir(Validador.validar(e));
        Database.transactionVoid(c -> {
            if (e.getId() == 0) {
                entrevistas.insertar(c, e);
            } else {
                entrevistas.actualizar(c, e);
            }
            Optional<Oferta> o = ofertas.buscarPorId(c, e.getIdOferta());
            if (o.isPresent() && o.get().getEstado() == Estado.PENDIENTE) {
                ofertas.cambiarEstado(c, e.getIdOferta(), Estado.ENTREVISTA);
            }
        });
    }

    public void eliminarEntrevista(int id) {
        Database.transactionVoid(c -> entrevistas.eliminar(c, id));
    }

    // ---------------- Estadísticas ----------------

    public Map<String, Integer> porEmpresa(OfertaFilter f) {
        return Database.query(c -> ofertas.contarPorEmpresa(c, f));
    }

    public Map<String, Integer> porMes(OfertaFilter f) {
        return Database.query(c -> ofertas.contarPorMes(c, f));
    }

    public Map<String, Integer> porEstado(OfertaFilter f) {
        return Database.query(c -> ofertas.contarPorEstado(c, f));
    }
}
