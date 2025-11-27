package com.finbasics.service;

import com.finbasics.model.NewApplication;
import com.finbasics.model.StatementAnalysis;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Builds realistic SME vs Consumer financial analysis snapshots
 * based on project requirements document.
 */
public class StatementAnalysisService {

    public StatementAnalysis buildAutoAnalysis(NewApplication app, int applicationId) {
        boolean isSme = "SME".equalsIgnoreCase(app.getBorrowerType());
        return isSme
                ? buildSmeAnalysis(app, applicationId)
                : buildConsumerAnalysis(app, applicationId);
    }

    // ---------------- SME FLOW ----------------

    private StatementAnalysis buildSmeAnalysis(NewApplication app, int appId) {
        double amount = app.getRequestedAmount();

        double revenue = amount * 4.5;          // loan ~ 22% of sales
        double ebitdaMargin = 0.22;
        double ebitda = revenue * ebitdaMargin;
        double netMargin = 0.08;
        double netIncome = revenue * netMargin;

        double debtToEquity = 1.5;
        double equity = amount / debtToEquity;
        double totalLiabilities = equity * debtToEquity;
        double totalAssets = equity + totalLiabilities;

        double currentRatio = 1.4;
        double currentLiabilities = revenue * 0.18 / 12.0;
        double currentAssets = currentRatio * currentLiabilities;
        double inventoryShare = 0.35;
        double quickAssets = currentAssets * (1 - inventoryShare);
        double quickRatio = quickAssets / currentLiabilities;
        double cash = quickAssets * 0.5;

        // SPELL – typical assumptions
        double dso = 45.0;
        double inventoryTurnover = 365.0 / 60.0; // ≈ 6x
        double assetTurnover = revenue / totalAssets;

        double rate = 0.085;
        int years = 7;
        double annualDebtService = annuityAnnual(amount, rate, years);
        double operatingCashFlow = ebitda * 0.9;
        double dscr = operatingCashFlow / annualDebtService;

        double roa = netIncome / totalAssets;
        double roe = netIncome / equity;

        StatementAnalysis s = skeleton(appId, "SME");

        s.setRevenue(revenue);
        s.setEbitda(ebitda);
        s.setNetIncome(netIncome);
        s.setTotalAssets(totalAssets);
        s.setTotalLiabilities(totalLiabilities);
        s.setCurrentAssets(currentAssets);
        s.setCurrentLiabilities(currentLiabilities);
        s.setCash(cash);
        s.setInterestExpense(amount * rate);
        s.setDebtService(annualDebtService);

        s.setEbitdaMargin(ebitdaMargin);
        s.setNetMargin(netMargin);
        s.setCurrentRatio(currentRatio);
        s.setQuickRatio(quickRatio);
        s.setDebtToEquity(debtToEquity);
        s.setDscr(dscr);
        s.setRoa(roa);
        s.setRoe(roe);

        s.setDso(dso);
        s.setInventoryTurnover(inventoryTurnover);
        s.setAssetTurnover(assetTurnover);

        return s;
    }

    // ---------------- CONSUMER FLOW ----------------

    private StatementAnalysis buildConsumerAnalysis(NewApplication app, int appId) {
        double amount = app.getRequestedAmount();

        double annualIncome = (app.getAnnualIncome() != null && app.getAnnualIncome() > 0)
                ? app.getAnnualIncome()
                : amount * 1.8;
        double monthlyIncome = annualIncome / 12.0;

        double existingMonthlyDebt = monthlyIncome * 0.18;

        double rate;
        int years;
        String product = app.getProductType();
        if ("Auto Loan".equalsIgnoreCase(product)) {
            rate = 0.079; years = 5;
        } else if ("Home Equity (HELOC)".equalsIgnoreCase(product) || "Mortgage".equalsIgnoreCase(product)) {
            rate = 0.065; years = 25;
        } else {
            rate = 0.115; years = 3;
        }

        double monthlyPayment = annuityMonthly(amount, rate / 12.0, years * 12);
        double totalMonthlyDebt = existingMonthlyDebt + monthlyPayment;
        double dti = totalMonthlyDebt / monthlyIncome;

        double ltv;
        if ("Auto Loan".equalsIgnoreCase(product)) {
            double vehicleValue = amount / 0.9;
            ltv = amount / vehicleValue;
        } else if ("Home Equity (HELOC)".equalsIgnoreCase(product)
                || "Mortgage".equalsIgnoreCase(product)) {
            double propertyValue = amount / 0.8;
            ltv = amount / propertyValue;
        } else {
            ltv = 0.0;
        }

        double livingExpenses = monthlyIncome * 0.45;
        double freeCashFlow = monthlyIncome - livingExpenses - existingMonthlyDebt;
        double dscrLike = freeCashFlow / monthlyPayment;

        int creditScore = creditScoreFromBands(dti, ltv);

        StatementAnalysis s = skeleton(appId, "CONSUMER");

        s.setRevenue(annualIncome);
        s.setEbitda(annualIncome * 0.25);
        s.setNetIncome(annualIncome * 0.18);
        double totalAssets = amount / Math.max(ltv, 0.5);
        s.setTotalAssets(totalAssets);
        s.setTotalLiabilities(amount);
        s.setCurrentAssets(livingExpenses * 3);
        s.setCurrentLiabilities(totalMonthlyDebt * 12);
        s.setCash(livingExpenses);
        s.setInterestExpense(amount * rate);
        s.setDebtService(monthlyPayment * 12);

        s.setEbitdaMargin(s.getEbitda() / s.getRevenue());
        s.setNetMargin(s.getNetIncome() / s.getRevenue());
        s.setCurrentRatio(s.getCurrentAssets() / s.getCurrentLiabilities());
        s.setQuickRatio(s.getCurrentRatio());
        double equity = totalAssets - amount;
        if (equity <= 0) equity = 1;
        s.setDebtToEquity(amount / equity);
        s.setDscr(dscrLike);
        s.setRoa(s.getNetIncome() / totalAssets);
        s.setRoe(s.getNetIncome() / equity);

        s.setMonthlyIncome(monthlyIncome);
        s.setMonthlyDebtPayments(totalMonthlyDebt);
        s.setDti(dti);
        s.setLtv(ltv);
        s.setCreditScore(creditScore);

        return s;
    }

    private StatementAnalysis skeleton(int appId, String borrowerType) {
        StatementAnalysis s = new StatementAnalysis();
        s.setApplicationId(appId);
        s.setBorrowerType(borrowerType);
        LocalDate end = LocalDate.now();
        s.setPeriodEnd(end.toString());
        s.setPeriodStart(end.minusYears(1).toString());
        s.setCreatedAt(Instant.now().toString());
        return s;
    }

    private double annuityAnnual(double principal, double rate, int years) {
        int n = years;
        double r = rate;
        return principal * (r / (1 - Math.pow(1 + r, -n)));
    }

    private double annuityMonthly(double principal, double monthlyRate, int periods) {
        int n = periods;
        double r = monthlyRate;
        return principal * (r / (1 - Math.pow(1 + r, -n)));
    }

    private int creditScoreFromBands(double dti, double ltv) {
        if (dti <= 0.32 && (ltv == 0.0 || ltv <= 0.80)) return 740;
        if (dti <= 0.40 && (ltv == 0.0 || ltv <= 0.90)) return 690;
        if (dti <= 0.45 && (ltv == 0.0 || ltv <= 0.95)) return 650;
        return 600;
    }
}
