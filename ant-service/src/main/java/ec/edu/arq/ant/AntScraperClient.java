package ec.edu.arq.ant;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AntScraperClient {

    private static final String ANT_URL = "https://ant.gob.ec";

    public Map<String, Object> consultarPuntosPorCedula(String cedula) {
        try {
            String url = String.format(ANT_URL, cedula);
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            String texto = doc.text();
            if (texto == null || texto.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "La ANT devolvió una respuesta vacía");
            }

            String nombre = extraerNombre(doc);
            Integer puntos = extraerPuntos(doc, texto);

            if (puntos == null && (nombre == null || nombre.isBlank())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró información de licencia/puntos para la cédula ingresada");
            }

            Map<String, Object> resultado = new LinkedHashMap<>();
            resultado.put("cedula", cedula);
            resultado.put("nombre", nombre != null ? nombre : "No disponible");
            resultado.put("puntos", puntos != null ? puntos : 0);
            resultado.put("fuente", "ANT_LIVE");
            return resultado;

        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (IOException ex) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "No se pudo conectar con el portal de la ANT");
        }
    }

    private String extraerNombre(Document doc) {
        Element el = doc.selectFirst("td.titulo1, td.titulo, #nombre");
        if (el != null && !el.text().isBlank()) {
            return el.text().trim();
        }
        for (Element td : doc.select("td")) {
            String t = td.text().trim();
            if (t.matches("[A-ZÁÉÍÓÚÑ ]{10,}") && !t.contains("PUNTOS") && !t.contains("CEDULA")) {
                return t;
            }
        }
        return null;
    }

    private Integer extraerPuntos(Document doc, String texto) {
        for (Element el : doc.select("td, span, div")) {
            String t = el.text().trim();
            if (t.matches("(?i).*puntos.*")) {
                Matcher m = Pattern.compile("(\\d{1,2})").matcher(t);
                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                }
            }
        }
        Matcher m = Pattern.compile("(?i)puntos\\s*:?\\s*(\\d{1,2})").matcher(texto);
        if (m.find()) {
            return Integer.parseInt(m.group(1));
        }
        return null;
    }
}
