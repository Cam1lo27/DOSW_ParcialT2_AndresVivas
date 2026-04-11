package edu.dosw.parcial.core;

import edu.dosw.parcial.controller.dtos.request.CreateOrderRequest;
import edu.dosw.parcial.controller.dtos.request.OrderItemRequest;
import edu.dosw.parcial.core.exception.BusinessException;
import edu.dosw.parcial.core.models.OrderStatusEnum;
import edu.dosw.parcial.core.models.RoleEnum;
import edu.dosw.parcial.core.services.OrderService;
import edu.dosw.parcial.persistence.entities.*;
import edu.dosw.parcial.persistence.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private UserRepository userRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks
    private OrderService orderService;

    private UserEntity user;
    private ProductEntity product;
    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        user = UserEntity.builder()
                .id(1L)
                .email("andres@unal.edu.co")
                .fullName("Andres Vivas")
                .role(RoleEnum.CLIENTE)
                .password("encoded")
                .build();

        product = ProductEntity.builder()
                .id(1L)
                .name("Café")
                .price(2500.0)
                .stock(10)
                .status(ProductEntity.ProductStatus.DISPONIBLE)
                .build();

        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(1L);
        item.setQuantity(2);

        request = new CreateOrderRequest();
        request.setItems(List.of(item));
    }

    @Test
    void createOrder_exitoso() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserAndStatus(any(), any())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any())).thenAnswer(inv -> {
            OrderEntity o = inv.getArgument(0);
            o.setId(1L);
            if (o.getItems() == null) o.setItems(new ArrayList<>());
            return o;
        });

        var response = orderService.createOrder("andres@unal.edu.co", request);

        assertNotNull(response);
        assertEquals("CREADO", response.getStatus());
        assertEquals(5000.0, response.getTotal());
    }

    @Test
    void createOrder_usuarioYaTienePedidoActivo_lanzaExcepcion() {
        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserAndStatus(any(), eq(OrderStatusEnum.CREADO))).thenReturn(true);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder("andres@unal.edu.co", request));

        assertTrue(ex.getMessage().contains("pedido activo"));
    }

    @Test
    void createOrder_productoNoDisponible_lanzaExcepcion() {
        product.setStatus(ProductEntity.ProductStatus.NO_DISPONIBLE);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserAndStatus(any(), any())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder("andres@unal.edu.co", request));

        assertTrue(ex.getMessage().contains("no está disponible"));
    }

    @Test
    void createOrder_stockInsuficiente_lanzaExcepcion() {
        product.setStock(1);

        when(userRepository.findByEmail(any())).thenReturn(Optional.of(user));
        when(orderRepository.existsByUserAndStatus(any(), any())).thenReturn(false);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.createOrder("andres@unal.edu.co", request));

        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void cancelOrder_exitoso() {
        OrderItemEntity item = OrderItemEntity.builder()
                .product(product).quantity(2).build();

        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user)
                .status(OrderStatusEnum.CREADO)
                .total(5000.0)
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>(List.of(item)))
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenReturn(order);

        var response = orderService.cancelOrder(1L, "andres@unal.edu.co");

        assertEquals("CANCELADO", response.getStatus());
        assertEquals(12, product.getStock());
    }

    @Test
    void cancelOrder_estadoNoCreado_lanzaExcepcion() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user)
                .status(OrderStatusEnum.EN_PREPARACION)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L, "andres@unal.edu.co"));

        assertTrue(ex.getMessage().contains("estado CREADO"));
    }

    @Test
    void cancelOrder_usuarioNoPropietario_lanzaExcepcion() {
        OrderEntity order = OrderEntity.builder()
                .id(1L).user(user)
                .status(OrderStatusEnum.CREADO)
                .items(new ArrayList<>())
                .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        assertThrows(BusinessException.class,
                () -> orderService.cancelOrder(1L, "otro@unal.edu.co"));
    }
}