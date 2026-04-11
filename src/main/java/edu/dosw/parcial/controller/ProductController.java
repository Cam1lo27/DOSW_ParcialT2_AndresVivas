package edu.dosw.parcial.controller;

import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ApiResponse;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Productos", description = "Gestión de productos")
public class ProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Crear un nuevo producto")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody CreateProductRequest request) {
        ProductResponse response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Producto creado exitosamente", response));
    }

    @GetMapping
    @Operation(summary = "Listar todos los productos")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.ok("Productos encontrados", productService.getAll()));
    }

    @GetMapping("/qr/{qrCode}")
    @Operation(summary = "Buscar producto por código QR")
    public ResponseEntity<ApiResponse<ProductResponse>> getByQrCode(@PathVariable String qrCode) {
        return ResponseEntity.ok(ApiResponse.ok("Producto encontrado", productService.getByQrCode(qrCode)));
    }
}