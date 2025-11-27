package com.finbasics.service;

import com.finbasics.model.NewApplication;
import com.finbasics.model.StatementAnalysis;

import java.time.LocalDate;

/**
 * Builds a realistic single-period statement analysis snapshot for a new application.
 *
 * This is deliberately deterministic so test runs are repeatable, but all values
 * (DSCR, margins, leverage, liquidity) are computed from the requested amount,
 * product type and borrower type using standard credit assumptions.
 */
public class StatementAnalysisService {

    public StatementAnalysis buildAutoAnalysis(NewApplication app, int applicationId) {
        boolean isSme = "SME".equalsIgnoreCase(app.getBorrowerType());
        double amount = app.getRequestedAmount();

        // Loan economics assumptions
        double nominalRate = switch (app.getProductType()) {
            case "SME Term Loan" -> 0.085;
            case "Line of Credit" -> 0.095;
            case "CRE Mortgage", "Mortgage" -> 0.072;
            case "Equipment Lease" -> 0.089;
            case "Personal Loan" -> 0.112;
            case "Auto Loan" -> 0.079;
            case "Home Equity (HELOC)" -> 0.085;
            default -> 0.09;
        };

        int tenorYears = switch (app.getProductType()) {
            case "Line of Credit", "Personal Loan" -> 3;
            case "Auto Loan", "Equipment Lease", "Home Equity (HELOC)" -> 5;
            default -> 7;
        };

        double annualDebtService = annuityPayment(amount, nominalRate, tenorYears);

        // Target DSCR (healthy range for approvals)
        double targetDscr = isSme ? 1.40 : 1.30;
        double operatingCashFlow = targetDscr * annualDebtService;

        // Revenue assumptions: SMEs typically 3–5x requested amount; consumers ~2x
        double revenueMultiple = isSme ? 4.2 : 2.1;
        double revenue = amount * revenueMultiple;

        double ebitdaMargin = isSme ? 0.22 : 0.18;
        double ebitda = revenue * ebitdaMargin;

        // Net margin after depreciation, interest and tax
        double netMargin = isSme ? 0.075 : 0.06;
        double netIncome = revenue * netMargin;

        // Balance sheet: moderate leverage
        double targetDebtToEquity = isSme ? 1.40 : 1.20;
        double equity = amount / targetDebtToEquity;
        double totalLiabilities = equity * targetDebtToEquity;
        double totalAssets = equity + totalLiabilities;

        // Liquidity: current ratio around 1.4–1.6, quick ratio slightly lower
        double currentRatio = isSme ? 1.5 : 1.4;
        double currentLiabilities = annualDebtService; // simplified: current liabilities mostly debt service
        double currentAssets = currentRatio * currentLiabilities;
        double inventoryPortion = isSme ? 0.30 : 0.20;
        double quickAssets = currentAssets * (1.0 - inventoryPortion);
        double quickRatio = quickAssets / currentLiabilities;

        double cash = quickAssets * 0.55; // portion of quick assets in cash
        double interestExpense = amount * nominalRate;
        double dscr = operatingCashFlow / annualDebtService;

        double roa = netIncome / totalAssets;
        double roe = netIncome / equity;

        StatementAnalysis s = new StatementAnalysis();
        s.setApplicationId(applicationId);

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusYears(1);

        s.setPeriodStart(start.toString());
        s.setPeriodEnd(end.toString());

        s.setRevenue(revenue);
        s.setEbitda(ebitda);
        s.setNetIncome(netIncome);
        s.setTotalAssets(totalAssets);
        s.setTotalLiabilities(totalLiabilities);
        s.setCurrentAssets(currentAssets);
        s.setCurrentLiabilities(currentLiabilities);
        s.setCash(cash);
        s.setInterestExpense(interestExpense);
        s.setDebtService(annualDebtService);

        s.setEbitdaMargin(ebitdaMargin);
        s.setNetMargin(netMargin);
        s.setCurrentRatio(currentRatio);
        s.setQuickRatio(quickRatio);
        s.setDebtToEquity(targetDebtToEquity);
        s.setDscr(dscr);
        s.setRoa(roa);
        s.setRoe(roe);

        return s;
    }

    /**
     * Standard annuity payment formula: level annual payment for principal+interest.
     */
    private double annuityPayment(double principal, double rate, int years) {
        double r = rate; // annual rate
        int n = years;
        return principal * (r / (1 - Math.pow(1 + r, -n)));
    }
}
