package cl.veterinaria.sanmarcos.controller;

import cl.veterinaria.sanmarcos.dto.ReporteDto;
import cl.veterinaria.sanmarcos.service.ReporteService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reportes")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class ReporteController {
    private final ReporteService service;

    public ReporteController(ReporteService service) {
        this.service = service;
    }

    @GetMapping
    public ReporteDto resumen(@RequestParam(defaultValue = "mes") String periodo) {
        return service.resumen(periodo);
    }
}
