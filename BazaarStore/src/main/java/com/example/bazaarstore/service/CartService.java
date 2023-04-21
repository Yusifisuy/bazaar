package com.example.bazaarstore.service;

import com.example.bazaarstore.dto.cart.CartDTO;
import com.example.bazaarstore.dto.cart.CartItemDTO;
import com.example.bazaarstore.dto.payment.PaymentDTO;
import com.example.bazaarstore.dto.product.ProductShowDTO;
import com.example.bazaarstore.model.entity.*;
import com.example.bazaarstore.repository.*;
import com.itextpdf.text.DocumentException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CartService {
    private final AdressRepository adressRepository;
    private final CartItemRepository cartItemRepository;

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    private final MailService mailService;

    private final SmsService smsService;
    public CartService(CartRepository cartRepository, UserRepository userRepository,
                       ProductRepository productRepository,
                       CartItemRepository cartItemRepository, MailService mailService,
                       AdressRepository adressRepository, SmsService smsService) {
        this.cartRepository = cartRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartItemRepository = cartItemRepository;
        this.mailService = mailService;
        this.adressRepository = adressRepository;
        this.smsService = smsService;
    }

    public ProductShowDTO addProductToCart(Long productId,int quantity){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        log.info("Product id :" + product.getId());
        if (cartRepository.findByUser(user).isEmpty()){
            Cart cart = Cart.builder().user(user).build();
            cartRepository.save(cart);
        }
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        cartItemRepository.save(CartItem.builder().cart(cart).quantity(quantity).product(product).status(true).build());

        return ProductShowDTO.builder().productId(product.getId()).name(product.getName()).sku(product.getSku())
                .categoryName(product.getCategory().getCategoryName()).unitPrice(product.getUnitPrice())
                .image(product.getImage())
                .description(product.getDescription()).unitsInStock(product.getUnitsInStock())
                .username(product.getUser().getUsername()).build();
    }

    public CartDTO showCart(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Cart cart = cartRepository.findByUser(user).orElseThrow();
        log.info("Cart :" + cart.getUser().getUsername());
        List<CartItem> cartItems = cartItemRepository.findAllByCartAndStatusIsTrue(cart);

        List<CartItemDTO> cartItemDTOS = cartItems.stream().map(cartItem -> CartItemDTO.builder()
                .product(cartItem.getProduct()).quantity(cartItem.getQuantity())
                .cost(cartItem.getProduct().getUnitPrice()* cartItem.getQuantity()).build()).toList();

        double totalCost = 0;
        for (CartItemDTO c : cartItemDTOS){
            log.info("CART ITEM :" + c.getProduct().getName());
            totalCost+=c.getCost();
        }
        log.info("total cost :" + totalCost);
        return CartDTO.builder().cartItemDTOS(cartItemDTOS).totalCost(totalCost).build();
    }


    public String makePayment(PaymentDTO paymentDTO){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Adress adress = adressRepository.findByIdAndUser(paymentDTO.getAdressId(),user).orElseThrow();
        CartDTO cartDTO =showCart();
        List<CartItemDTO> cartItemDTOS = cartDTO.getCartItemDTOS();
        List<Product> products = cartItemDTOS.stream()
                .map(CartItemDTO::getProduct).toList();
        for (Product product : products){
            String userMail = product.getUser().getEmail();
            try {
                mailService.sendPaymentMail(userMail,user.getUsername(),"new Order",user,adress,"payment.pdf");
            } catch (MessagingException | DocumentException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        resetCart();
        return "succesfull , Total cost :" + cartDTO.getTotalCost();
    }

    private void resetCart(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Cart cart = cartRepository.findByUser(user).orElseThrow();
        List<CartItem> cartItems = cartItemRepository.findAllByCartAndStatusIsTrue(cart);
        for (CartItem cartItem : cartItems){
            cartItem.setStatus(false);
        }
        cartItemRepository.saveAll(cartItems);
    }


}
