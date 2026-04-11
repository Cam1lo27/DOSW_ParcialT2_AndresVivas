package edu.dosw.parcial.controller.dtos.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String qrCode;
    private Integer stock;
    private String status;
}