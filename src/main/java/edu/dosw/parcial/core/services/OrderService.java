package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.response.OrderResponse;
import edu.dosw.parcial.core.exception.BusinessException;
import edu.dosw.parcial.core.models.OrderStatusEnum;
import edu.dosw.parcial.persistence.entities.*;
import edu.dosw.parcial.persistence.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Transactional
    public OrderResponse createOrder(String userEmail, CreateOrderRequest request) {
        log.info("[CREATE_ORDER] Iniciando creación de pedido para: {}", userEmail);

        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        if (orderRepository.existsByUserAndStatus(user, OrderStatusEnum.CREADO)) {
            log.warn("[CREATE_ORDER] Usuario {} ya tiene un pedido activo", userEmail);
            throw new BusinessException("Ya tienes un pedido activo. Espera a que sea entregado o cancélalo.", HttpStatus.CONFLICT);
        }

        List<OrderItemEntity> items = new ArrayList<>();
        double total = 0.0;

        for (var itemReq : request.getItems()) {
            ProductEntity product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new BusinessException(
                            "Producto con id " + itemReq.getProductId() + " no encontrado", HttpStatus.NOT_FOUND));

            if (product.getStatus() == ProductEntity.ProductStatus.NO_DISPONIBLE) {
                log.warn("[CREATE_ORDER] Producto {} no disponible", product.getName());
                throw new BusinessException("El producto '" + product.getName() + "' no está disponible", HttpStatus.BAD_REQUEST);
            }

            if (product.getStock() < itemReq.getQuantity()) {
                log.warn("[CREATE_ORDER] Stock insuficiente para {}: solicitado={}, disponible={}",
                        product.getName(), itemReq.getQuantity(), product.getStock());
                throw new BusinessException(
                        "Stock insuficiente para '" + product.getName() + "'. Disponible: " + product.getStock(),
                        HttpStatus.BAD_REQUEST);
            }

            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            total += product.getPrice() * itemReq.getQuantity();
            items.add(OrderItemEntity.builder()
                    .product(product)
                    .quantity(itemReq.getQuantity())
                    .build());
        }

        OrderEntity order = OrderEntity.builder()
                .user(user)
                .status(OrderStatusEnum.CREADO)
                .total(total)
                .createdAt(LocalDateTime.now())
                .build();

        OrderEntity saved = orderRepository.save(order);

        for (OrderItemEntity item : items) {
            item.setOrder(saved);
        }
        saved.setItems(items);
        saved = orderRepository.save(saved);

        log.info("[CREATE_ORDER] Pedido creado con id: {} para usuario: {}", saved.getId(), userEmail);
        return toResponse(saved);
    }

    public OrderResponse getActiveOrder(String userEmail) {
        log.info("[GET_ORDER] Buscando pedido activo para: {}", userEmail);
        UserEntity user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new BusinessException("Usuario no encontrado", HttpStatus.NOT_FOUND));

        OrderEntity order = orderRepository.findByUserAndStatus(user, OrderStatusEnum.CREADO)
                .orElseThrow(() -> new BusinessException("No tienes ningún pedido activo", HttpStatus.NOT_FOUND));

        return toResponse(order);
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId, String userEmail) {
        log.info("[CANCEL_ORDER] Usuario {} intenta cancelar pedido {}", userEmail, orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado", HttpStatus.NOT_FOUND));

        if (!order.getUser().getEmail().equals(userEmail)) {
            throw new BusinessException("No tienes permiso para cancelar este pedido", HttpStatus.FORBIDDEN);
        }

        if (order.getStatus() != OrderStatusEnum.CREADO) {
            log.warn("[CANCEL_ORDER] Pedido {} no cancelable, estado: {}", orderId, order.getStatus());
            throw new BusinessException("Solo se pueden cancelar pedidos en estado CREADO", HttpStatus.BAD_REQUEST);
        }

        for (OrderItemEntity item : order.getItems()) {
            ProductEntity product = item.getProduct();
            product.setStock(product.getStock() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(OrderStatusEnum.CANCELADO);
        OrderEntity saved = orderRepository.save(order);
        log.info("[CANCEL_ORDER] Pedido {} cancelado exitosamente", orderId);
        return toResponse(saved);
    }

    @Transactional
    public OrderResponse updateStatus(Long orderId, OrderStatusEnum newStatus) {
        log.info("[UPDATE_STATUS] Actualizando pedido {} a estado {}", orderId, newStatus);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException("Pedido no encontrado", HttpStatus.NOT_FOUND));

        order.setStatus(newStatus);
        return toResponse(orderRepository.save(order));
    }

    private OrderResponse toResponse(OrderEntity order) {
        List<OrderResponse.OrderItemResponse> itemResponses = order.getItems().stream()
                .map(item -> OrderResponse.OrderItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .subtotal(item.getProduct().getPrice() * item.getQuantity())
                        .build())
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .userEmail(order.getUser().getEmail())
                .items(itemResponses)
                .status(order.getStatus().name())
                .total(order.getTotal())
                .createdAt(order.getCreatedAt())
                .build();
    }
}