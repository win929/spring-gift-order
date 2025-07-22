package gift.api.option.domain;

import gift.api.product.domain.Product;
import gift.exception.option.InvalidOptionQuantityException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "option", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "name"})
})
public class Option {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    protected Option() {
    }

    public Option(String name, int quantity, Product product) {
        this.name = name;
        this.quantity = quantity;
        this.product = product;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void update(String name, int quantity) {
        this.name = name;
        this.quantity = quantity;
    }

    public void subtractQuantity(int amount) {
        int restQuantity = this.quantity - amount;
        if (restQuantity < 0) {
            throw new InvalidOptionQuantityException("재고가 부족합니다.");
        }
        this.quantity = restQuantity;
    }
}
