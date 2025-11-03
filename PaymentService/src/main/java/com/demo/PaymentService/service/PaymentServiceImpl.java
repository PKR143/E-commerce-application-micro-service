package com.demo.PaymentService.service;

import com.demo.PaymentService.dto.*;
import com.demo.PaymentService.entity.*;
import com.demo.PaymentService.exception.EcommerceException;
import com.demo.PaymentService.repository.CardRepository;
import com.demo.PaymentService.repository.CartRepository;
import com.demo.PaymentService.repository.PaymentRepository;
import com.demo.PaymentService.repository.UserRepository;
import com.demo.PaymentService.util.CardUtil;
import com.demo.PaymentService.util.TxnUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService{

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    CardRepository cardRepo;

    @Autowired
    UserRepository userRepo;

    @Autowired
    CartRepository cartRepo;
    
    @Autowired
    PaymentRepository paymentRepo;

    @Autowired
    OtpGenerateService otpService;

    @Autowired
    MailService mailService;

    @Autowired
    PubSubService pubSubService;

    private ExecutorService executorService = Executors.newFixedThreadPool(20);

    @Override
    public ResponseEntity<?> doPayment(TxnRequest request) throws RuntimeException {
        log.info("---------------payment request received---------------");
        try{

            log.info("----------------validating txn request-------");

//            Optional<CardDetailsEntity> existCard = cardRepo.findById(request.getCardNumber());
            Optional<CardDetailsEntity> existCard = cardRepo.findByCardNumberAndUsername(request.getCardNumber(), request.getUsername());
            if(existCard.isEmpty()){
                log.info("card is not registered");
                throw new EcommerceException("Card is not registered");
            }
            CardDetailsEntity existCardEntity = existCard.get();
            String username = existCardEntity.getUsername();
            UserEntity exists = userRepo.findByUsername(username);
            if(exists == null){
                log.info("requested username not exists in db");
                throw new EcommerceException("Username not exists, please try with valid username");
            }

            //check card is active or not
            if(!existCardEntity.getIsActive()){
                log.info("card is not active");
                throw new EcommerceException("Card is not active!!!");
            }

            Optional<UserCartDetailsEntity> cartOpt = cartRepo.findById(username);
            if(cartOpt.isEmpty()){
                log.info("Cart is empty, first add items to cart then place order");
                throw new EcommerceException("Cart is empty, first add items to cart then place order");
            }

            UserCartDetailsEntity cartDetailsEntity = cartOpt.get();
            if(cartDetailsEntity.getItems().isEmpty()){
                log.info("empty cart, add items in  the cart first.");
                throw new EcommerceException("Cart is empty, first add items to cart then place order");
            }
//
//            //check the request amount
            Double requestAmount;
//            try{
//                requestAmount = Double.parseDouble(request.getRequestAmount());
//            }catch (Exception e){
//                log.info("invalid amount received in request");
//                throw new EcommerceException("invalid amount entered in request");
//            }

            //extracting the amount from the cart

            requestAmount = cartDetailsEntity.getAmount();

            //validating the requests

            if(!request.getCvv().equals(existCardEntity.getCvv())){
                log.info("incorrect CVV provided in request");
                throw new EcommerceException("incorrect CVV provided in request");
            }

            if(!request.getCardHolderName().equals(existCardEntity.getCardHolderName())){
                log.info("incorrect Card Holder name provided in request");
                throw new EcommerceException("incorrect Card Holder name provided in request");
            }

            if(!request.getExpiryYear().equals(existCardEntity.getExpiryYear())){
                log.info("incorrect Expiry year provided in request");
                throw new EcommerceException("incorrect Expiry year provided in request");
            }

            if(!request.getExpiryMonth().equals(existCardEntity.getExpiryMonth())){
                log.info("incorrect Expiry Month provided in request");
                throw new EcommerceException("incorrect Expiry Month provided in request");
            }

            if(!request.getMobileNo().equals(existCardEntity.getMobileNo())){
                log.info("incorrect Mobile No provided in request");
                throw new EcommerceException("incorrect MobileNo provided in request");
            }
            if(!request.getEmail().equals(existCardEntity.getMailId())){
                log.info("incorrect mail id provided in request");
                throw new EcommerceException("incorrect mail id provided in request");
            }

            if(requestAmount < 100 || requestAmount > existCardEntity.getCardLimit()){
                log.info("amount range should be between 100 & {}",existCardEntity.getCardLimit());
                throw new EcommerceException("amount range should be between 100 & "+existCardEntity.getCardLimit());
            }

            //generate txn id

            TransactionStatusEntity status = new TransactionStatusEntity();

            //check PIN
            String PIN = request.getPin();
            String actualPin = existCardEntity.getPin();

            if(!passwordEncoder.matches(PIN,actualPin)){
                log.info(username+", "+request.getCardNumber()+", wrong PIN entered");
                throw new EcommerceException("wrong PIN entered");
            }

            //initiate the transaction
            Long txnId = generateId();
            status.setTxnId(txnId);
            status.setUsername(username);
            status.setRequestAmount(requestAmount);
            status.setCardNumber(request.getCardNumber());
            status.setCvv(request.getCvv());
            status.setExpiryMonth(request.getExpiryMonth());
            status.setExpiryYear(request.getExpiryYear());
            status.setCardHolderName(request.getCardHolderName());
            status.setBankName(existCardEntity.getBankName());
            status.setIssuedMonth(existCardEntity.getIssuedMonth());
            status.setIssuedYear(existCardEntity.getIssuedYear());
            status.setPin(request.getPin());
            status.setIsActive(existCardEntity.getIsActive());
            status.setAddress(existCardEntity.getAddress());
            status.setMailId(existCardEntity.getMailId());
            status.setMobileNo(request.getMobileNo());
            status.setTransactionDate(new Date());

            status.setStatusCode(TxnUtil.INITIATED);
            status.setStatus("INITIATED");

            paymentRepo.save(status);
            log.info("transaction initiated with txn id: {}",txnId);
            log.info("Generate otp for txnId: {}",txnId);

            String otp = otpService.generateOtp(txnId);

            executorService.submit(()->mailService.sendOtpInMail(otp, status));
//            executorService.submit(()->smsService.sendOtpInMobile(otp, status));

            return ResponseEntity.status(HttpStatus.OK).body(new GeneralResponse(new OtpResponse(txnId,"OTP generated successfully"), 1L, "OTP is valid for 10 minutes only."));
//            Double limit = existCardEntity.getCardLimit();
//            Double usedAmount = existCardEntity.getAmountUsed();
//            //processing transaction
//
//            if((requestAmount+usedAmount)  > limit){
//                log.info(txnId+" Insufficient funds");
//                status.setStatusCode(TxnUtil.FAILED);
//                status.setStatus("FAILED");
//                paymentRepo.save(status);
//                throw new EcommerceException("Insufficient funds");
//            }
//
//            //settle the amount
//            existCardEntity.setAmountUsed(requestAmount+usedAmount);
//            cardRepo.save(existCardEntity);
//            log.info(txnId+" transaction successful");
//            status.setStatusCode(TxnUtil.SUCCESS);
//            status.setStatus("SUCCESS");
//            paymentRepo.save(status);
//
//
//
//            TxnResponse response = TxnResponse.builder()
//                    .txnId(txnId)
//                    .cardNumber(request.getCardNumber())
//                    .cardHolderName(request.getCardHolderName())
//                    .requestAmount(requestAmount)
//                    .cardType(CardUtil.findCardType(request.getCardNumber()))
//                    .mobileNo(request.getMobileNo())
//                    .build();
//            return ResponseEntity.ok(new GeneralResponse(response,1L,"Transaction Successful"));

        }catch(EcommerceException e){
            log.info("EcommerceException @doPayment due to: {}",e.getMessage());
            throw e;
        } catch (Exception e) {
            log.info("Exception @doPayment due to: {}",e.getMessage());
            throw new EcommerceException(e.getMessage());
        }
    }

    @Override
    public ResponseEntity<?> completeTxn(Long txnId, String otp) {
        try {

            if (txnId == null) {
                log.info("txnId is required to complete the transaction");
                throw new EcommerceException("txnId is required to complete the transaction");
            }
            log.info(txnId + " request to complete the transaction");
            //validating the txn id
            Optional<TransactionStatusEntity> optTxn = paymentRepo.findById(txnId);
            if (optTxn.isEmpty()) {
                log.info(txnId + " is invalid, not exist in db");
                throw new EcommerceException("txn id is not exist in db");
            }

            TransactionStatusEntity txnEntity = optTxn.get();

            if (txnEntity.getStatus().equalsIgnoreCase("SUCCESS") || (txnEntity.getStatusCode() == 1L)) {
                log.info("Txn is already successful");
                throw new EcommerceException("Transaction is already Successful!!");
            }

            if (!txnEntity.getStatus().equalsIgnoreCase("INITIATED") || !(txnEntity.getStatusCode() == 0L)) {
                log.info("error in txn status entity, status must be initiated and status code must be 0");
                throw new EcommerceException("Transaction status must be initiated and status code must be 0");
            }

            Optional<UserCartDetailsEntity> cartOpt = cartRepo.findById(txnEntity.getUsername());

            //validate the otp
            if (otp == null || otp.isEmpty()) {
                log.info("otp can't be null or empty");
                throw new EcommerceException("Otp can't be null or empty in the request");
            }

            if (!otp.trim().equals(txnEntity.getOtp())) {
                log.info(otp + " incorrect otp entered");
                throw new EcommerceException("otp mismatch, please provide correct otp");
            }

            if ((LocalTime.now().getMinute() - Long.parseLong(txnEntity.getOtpCreatedTime()) >= 10) && (LocalTime.now().getMinute() - Long.parseLong(txnEntity.getOtpCreatedTime()) < 0 )){
                log.info(otp + " is expired, otp is valid only for 10 minutes");
                throw new EcommerceException("otp is valid only for 10 minutes, generate a new otp to complete the transaction");
            }

            //complete the transaction

            Optional<CardDetailsEntity> existCard = cardRepo.findById(txnEntity.getCardNumber());

            if (existCard.isEmpty()) {
                log.info("unable to find card details from db");
                throw new EcommerceException("card number not found in db");
            }
            CardDetailsEntity existCardEntity = existCard.get();
            Double limit = existCardEntity.getCardLimit();
            Double usedAmount = existCardEntity.getAmountUsed();
            Double requestAmount = txnEntity.getRequestAmount();
            //processing transaction

            if ((requestAmount + usedAmount) > limit) {
                log.info(txnId + " Maximum amount limit exceed");
                txnEntity.setStatusCode(TxnUtil.FAILED);
                txnEntity.setStatus("FAILED");
                paymentRepo.save(txnEntity);
                throw new EcommerceException("Maximum amount limit exceed");
            }

            //settle the amount
            existCardEntity.setAmountUsed(requestAmount + usedAmount);
            cardRepo.save(existCardEntity);
            log.info(txnId + " transaction successful");
            txnEntity.setStatusCode(TxnUtil.SUCCESS);
            txnEntity.setStatus("SUCCESS");
            txnEntity.setTransactionDate(new Date());
            paymentRepo.save(txnEntity);


            if(cartOpt.isEmpty()){
                log.info("Cart details is not available fro the username");
                throw new EcommerceException("Something went wrong please try again after sometimes");

            }
            UserCartDetailsEntity cartDetailsEntity = cartOpt.get();
            //set transaction event
            TransactionEvent event = mapToTransactionEvent(txnEntity, cartDetailsEntity.getItems());

            executorService.submit(()-> {
                try {
                    pubSubService.publishMessage(event);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            //make the cart empty and
            CartDetails cartDetails = mapToCartDetails(cartDetailsEntity);
            cartDetailsEntity.setAmount(0D);
            cartDetailsEntity.setItems(new ArrayList<>());
            cartRepo.save(cartDetailsEntity);

            TxnResponse response = TxnResponse.builder()
                    .txnId(txnId)
                    .cardNumber(txnEntity.getCardNumber())
                    .cardHolderName(txnEntity.getCardHolderName())
                    .requestAmount(requestAmount)
                    .cardType(CardUtil.findCardType(txnEntity.getCardNumber()))
                    .mobileNo(txnEntity.getMobileNo())
                    .build();


            executorService.submit(()->mailService.sendTxnMail(cartDetails, response , txnEntity));
//            mailService.sendTxnMail(cartDetails, response , txnEntity);
//            executorService.submit(()-> smsService.sendAck(txnEntity));
            log.info("UserCartDetailsEntity @completeTxn: {}",cartOpt.get().getUsername());
            log.info(txnId+" Transaction Successful!");
            return ResponseEntity.ok(new GeneralResponse(response, 1L, "Transaction Successful"));

        } catch (Exception e) {
            log.info("Exception occurred while completing the transaction");
            throw e;
        }
    }

    @Override
    public ResponseEntity<?> addCard(CardRequest card) throws EcommerceException {
        log.info("----------------request received to add card--------");
        log.info("request: {}",card.toString());
        try{
            log.info("validating card details");

            if(isNullOrEmpty(card.getUsername())){
                log.info("username is null");
                throw new EcommerceException("username can't be null or empty");
            }
            UserEntity exists = userRepo.findByUsername(card.getUsername());
            if(exists == null){
                log.info("username not exist in DB");
                throw new EcommerceException("Username not exists, please try with valid username");
            }

            if(isNullOrEmpty(card.getCardNumber())){
                log.info("card number can't be null or empty");
                throw new EcommerceException("card number can't be empty or null");
            }

            Optional<CardDetailsEntity> existCard = cardRepo.findById(card.getCardNumber().trim());

            if(existCard.isPresent()){
                log.info("card is already registered");
                throw new EcommerceException("Duplicate card requested , please try with new card");
            }

            //check the card is expired or not

            if(Long.parseLong(card.getExpiryYear()) < LocalDate.now().getYear() && Long.parseLong(card.getExpiryMonth()) < LocalDate.now().getMonthValue()){
                log.info("card is expired ");
                throw new EcommerceException("Expired Card, please try with a valid card");
            }

            CardDetailsEntity entity = toEntity(card);
            cardRepo.save(entity);
            log.info("----------------card details saved-------------------");
            return ResponseEntity.status(HttpStatus.CREATED).body(new GeneralResponse(new CardResponse(card.getCardNumber(), card.getCardHolderName()),1L, "Card registered successfully"));
        }catch(EcommerceException e){
            log.info("Order exception @addCard due to: {}",e.getMessage());
            throw e;
        }catch (Exception e){
            log.info("Exception @addCard due to: {}",e.getMessage());
            throw new EcommerceException(e.getMessage());
        }
    }
    private Boolean isNullOrEmpty(String o){
        return (o == null || o.trim().isEmpty());
    }
    public CardDetailsEntity toEntity(CardRequest request) {
        if (request == null) {
            return null;
        }
        return CardDetailsEntity.builder()
                .cardNumber(request.getCardNumber())
                .username(request.getUsername())
                .cvv(request.getCvv())
                .expiryMonth(request.getExpiryMonth())
                .expiryYear(request.getExpiryYear())
                .cardHolderName(request.getCardHolderName())
                .bankName(request.getBankName())
                .issuedMonth(request.getIssuedMonth())
                .issuedYear(request.getIssuedYear())
                .pin(request.getPin())
                .isActive(request.getIsActive())
                .cardLimit(request.getCardLimit())
                .amountUsed(request.getAmountUsed())
                .address(request.getAddress())
                .mailId(request.getMailId())
                .mobileNo(request.getMobileNo())
                .build();
    }
    public Long generateId(){

        Date dNow = new Date();
        SimpleDateFormat ft = new SimpleDateFormat("yyyyMMddHHmmssSS");
        String datetime = ft.format(dNow);
        return Long.parseLong(datetime);
    }
    private TransactionEvent mapToTransactionEvent(TransactionStatusEntity status, List<ProductDetailsEntity> items) {
        return TransactionEvent.builder()
                .txnId(status.getTxnId())
                .username(status.getUsername())
                .items(items)
                .amount(status.getRequestAmount())
                .mobileNum(status.getMobileNo())
                .mailId(status.getMailId())
                .orderDate(status.getTransactionDate())
                .address(status.getAddress())
                .status(status.getStatus())
                .statusCode((long) status.getStatusCode())
                .build();
    }
    private CartDetails mapToCartDetails(UserCartDetailsEntity cartDetailsEntity) {
        return CartDetails.builder()
                .username(cartDetailsEntity.getUsername())
                .items(mapToProductDetails(cartDetailsEntity.getItems()))
                .amount(cartDetailsEntity.getAmount())
                .build();
    }
    private List<ProductDetails> mapToProductDetails(List<ProductDetailsEntity> items) {

        ProductDetails productDetails;
        List<ProductDetails> set = new ArrayList<>();
        for(ProductDetailsEntity p : items){
            productDetails = ProductDetails.builder()
                    .productName(p.getProductName())
                    .productType(p.getProductType())
                    .price(p.getPrice())
                    .build();
            set.add(productDetails);
        }
        return set;
    }
}
