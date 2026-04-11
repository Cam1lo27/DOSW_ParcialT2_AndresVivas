package edu.dosw.parcial.controller;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.response.ApiResponse;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.core.models.OrderStatusEnum;
import edu.dosw.parcial.core.services.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Pedidos", description = "Creación y gestión de pedidos")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Crear un nuevo pedido")
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            @RequestParam String userEmail) {
        OrderResponse response = orderService.createOrder(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pedido creado exitosamente", response));
    }

    @GetMapping("/active")
    @Operation(summary = "Obtener el pedido activo del usuario")
    public ResponseEntity<ApiResponse<OrderResponse>> getActiveOrder(
            @RequestParam String userEmail) {
        OrderResponse response = orderService.getActiveOrder(userEmail);
        return ResponseEntity.ok(ApiResponse.ok("Pedido activo encontrado", response));
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancelar un pedido en estado CREADO")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable Long id,
            @RequestParam String userEmail) {
        OrderResponse response = orderService.cancelOrder(id, userEmail);
        return ResponseEntity.ok(ApiResponse.ok("Pedido cancelado exitosamente", response));
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Actualizar estado del pedido (solo ADMIN)")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatusEnum status) {
        OrderResponse response = orderService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", response));
    }
}