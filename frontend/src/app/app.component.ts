import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ConsultaService } from './services/consulta.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent {
  private api = inject(ConsultaService);

  ruc = '';
  placa = '';
  cargando = false;
  error = '';

  contribuyente: any = null;
  vehiculoData: any = null;
  licenciaData: any = null;

  consultar(): void {
    this.error = '';
    this.contribuyente = null;
    this.vehiculoData = null;
    this.licenciaData = null;

    const rucClean = this.ruc.trim();
    const placaClean = this.placa.trim().toUpperCase();

    if (!/^\d{13}$/.test(rucClean)) {
      this.error = 'Ingrese un RUC válido de 13 dígitos.';
      return;
    }
    if (!/^[A-Z0-9]{5,8}$/.test(placaClean)) {
      this.error = 'Ingrese una placa vehicular válida.';
      return;
    }

    const cedula = rucClean.substring(0, 10);
    this.cargando = true;

    forkJoin({
      sriContribuyente: this.api.consultarContribuyente(rucClean),
      sriVehiculo: this.api.consultarVehiculo(placaClean),
      antLicencia: this.api.consultarLicencia(cedula)
    }).subscribe({
      next: (res) => {
        this.contribuyente = Array.isArray(res.sriContribuyente)
          ? res.sriContribuyente[0]
          : res.sriContribuyente;
        this.vehiculoData = res.sriVehiculo;
        this.licenciaData = res.antLicencia;
        this.cargando = false;
      },
      error: (err) => {
        this.error = err?.error?.message || 'Error al realizar la consulta vehicular.';
        this.cargando = false;
      }
    });
  }
}
