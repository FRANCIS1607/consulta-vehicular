package ec.edu.arq.ant;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200")
public class AntController {

    private final AntScraperClient scraper;
    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    public AntController(AntScraperClient scraper, StringRedisTemplate redis) {
        this.scraper = scraper;
        this.redis = redis;
    }

    @Operation(summary = "Consulta puntos de licencia en la ANT por cédula con respaldo en caché Redis")
    @GetMapping("/licencias/{cedula}")
    public Map<String, Object> licencia(@PathVariable String cedula) {
        if (!cedula.matches("\\d{10}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La cédula debe tener 10 dígitos");
        }
        String key = "ant:licencia:" + cedula;

        try {
            Map<String, Object> vivo = scraper.consultarPuntosPorCedula(cedula);
            try {
                redis.opsForValue().set(key, mapper.writeValueAsString(vivo), Duration.ofHours(24));
            } catch (Exception ignored) {
                // Si Redis no está disponible, se entrega el resultado vivo
            }
            return vivo;
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode() == HttpStatus.NOT_FOUND || ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw ex;
            }
            try {
                String cached = redis.opsForValue().get(key);
                if (cached != null) {
                    Map<String, Object> desdeCache = mapper.readValue(cached, new TypeReference<>() {});
                    Map<String, Object> resp = new LinkedHashMap<>(desdeCache);
                    resp.put("fuente", "REDIS_CACHE");
                    return resp;
                }
            } catch (Exception ignored) {
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "ANT no disponible y no existe respaldo en caché para esta cédula");
        }
    }
}
