package gift.api.product.service;

import gift.api.option.domain.Option;
import gift.api.option.dto.OptionRequestDto;
import gift.api.option.dto.OptionResponseDto;
import gift.api.option.repository.OptionRepository;
import gift.api.product.domain.Product;
import gift.api.product.dto.ProductRequestDto;
import gift.api.product.dto.ProductResponseDto;
import gift.api.product.repository.ProductRepository;
import gift.exception.conflict.OptionNameDuplicateException;
import gift.exception.notfound.OptionNotFoundException;
import gift.exception.notfound.ProductNotFoundException;
import gift.exception.option.OptionPolicyException;
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
    private final OptionRepository optionRepository;

    public ProductService(ProductRepository productRepository, OptionRepository optionRepository) {
        this.productRepository = productRepository;
        this.optionRepository = optionRepository;
    }

    public Page<ProductResponseDto> findAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(ProductResponseDto::from);
    }

    public ProductResponseDto findProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto createProduct(ProductRequestDto productRequestDto) {
        Product createdProduct = new Product(
                productRequestDto.name(),
                productRequestDto.price(),
                productRequestDto.imageUrl()
        );

        Product savedProduct = productRepository.save(createdProduct);

        Option defaultOption = new Option("기본 옵션", 1, savedProduct);
        optionRepository.save(defaultOption);

        return ProductResponseDto.from(savedProduct);
    }

    @Transactional
    public ProductResponseDto updateProduct(Long id, ProductRequestDto productRequestDto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

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
        findProductByIdOrThrow(productId);

        return optionRepository.findByProductId(productId).stream()
                .map(OptionResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public OptionResponseDto addOption(Long productId, OptionRequestDto requestDto) {
        Product product = findProductByIdOrThrow(productId);

        validateOptionNameDuplicate(product, requestDto.name());

        Option newOption = new Option(requestDto.name(), requestDto.quantity(), product);
        Option savedOption = optionRepository.save(newOption);

        return OptionResponseDto.from(savedOption);
    }

    @Transactional
    public OptionResponseDto updateOption(Long productId, Long optionId,
            OptionRequestDto requestDto) {
        Product product = findProductByIdOrThrow(productId);

        Option option = findOptionByIdOrThrow(optionId);

        option.validateProduct(productId);

        // 수정하려는 이름이 현재 이름과 다른 경우, 기존 옵션 이름과 중복 검사
        if (!option.getName().equals(requestDto.name())) {
            validateOptionNameDuplicate(product, requestDto.name());
        }

        option.update(requestDto.name(), requestDto.quantity());

        return OptionResponseDto.from(option);
    }

    @Transactional
    public void deleteOption(Long productId, Long optionId) {
        Product product = findProductByIdOrThrow(productId);

        Option option = findOptionByIdOrThrow(optionId);

        option.validateProduct(productId);

        if (option.getProduct().getOptions().size() <= 1) {
            throw new OptionPolicyException("상품에는 최소 1개의 옵션이 존재해야 합니다.");
        }

        // 부모의 관리 목록에서 자식을 빼는 방식
        product.getOptions().remove(option);
    }

    private void validateOptionNameDuplicate(Product product, String optionName) {
        optionRepository.findByProductAndName(product, optionName).ifPresent(opt -> {
            throw new OptionNameDuplicateException(optionName);
        });
    }

    private Product findProductByIdOrThrow(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private Option findOptionByIdOrThrow(Long optionId) {
        return optionRepository.findById(optionId)
                .orElseThrow(() -> new OptionNotFoundException(optionId));
    }
}