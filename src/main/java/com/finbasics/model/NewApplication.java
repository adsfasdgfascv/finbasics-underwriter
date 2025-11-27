package com.finbasics.model;

/**
 * DTO used by the submission wizard to carry all data needed
 * to create a new application and run automatic analysis.
 */
public class NewApplication {

    private String borrowerType;      // "SME" or "CONSUMER"
    private String borrowerName;
    private String borrowerIdNumber;
    private String productType;
    private double requestedAmount;

    // SME fields
    private String businessName;
    private String ein;
    private String naicsCode;
    private String dateEstablishedIso;
    private String guarantorName;

    // Consumer fields
    private String consumerName;
    private String ssn;
    private String employer;
    private Double annualIncome;

    // --- Getters / Setters ---

    public String getBorrowerType() { return borrowerType; }
    public void setBorrowerType(String borrowerType) { this.borrowerType = borrowerType; }

    public String getBorrowerName() { return borrowerName; }
    public void setBorrowerName(String borrowerName) { this.borrowerName = borrowerName; }

    public String getBorrowerIdNumber() { return borrowerIdNumber; }
    public void setBorrowerIdNumber(String borrowerIdNumber) { this.borrowerIdNumber = borrowerIdNumber; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public double getRequestedAmount() { return requestedAmount; }
    public void setRequestedAmount(double requestedAmount) { this.requestedAmount = requestedAmount; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getEin() { return ein; }
    public void setEin(String ein) { this.ein = ein; }

    public String getNaicsCode() { return naicsCode; }
    public void setNaicsCode(String naicsCode) { this.naicsCode = naicsCode; }

    public String getDateEstablishedIso() { return dateEstablishedIso; }
    public void setDateEstablishedIso(String dateEstablishedIso) { this.dateEstablishedIso = dateEstablishedIso; }

    public String getGuarantorName() { return guarantorName; }
    public void setGuarantorName(String guarantorName) { this.guarantorName = guarantorName; }

    public String getConsumerName() { return consumerName; }
    public void setConsumerName(String consumerName) { this.consumerName = consumerName; }

    public String getSsn() { return ssn; }
    public void setSsn(String ssn) { this.ssn = ssn; }

    public String getEmployer() { return employer; }
    public void setEmployer(String employer) { this.employer = employer; }

    public Double getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(Double annualIncome) { this.annualIncome = annualIncome; }
}
