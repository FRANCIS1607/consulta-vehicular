package ec.edu.arq.sri;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "http://localhost:4200")
public class SriController {

    private final SriClient sri;

    public SriController(SriClient sri) {
        this.sri = sri;
    }

    @Operation(summary = "Valida que el RUC sea contribuyente persona natural y devuelve sus datos")
    @GetMapping("/contribuyentes/{ruc}")
    public Object contribuyente(@PathVariable String ruc) {
        if (!ruc.matches("\\d{13}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El RUC debe tener 13 dígitos");
        }
        if (!sri.existeRuc(ruc)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El RUC no es contribuyente del SRI");
        }
        if (ruc.charAt(2) >= '6') {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El RUC no corresponde a una persona natural");
        }
        Object datos = sri.datosRuc(ruc);
        Map<?, ?> primero = null;
        if (datos instanceof List<?> lista && !lista.isEmpty() && lista.get(0) instanceof Map<?, ?> m) {
            primero = m;
        } else if (datos instanceof Map<?, ?> m) {
            primero = m;
        }
        if (primero != null && primero.get("tipoContribuyente") != null
                && !String.valueOf(primero.get("tipoContribuyente")).toUpperCase().contains("NATURAL")) {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "El RUC no corresponde a una persona natural");
        }
        return datos;
    }

    @Operation(summary = "Valida la placa y devuelve datos del vehículo y valores de matrícula")
    @GetMapping("/vehiculos/{placa}")
    public Map<String, Object> vehiculo(@PathVariable String placa) {
        String p = placa.trim().toUpperCase();
        if (!p.matches("[A-Z0-9]{5,8}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La placa no tiene un formato válido");
        }
        if (!sri.existePlaca(p)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La placa no existe en el SRI");
        }
        Object vehiculo = sri.datosVehiculo(p);
        Object codigo = (vehiculo instanceof Map<?, ?> m) ? m.get("codigoVehiculo") : null;
        if (codigo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "No se encontró el código del vehículo en la respuesta del SRI");
        }
        Map<String, Object> resultado = new LinkedHashMap<>();
        resultado.put("vehiculo", vehiculo);
        resultado.put("rubros", sri.rubros(String.valueOf(codigo)));
        return resultado;
    }
}
