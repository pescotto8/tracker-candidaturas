package com.impulso.util;

import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

/** Formatos del prototipo: fechas dd/MM/yyyy, importes "32.000 €". */
public final class Fmt {
    public static final Locale ES = Locale.forLanguageTag("es-ES");
    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter FECHA_LARGA = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", ES);

    /** Convierte DatePicker.value &lt;-&gt; "dd/MM/yyyy". Un texto no válido da null. */
    public static final StringConverter<LocalDate> DATE_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(LocalDate d) {
            return d == null ? "" : FECHA.format(d);
        }

        @Override
        public LocalDate fromString(String s) {
            if (s == null || s.isBlank()) {
                return null;
            }
            try {
                return LocalDate.parse(s.trim(), FECHA);
            } catch (DateTimeParseException e) {
                return null;
            }
        }
    };

    private Fmt() {
    }

    public static String fecha(LocalDate d) {
        return d == null ? "" : FECHA.format(d);
    }

    public static String money(Double v) {
        if (v == null) {
            return "—";
        }
        // En es-ES los números de 4 cifras no llevan separador; lo forzamos como en el prototipo (32.000).
        String s = String.format(Locale.GERMANY, "%,d", Math.round(v));
        return s + " €";
    }

    public static String percent(double fraction) {
        return String.format(ES, "%.1f %%", fraction * 100);
    }

    /** Admite "32000", "32.000", "32 000" o "32000,50". Vacío = null. */
    public static Double parseMoney(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        String t = s.trim().replace("€", "").replace(" ", "");
        if (t.contains(",")) {
            t = t.replace(".", "").replace(',', '.');
        } else if (t.matches("\\d{1,3}(\\.\\d{3})+")) {
            t = t.replace(".", "");
        }
        return Double.valueOf(t);
    }

    /** "HH:mm" -&gt; LocalTime; vacío = null; formato incorrecto = DateTimeParseException. */
    public static LocalTime parseHora(String s) {
        return s == null || s.isBlank() ? null : LocalTime.parse(s.trim(), HORA);
    }

    public static String hora(LocalTime t) {
        return t == null ? "" : HORA.format(t);
    }
}
