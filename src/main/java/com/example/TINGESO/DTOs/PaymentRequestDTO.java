package com.example.TINGESO.DTOs;

/**
 * Data Transfer Object for receiving simulated payment gateway information.
 * As per Epic 5 rules, we receive these fields but NEVER store them 
 * in our persistence layer to avoid PCI compliance issues.
 */
public class PaymentRequestDTO {
    
    private String cardHolderName;
    private String cardNumber;
    private String expiryDate;
    private String cvv;
    private String paymentMethod; // e.g. "CREDIT" or "DEBIT"

    public String getCardHolderName() {
        return cardHolderName;
    }

    public void setCardHolderName(String cardHolderName) {
        this.cardHolderName = cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
}
