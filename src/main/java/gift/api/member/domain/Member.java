package gift.api.member.domain;

import gift.api.product.domain.Product;
import gift.api.wish.domain.Wish;
import gift.exception.conflict.WishDuplicateException;
import gift.exception.notfound.WishNotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wish> wishList = new ArrayList<>();

    protected Member() {
    }

    public Member(String email, String password, MemberRole role) {
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }

    public List<Wish> getWishList() {
        return Collections.unmodifiableList(wishList);
    }

    public Wish addWish(Product product) {
        if (this.wishList.stream().anyMatch(wish -> wish.getProduct().equals(product))) {
            throw new WishDuplicateException(product.getName());
        }

        Wish newWish = new Wish(this, product);
        this.wishList.add(newWish);

        return newWish;
    }

    public void removeWish(Long wishId) {
        Wish wishToRemove = this.wishList.stream()
                .filter(wish -> wish.getId().equals(wishId))
                .findFirst()
                .orElseThrow(() -> new WishNotFoundException(wishId));

        this.wishList.remove(wishToRemove);
    }
}
