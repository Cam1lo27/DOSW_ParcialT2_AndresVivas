package edu.dosw.parcial.core.services;

import edu.dosw.parcial.controller.dtos.request.CreateProductRequest;
import edu.dosw.parcial.controller.dtos.response.ProductResponse;
import edu.dosw.parcial.core.exception.BusinessException;
import edu.dosw.parcial.persistence.entities.ProductEntity;
import edu.dosw.parcial.persistence.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse create(CreateProductRequest request) {
        log.info("[CREATE_PRODUCT] Creando producto: {}", request.getName());

        if (productRepository.findByQrCode(request.getQrCode()).isPresent()) {
            throw new BusinessException("Ya existe un producto con ese código QR", HttpStatus.CONFLICT);
        }

        ProductEntity product = ProductEntity.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .qrCode(request.getQrCode())
                .stock(request.getStock())
                .status(ProductEntity.ProductStatus.DISPONIBLE)
                .build();

        ProductEntity saved = productRepository.save(product);
        log.info("[CREATE_PRODUCT] Producto creado con id: {}", saved.getId());
        return toResponse(saved);
    }

    public List<ProductResponse> getAll() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse getByQrCode(String qrCode) {
        ProductEntity product = productRepository.findByQrCode(qrCode)
                .orElseThrow(() -> new BusinessException("Producto no encontrado", HttpStatus.NOT_FOUND));
        return toResponse(product);
    }

    private ProductResponse toResponse(ProductEntity product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .qrCode(product.getQrCode())
                .stock(product.getStock())
                .status(product.getStatus().name())
                .build();
    }
}