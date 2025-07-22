package gift.api.product.domain;

import gift.api.option.domain.Option;
import gift.exception.conflict.OptionNameDuplicateException;
import gift.exception.notfound.OptionNotFoundException;
import gift.exception.option.OptionPolicyException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "product")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private String imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Option> options = new ArrayList<>();

    protected Product() {
    }

    public Product(String name, Long price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public List<Option> getOptions() {
        return Collections.unmodifiableList(options);
    }

    public void update(String name, Long price, String imageUrl) {
        this.name = name;
        this.price = price;
        this.imageUrl = imageUrl;
    }

    public Option addOption(String name, int quantity) {
        validateOptionNameDuplicate(name);

        Option newOption = new Option(name, quantity, this);
        this.options.add(newOption);
        newOption.setProduct(this);

        return newOption;
    }

    public Option updateOption(Long optionId, String name, int quantity) {
        Option optionToUpdate = this.options.stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new OptionNotFoundException(optionId));

        if (!optionToUpdate.getName().equals(name)) {
            validateOptionNameDuplicate(name);
        }

        optionToUpdate.update(name, quantity);

        return optionToUpdate;
    }

    public void removeOption(Long optionId) {
        if (this.options.size() <= 1) {
            throw new OptionPolicyException("상품에는 최소 1개의 옵션이 존재해야 합니다.");
        }

        Option optionToRemove = this.options.stream()
                .filter(option -> option.getId().equals(optionId))
                .findFirst()
                .orElseThrow(() -> new OptionNotFoundException(optionId));

        this.options.remove(optionToRemove);
    }

    private void validateOptionNameDuplicate(String name) {
        boolean isDuplicate = this.options.stream()
                .anyMatch(option -> option.getName().equals(name));

        if (isDuplicate) {
            throw new OptionNameDuplicateException(name);
        }
    }
}
