package ec.edu.arq.sri;

import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class SriClient {

    private static final String CATASTRO =
        "https://srienlinea.sri.gob.ec/sri-catastro-sujeto-servicio-internet/rest/ConsolidadoContribuyente";
    private static final String VEHICULO =
        "https://srienlinea.sri.gob.ec/sri-matriculacion-vehicular-recaudacion-servicio-internet/rest";

    private final RestClient http;

    public SriClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(5));
        factory.setReadTimeout(Duration.ofSeconds(10));
        this.http = RestClient.builder()
                .requestFactory(factory)
                .defaultHeader("User-Agent", "Mozilla/5.0")
                .defaultHeader("Accept", "application/json")
                .build();
    }

    public boolean existeRuc(String ruc) {
        String r = http.get()
                .uri(CATASTRO + "/existePorNumeroRuc?numeroRuc={ruc}", ruc)
                .retrieve().body(String.class);
        return r != null && r.trim().equalsIgnoreCase("true");
    }

    public Object datosRuc(String ruc) {
        return http.get()
                .uri(CATASTRO + "/obtenerPorNumerosRuc?&ruc={ruc}", ruc)
                .retrieve().body(Object.class);
    }

    public boolean existePlaca(String placa) {
        String r = http.get()
                .uri(VEHICULO + "/BaseVehiculo/existePorNumeroPlaca?numeroPlaca={placa}", placa)
                .retrieve().body(String.class);
        return r != null && r.trim().equalsIgnoreCase("true");
    }

    public Object datosVehiculo(String placa) {
        return http.get()
                .uri(VEHICULO + "/BaseVehiculo/obtenerPorNumeroPlacaOPorNumeroCampvOPorNumeroCpn?numeroPlacaCampvCpn={placa}", placa)
                .retrieve().body(Object.class);
    }

    public Object rubros(String codigoVehiculo) {
        return http.get()
                .uri(VEHICULO + "/ConsultaRubros/obtenerPorCodigoVehiculo?codigoVehiculo={codigo}", codigoVehiculo)
                .retrieve().body(Object.class);
    }
}
