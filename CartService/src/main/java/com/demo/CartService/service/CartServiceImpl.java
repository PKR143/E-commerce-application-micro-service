package com.demo.CartService.service;

import com.demo.CartService.dto.CartRequest;
import com.demo.CartService.dto.CartResponse;
import com.demo.CartService.dto.GeneralResponse;
import com.demo.CartService.dto.Response;
import com.demo.CartService.entity.ProductDetailsEntity;
import com.demo.CartService.entity.UserCartDetailsEntity;
import com.demo.CartService.exception.EcommerceException;
import com.demo.CartService.repository.CartRepository;
import com.demo.CartService.repository.ProductRepository;
import com.demo.CartService.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService{

    @Autowired
    CartRepository cartRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductRepository productRepository;
    

    @Override
    public ResponseEntity<?> addToCart(CartRequest cart) {
        log.info("add to cart request: {}",cart);

        try{
            if(cart.getUsername() == null || cart.getUsername().isEmpty()){
                log.info("username is missing in the request");
                throw new EcommerceException("Username is missing in the request");
            }
            if(cart.getProductId() == null){
                log.info("product id is missing in the request");
                throw new EcommerceException("product id is missing in the request");
            }

//            if(cart.getQuantity() == null){
//                cart.setQuantity(1L);
//            }

            if(userRepository.findById(cart.getUsername()).isEmpty()){
                log.info("Username not exists in db");
                throw new EcommerceException("Username not exists.");
            }

            Optional<ProductDetailsEntity> productDetailsEntityOpt = productRepository.findById(cart.getProductId());

            if(productDetailsEntityOpt.isEmpty()){
                log.info("Product not exists in db");
                throw new EcommerceException("No Such product exists.");
            }
            ProductDetailsEntity productEntity = productDetailsEntityOpt.get();

            Optional<UserCartDetailsEntity> cartOpt = cartRepository.findById(cart.getUsername());
            UserCartDetailsEntity  cartEntity;
            List<ProductDetailsEntity> list = new ArrayList<>();
            Double price = 0D;
            if(cartOpt.isPresent()){
                //cart already created
                cartEntity = cartOpt.get();
                list = cartEntity.getItems();


                for(ProductDetailsEntity product: list){
                    if(product.getProductId().equals(cart.getProductId())){
                        log.info("Product is already present in DB");
                        return ResponseEntity.ok(new GeneralResponse(mapToCartResponse(cart.getUsername(), productEntity),1L, "Item is already present in Cart."));
                    }
                }
                if(cartEntity.getAmount() != null){
                    price = cartEntity.getAmount();
                }
            }else{
                //new cart creation
                cartEntity = new UserCartDetailsEntity();
                cartEntity.setUsername(cart.getUsername());
            }
            //add item to cart
            price += productEntity.getPrice();
            int priceInt = (int) (price * 100);
            price = (double) priceInt/100;
            cartEntity.setAmount(price);
            list.add(productEntity);
            //Save to db
            cartRepository.save(cartEntity);
            log.info(cart.getProductId()+" Item added to cart");
            return ResponseEntity.status(HttpStatus.CREATED).body(new GeneralResponse(mapToCartResponse(cart.getUsername(),productEntity),1L,"Item added to cart."));
        }catch(EcommerceException e){
            throw e;
        }
        catch (Exception e) {
            log.info("Exception @addToCart due to: {}",e.getMessage());
            throw e;
        }

    }

    private Response mapToCartResponse(String username, ProductDetailsEntity productEntity) {
        return CartResponse.builder()
                .username(username)
                .productId(productEntity.getProductId())
                .productName(productEntity.getProductName())
                .productDescription(productEntity.getProductDescription())
                .productSize(productEntity.getProductSize())
                .productColour(productEntity.getProductColour())
                .stockAvailable(productEntity.getStockAvailable())
                .price(productEntity.getPrice())
                .build();
    }

    @Override
    public ResponseEntity<?> getCartItems(String username) {
        log.info("fetching cart items for user: {}",username);
        try{

            if(username == null || username.isEmpty()){
                log.info("username is missing in the request");
                throw new EcommerceException("Username is missing in the request");
            }

            Optional<UserCartDetailsEntity> cartOpt = cartRepository.findById(username);
            if(cartOpt.isEmpty()){
                log.info("username has not been created for the username: {}",username);
                return ResponseEntity.ok(new GeneralResponse(null, 1L, "Empty Cart"));

            }
            UserCartDetailsEntity cartEntity = cartOpt.get();
            return ResponseEntity.ok(cartEntity);
        }catch(EcommerceException e){
            throw e;
        }catch(Exception e){
            log.info("Exception @getCartItems due to : {}",e.getMessage());
            throw e;
        }
    }
}

