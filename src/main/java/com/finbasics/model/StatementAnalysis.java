package com.finbasics.model;

public class StatementAnalysis {

    private int applicationId;

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

    private String createdAt;

    // Getters / setters

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    public String getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(String periodStart) {
        this.periodStart = periodStart;
    }

    public String getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(String periodEnd) {
        this.periodEnd = periodEnd;
    }

    public double getRevenue() {
        return revenue;
    }

    public void setRevenue(double revenue) {
        this.revenue = revenue;
    }

    public double getEbitda() {
        return ebitda;
    }

    public void setEbitda(double ebitda) {
        this.ebitda = ebitda;
    }

    public double getNetIncome() {
        return netIncome;
    }

    public void setNetIncome(double netIncome) {
        this.netIncome = netIncome;
    }

    public double getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(double totalAssets) {
        this.totalAssets = totalAssets;
    }

    public double getTotalLiabilities() {
        return totalLiabilities;
    }

    public void setTotalLiabilities(double totalLiabilities) {
        this.totalLiabilities = totalLiabilities;
    }

    public double getCurrentAssets() {
        return currentAssets;
    }

    public void setCurrentAssets(double currentAssets) {
        this.currentAssets = currentAssets;
    }

    public double getCurrentLiabilities() {
        return currentLiabilities;
    }

    public void setCurrentLiabilities(double currentLiabilities) {
        this.currentLiabilities = currentLiabilities;
    }

    public double getCash() {
        return cash;
    }

    public void setCash(double cash) {
        this.cash = cash;
    }

    public double getInterestExpense() {
        return interestExpense;
    }

    public void setInterestExpense(double interestExpense) {
        this.interestExpense = interestExpense;
    }

    public double getDebtService() {
        return debtService;
    }

    public void setDebtService(double debtService) {
        this.debtService = debtService;
    }

    public double getEbitdaMargin() {
        return ebitdaMargin;
    }

    public void setEbitdaMargin(double ebitdaMargin) {
        this.ebitdaMargin = ebitdaMargin;
    }

    public double getNetMargin() {
        return netMargin;
    }

    public void setNetMargin(double netMargin) {
        this.netMargin = netMargin;
    }

    public double getCurrentRatio() {
        return currentRatio;
    }

    public void setCurrentRatio(double currentRatio) {
        this.currentRatio = currentRatio;
    }

    public double getQuickRatio() {
        return quickRatio;
    }

    public void setQuickRatio(double quickRatio) {
        this.quickRatio = quickRatio;
    }

    public double getDebtToEquity() {
        return debtToEquity;
    }

    public void setDebtToEquity(double debtToEquity) {
        this.debtToEquity = debtToEquity;
    }

    public double getDscr() {
        return dscr;
    }

    public void setDscr(double dscr) {
        this.dscr = dscr;
    }

    public double getRoa() {
        return roa;
    }

    public void setRoa(double roa) {
        this.roa = roa;
    }

    public double getRoe() {
        return roe;
    }

    public void setRoe(double roe) {
        this.roe = roe;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
}
