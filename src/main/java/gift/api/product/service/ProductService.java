package gift.api.product.service;

import gift.api.option.domain.Option;
import gift.api.option.dto.OptionRequestDto;
import gift.api.option.dto.OptionResponseDto;
import gift.api.product.domain.Product;
import gift.api.product.dto.ProductRequestDto;
import gift.api.product.dto.ProductResponseDto;
import gift.api.product.repository.ProductRepository;
import gift.exception.notfound.ProductNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductResponseDto> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponseDto::from);
    }

    public ProductResponseDto findProductById(Long id) {
        Product product = findProductByIdOrThrow(id);

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Product createdProduct = new Product(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.imageUrl()
        );

        createdProduct.addOption("기본 옵션", 1);

        Product savedProduct = productRepository.save(createdProduct);

        return ProductResponseDto.from(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
        Product product = findProductByIdOrThrow(id);

        product.update(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.imageUrl()
        );

        return ProductResponseDto.from(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }

        productRepository.deleteById(id);
    }

    public List<OptionResponseDto> getOptionsByProductId(Long productId) {
        Product product = findProductByIdOrThrow(productId);

        return product.getOptions().stream()
                .map(OptionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OptionResponseDto addOption(Long productId, OptionRequestDto requestDto) {
        Product product = findProductByIdOrThrow(productId);

        Option newOption = product.addOption(requestDto.name(), requestDto.quantity());

        return OptionResponseDto.from(newOption);
    }

    @Transactional
    public OptionResponseDto updateOption(Long productId, Long optionId,
            OptionRequestDto requestDto) {
        Product product = findProductByIdOrThrow(productId);

        Option updatedOption = product.updateOption(optionId, requestDto.name(),
                requestDto.quantity());

        return OptionResponseDto.from(updatedOption);
    }

    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        Product product = findProductByIdOrThrow(productId);

        product.removeOption(optionId);
    }

    private Product findProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}