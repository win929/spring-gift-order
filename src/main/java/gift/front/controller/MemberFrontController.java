package gift.front.controller;

import gift.api.product.service.ProductService;
import gift.api.wish.service.WishService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/members")
public class MemberFrontController {

    private final ProductService productService;
    private final WishService wishService;

    public MemberFrontController(ProductService productService, WishService wishService) {
        this.productService = productService;
        this.wishService = wishService;
    }

    @GetMapping("/login")
    public String loginMember() {
        return "member/login";
    }

    @GetMapping("/register")
    public String registerMember() {
        return "member/register";
    }

    @GetMapping("/logout")
    public String logout(HttpServletResponse response) {
        // Authorization 쿠키를 삭제
        Cookie cookie = new Cookie("Authorization", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        return "redirect:/members/login";
    }

    @GetMapping("/products")
    public String allProducts(
            @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable,
            @RequestParam(name = "sort", defaultValue = "id,asc") String sort,
            Model model,
            HttpServletRequest request
    ) {
        String userEmail = (String) request.getAttribute("userEmail");
        String userRole = (String) request.getAttribute("userRole");

        if (userEmail != null) {
            model.addAttribute("userEmail", userEmail);
        }

        if (userRole != null) {
            model.addAttribute("userRole", userRole);
        }

        model.addAttribute("products", productService.findAllProducts(pageable));
        model.addAttribute("sort", sort);

        return "member/product-list";
    }

    @GetMapping("/products/{id}")
    public String productDetail(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("product", productService.findProductById(id));
        model.addAttribute("options", productService.getOptionsByProductId(id));

        return "member/product-detail";
    }

    @GetMapping("/wishes")
    public String wishlist(
            @PageableDefault(size = 5, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(name = "sort", defaultValue = "createdDate,desc") String sort,
            Model model,
            HttpServletRequest request
    ) {
        String userEmail = (String) request.getAttribute("userEmail");
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userRole", request.getAttribute("userRole"));

        model.addAttribute("wishlistPage", wishService.getWishlist(userEmail, pageable));
        model.addAttribute("sort", sort);

        return "member/wishlist";
    }

    @DeleteMapping("/wishes/{wishId}")
    public String deleteWish(
            @RequestAttribute("userEmail") String email,
            @PathVariable Long wishId
    ) {
        wishService.removeProductFromWishlist(email, wishId);

        return "redirect:/members/wishes";
    }
}
