package edu.dosw.parcial.controller.dtos.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateProductRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Min(value = 0, message = "El precio no puede ser negativo")
    private Double price;

    @NotBlank(message = "El código QR es obligatorio")
    private String qrCode;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;
}