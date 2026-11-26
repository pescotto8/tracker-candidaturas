package com.impulso.service;

import com.impulso.model.Empresa;
import com.impulso.model.Entrevista;
import com.impulso.model.Estado;
import com.impulso.model.Oferta;
import com.impulso.model.ResultadoEntrevista;
import com.impulso.model.TipoEntrevista;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorTest {

    private Oferta ofertaValida() {
        Oferta o = new Oferta();
        o.setIdEmpresa(1);
        o.setTitulo("Desarrollador Java");
        o.setEstado(Estado.PENDIENTE);
        o.setFechaPostulacion(LocalDate.now().minusDays(1));
        o.setSalario(30000.0);
        o.setEnlace("https://empresa.example/oferta");
        return o;
    }

    @Test
    void validOfferHasNoErrors() {
        assertTrue(Validador.validar(ofertaValida()).isEmpty());
    }

    @Test
    void offerWithoutOptionalFieldsIsValid() {
        Oferta o = ofertaValida();
        o.setSalario(null);
        o.setEnlace("");

        assertTrue(Validador.validar(o).isEmpty());
    }

    @Test
    void shortTitleIsRejected() {
        Oferta o = ofertaValida();
        o.setTitulo("ab");

        assertEquals(List.of("El título debe tener entre 3 y 120 caracteres."), Validador.validar(o));
    }

    @Test
    void offerNeedsCompanyAndDate() {
        Oferta o = ofertaValida();
        o.setIdEmpresa(0);
        o.setFechaPostulacion(null);

        List<String> errores = Validador.validar(o);

        assertEquals(2, errores.size());
        assertTrue(errores.contains("Selecciona una empresa registrada."));
        assertTrue(errores.contains("La fecha de postulación es obligatoria."));
    }

    @Test
    void futureDateIsRejected() {
        Oferta o = ofertaValida();
        o.setFechaPostulacion(LocalDate.now().plusDays(1));

        assertEquals(List.of("La fecha de postulación no puede ser posterior a hoy."), Validador.validar(o));
    }

    @Test
    void negativeSalaryIsRejectedButZeroIsAllowed() {
        Oferta o = ofertaValida();
        o.setSalario(-1.0);
        assertEquals(List.of("El salario debe ser igual o mayor que 0."), Validador.validar(o));

        o.setSalario(0.0);
        assertTrue(Validador.validar(o).isEmpty());
    }

    @Test
    void urlMustStartWithHttp() {
        assertTrue(Validador.urlValida(null));
        assertTrue(Validador.urlValida("  "));
        assertTrue(Validador.urlValida("HTTP://x.com"));
        assertTrue(Validador.urlValida("https://x.com/a?b=1"));
        assertFalse(Validador.urlValida("www.x.com"));
        assertFalse(Validador.urlValida("ftp://x.com"));
        assertFalse(Validador.urlValida("https://con espacios.com"));
    }

    @Test
    void companyRules() {
        Empresa valida = new Empresa(0, "Indra", "Tecnología", "https://indra.es", LocalDate.now());
        assertTrue(Validador.validar(valida).isEmpty());

        Empresa mala = new Empresa(0, "I", " ", "indra.es", LocalDate.now().plusDays(1));
        assertEquals(4, Validador.validar(mala).size());
    }

    @Test
    void interviewNeedsDateTypeAndResult() {
        Entrevista vacia = new Entrevista();
        vacia.setTipo(null);
        vacia.setResultado(null);
        assertEquals(3, Validador.validar(vacia).size());

        Entrevista completa = new Entrevista();
        completa.setFecha(LocalDate.now());
        completa.setTipo(TipoEntrevista.TECNICA);
        completa.setResultado(ResultadoEntrevista.PENDIENTE);
        assertTrue(Validador.validar(completa).isEmpty());
    }
}
