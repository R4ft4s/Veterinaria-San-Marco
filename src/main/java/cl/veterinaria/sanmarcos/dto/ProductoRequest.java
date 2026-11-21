package cl.veterinaria.sanmarcos.dto;

public record ProductoRequest(String id, String categoria, String nombre, String principioActivo,
                              String presentacion, String especie, Integer stock, Integer precio,
                              Integer precioOferta, String icono) {}
