package gift.api.wish.domain;

import gift.api.member.domain.Member;
import gift.api.product.domain.Product;
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
import java.time.LocalDateTime;

@Entity
@Table(name = "wish", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"member_id", "product_id"})
})
public class Wish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private final LocalDateTime createdDate;

    protected Wish() {
        this.createdDate = LocalDateTime.now();
    }

    public Wish(Member member, Product product) {
        this.member = member;
        this.product = product;
        this.createdDate = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Product getProduct() {
        return product;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}
