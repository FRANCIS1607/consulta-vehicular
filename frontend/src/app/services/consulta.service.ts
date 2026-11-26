import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class ConsultaService {
  private http = inject(HttpClient);
  private sriUrl = 'http://localhost:8081/api/v1';
  private antUrl = 'http://localhost:8082/api/v1';

  consultarContribuyente(ruc: string): Observable<any> {
    return this.http.get(`${this.sriUrl}/contribuyentes/${ruc}`);
  }

  consultarVehiculo(placa: string): Observable<any> {
    return this.http.get(`${this.sriUrl}/vehiculos/${placa}`);
  }

  consultarLicencia(cedula: string): Observable<any> {
    return this.http.get(`${this.antUrl}/licencias/${cedula}`);
  }
}
