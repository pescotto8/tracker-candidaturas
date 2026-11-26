package com.impulso.service;

import com.impulso.model.Estado;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvImporterTest {

    @Test
    void splitsBySemicolonKeepingEmptyFields() {
        assertArrayEquals(new String[]{"1", "a", "", "d"}, CsvImporter.partir("1;a;;d"));
    }

    @Test
    void quotedFieldsKeepSemicolonsAndEscapedQuotes() {
        assertArrayEquals(new String[]{"1", "uno; dos", "di \"hola\""},
                CsvImporter.partir("1;\"uno; dos\";\"di \"\"hola\"\"\""));
    }

    @Test
    void parsesAV1Row() {
        String[] campos = CsvImporter.partir(
                "1;Desarrollador Java Junior;Indra;pendiente;20/10/2026;https://www.indracompany.com/empleo;Contacto por LinkedIn");

        CsvImporter.Fila fila = CsvImporter.parsear(2, campos);

        assertEquals(2, fila.linea());
        assertEquals("Indra", fila.empresa());
        assertEquals("Desarrollador Java Junior", fila.oferta().getTitulo());
        assertEquals(Estado.PENDIENTE, fila.oferta().getEstado());
        assertEquals(LocalDate.of(2026, 10, 20), fila.oferta().getFechaPostulacion());
        assertEquals("https://www.indracompany.com/empleo", fila.oferta().getEnlace());
        assertEquals("Contacto por LinkedIn", fila.oferta().getNotas());
    }

    @Test
    void emptyLinkAndNotesBecomeNull() {
        CsvImporter.Fila fila = CsvImporter.parsear(2, CsvImporter.partir("5;Titulo;Acme;ACEPTADO;01/02/2026;;"));

        assertNull(fila.oferta().getEnlace());
        assertNull(fila.oferta().getNotas());
        assertEquals(Estado.ACEPTADO, fila.oferta().getEstado());
    }

    @Test
    void wrongNumberOfFieldsFails() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> CsvImporter.parsear(3, CsvImporter.partir("1;Titulo;Acme")));

        assertEquals("se esperaban 7 campos y hay 3.", error.getMessage());
    }

    @Test
    void invalidStateListsValidOnes() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> CsvImporter.parsear(3, CsvImporter.partir("1;Titulo;Acme;raro;01/02/2026;;")));

        assertTrue(error.getMessage().startsWith("estado no válido: 'raro'"));
    }

    @Test
    void impossibleDateFails() {
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> CsvImporter.parsear(3, CsvImporter.partir("1;Titulo;Acme;pendiente;31/02/2026;;")));

        assertEquals("fecha no válida: '31/02/2026'. Formato esperado: dd/MM/yyyy.", error.getMessage());
    }

    @Test
    void isoDateIsNotAcceptedInV1Format() {
        assertThrows(IllegalArgumentException.class,
                () -> CsvImporter.parsear(3, CsvImporter.partir("1;Titulo;Acme;pendiente;2026-02-01;;")));
    }
}
