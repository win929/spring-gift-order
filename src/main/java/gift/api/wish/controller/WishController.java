package gift.api.wish.controller;

import gift.api.wish.dto.WishRequestDto;
import gift.api.wish.dto.WishResponseDto;
import gift.api.wish.service.WishService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(("/api/wishes"))
public class WishController {

    private final WishService wishService;

    public WishController(WishService wishService) {
        this.wishService = wishService;
    }

    @GetMapping
    public ResponseEntity<List<WishResponseDto>> getWishlist(
            @RequestAttribute("userEmail") String email,
            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable) {
        List<WishResponseDto> wishlist = wishService.getWishlist(email, pageable)
                .getContent();

        return ResponseEntity.ok(wishlist);
    }

    @PostMapping
    public ResponseEntity<WishResponseDto> addProductToWishlist(
            @RequestAttribute("userEmail") String email,
            @Valid @RequestBody WishRequestDto wishRequestDto
    ) {
        WishResponseDto wishResponseDto = wishService.addProductToWishlist(email,
                wishRequestDto.productId());

        URI location = URI.create("/members/products/" + wishResponseDto.product().id());

        return ResponseEntity.created(location).body(wishResponseDto);
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> removeProductFromWishlist(
            @RequestAttribute("userEmail") String email,
            @PathVariable Long wishId) {
        wishService.removeProductFromWishlist(email, wishId);

        return ResponseEntity.noContent().build();
    }
}
