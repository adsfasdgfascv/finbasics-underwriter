package com.finbasics.model;

/**
 * Represents a single-period statement analysis snapshot for an application.
 * Supports both SME (corporate) and CONSUMER (personal) borrower types.
 */
public class StatementAnalysis {

    private int applicationId;
    private String borrowerType;

    private String periodStart;
    private String periodEnd;

    private double revenue;
    private double ebitda;
    private double netIncome;
    private double totalAssets;
    private double totalLiabilities;
    private double currentAssets;
    private double currentLiabilities;
    private double cash;
    private double interestExpense;
    private double debtService;

    private double ebitdaMargin;
    private double netMargin;
    private double currentRatio;
    private double quickRatio;
    private double debtToEquity;
    private double dscr;
    private double roa;
    private double roe;

    private double dso;
    private double inventoryTurnover;
    private double assetTurnover;

    private double monthlyIncome;
    private double monthlyDebtPayments;
    private double dti;
    private double ltv;
    private int creditScore;

    private String createdAt;

    // Getters / setters

    public int getApplicationId() { return applicationId; }
    public void setApplicationId(int applicationId) { this.applicationId = applicationId; }

    public String getBorrowerType() { return borrowerType; }
    public void setBorrowerType(String borrowerType) { this.borrowerType = borrowerType; }

    public String getPeriodStart() { return periodStart; }
    public void setPeriodStart(String periodStart) { this.periodStart = periodStart; }

    public String getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(String periodEnd) { this.periodEnd = periodEnd; }

    public double getRevenue() { return revenue; }
    public void setRevenue(double revenue) { this.revenue = revenue; }

    public double getEbitda() { return ebitda; }
    public void setEbitda(double ebitda) { this.ebitda = ebitda; }

    public double getNetIncome() { return netIncome; }
    public void setNetIncome(double netIncome) { this.netIncome = netIncome; }

    public double getTotalAssets() { return totalAssets; }
    public void setTotalAssets(double totalAssets) { this.totalAssets = totalAssets; }

    public double getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(double totalLiabilities) { this.totalLiabilities = totalLiabilities; }

    public double getCurrentAssets() { return currentAssets; }
    public void setCurrentAssets(double currentAssets) { this.currentAssets = currentAssets; }

    public double getCurrentLiabilities() { return currentLiabilities; }
    public void setCurrentLiabilities(double currentLiabilities) { this.currentLiabilities = currentLiabilities; }

    public double getCash() { return cash; }
    public void setCash(double cash) { this.cash = cash; }

    public double getInterestExpense() { return interestExpense; }
    public void setInterestExpense(double interestExpense) { this.interestExpense = interestExpense; }

    public double getDebtService() { return debtService; }
    public void setDebtService(double debtService) { this.debtService = debtService; }

    public double getEbitdaMargin() { return ebitdaMargin; }
    public void setEbitdaMargin(double ebitdaMargin) { this.ebitdaMargin = ebitdaMargin; }

    public double getNetMargin() { return netMargin; }
    public void setNetMargin(double netMargin) { this.netMargin = netMargin; }

    public double getCurrentRatio() { return currentRatio; }
    public void setCurrentRatio(double currentRatio) { this.currentRatio = currentRatio; }

    public double getQuickRatio() { return quickRatio; }
    public void setQuickRatio(double quickRatio) { this.quickRatio = quickRatio; }

    public double getDebtToEquity() { return debtToEquity; }
    public void setDebtToEquity(double debtToEquity) { this.debtToEquity = debtToEquity; }

    public double getDscr() { return dscr; }
    public void setDscr(double dscr) { this.dscr = dscr; }

    public double getRoa() { return roa; }
    public void setRoa(double roa) { this.roa = roa; }

    public double getRoe() { return roe; }
    public void setRoe(double roe) { this.roe = roe; }

    public double getDso() { return dso; }
    public void setDso(double dso) { this.dso = dso; }

    public double getInventoryTurnover() { return inventoryTurnover; }
    public void setInventoryTurnover(double inventoryTurnover) { this.inventoryTurnover = inventoryTurnover; }

    public double getAssetTurnover() { return assetTurnover; }
    public void setAssetTurnover(double assetTurnover) { this.assetTurnover = assetTurnover; }

    public double getMonthlyIncome() { return monthlyIncome; }
    public void setMonthlyIncome(double monthlyIncome) { this.monthlyIncome = monthlyIncome; }

    public double getMonthlyDebtPayments() { return monthlyDebtPayments; }
    public void setMonthlyDebtPayments(double monthlyDebtPayments) { this.monthlyDebtPayments = monthlyDebtPayments; }

    public double getDti() { return dti; }
    public void setDti(double dti) { this.dti = dti; }

    public double getLtv() { return ltv; }
    public void setLtv(double ltv) { this.ltv = ltv; }

    public int getCreditScore() { return creditScore; }
    public void setCreditScore(int creditScore) { this.creditScore = creditScore; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
