package com.finbasics.service;

import com.finbasics.model.StatementAnalysis;

/**
 * Centralized logic for financial ratios and risk scoring.
 */
public class FinancialCalculator {

    public enum Status { STRONG, ACCEPTABLE, WEAK, CRITICAL, NA }
    public record Evaluation(String label, double value, String formatted, Status status, String note) {}

    // -------------------------------------------
    // SME Ratios (Liquidity, Leverage, Efficiency)
    // -------------------------------------------

    public static Evaluation evalDscr(double val) {
        Status s = val >= 1.25 ? Status.STRONG : (val >= 1.10 ? Status.ACCEPTABLE : Status.CRITICAL);
        String n = s == Status.STRONG ? "Cash flow comfortably covers debt." : "Insufficient cash flow to service debt.";
        return new Evaluation("DSCR", val, String.format("%.2fx", val), s, n);
    }

    public static Evaluation evalCurrentRatio(double val) {
        Status s = val >= 1.2 ? Status.STRONG : (val >= 1.0 ? Status.ACCEPTABLE : Status.WEAK);
        return new Evaluation("Current Ratio", val, String.format("%.2fx", val), s, "Measure of short-term liquidity.");
    }

    public static Evaluation evalDebtEquity(double val) {
        Status s = val <= 2.5 ? Status.STRONG : (val <= 4.0 ? Status.ACCEPTABLE : Status.WEAK);
        return new Evaluation("Debt/Equity", val, String.format("%.2fx", val), s, "Measure of financial leverage.");
    }

    public static Evaluation evalNetMargin(double val) {
        Status s = val >= 0.10 ? Status.STRONG : (val >= 0.05 ? Status.ACCEPTABLE : Status.WEAK);
        return new Evaluation("Net Margin", val, String.format("%.1f%%", val*100), s, "Profitability after all expenses.");
    }

    public static Evaluation evalQuickRatio(double val) {
        Status s = val >= 1.0 ? Status.STRONG : Status.ACCEPTABLE;
        return new Evaluation("Quick Ratio", val, String.format("%.2fx", val), s, "(Cash + AR) / Current Liab.");
    }

    public static Evaluation evalROE(double val) {
        Status s = val >= 0.15 ? Status.STRONG : Status.ACCEPTABLE;
        return new Evaluation("ROE", val, String.format("%.1f%%", val*100), s, "Return on Equity.");
    }

    // -------------------------------------------
    // Consumer Ratios (DTI, LTV, Credit)
    // -------------------------------------------

    public static Evaluation evalDti(double val) {
        Status s = val <= 0.36 ? Status.STRONG : (val <= 0.43 ? Status.ACCEPTABLE : Status.CRITICAL);
        return new Evaluation("DTI Ratio", val, String.format("%.1f%%", val*100), s, "Debt-to-Income (Affordability).");
    }

    public static Evaluation evalCreditScore(int val) {
        Status s = val >= 720 ? Status.STRONG : (val >= 640 ? Status.ACCEPTABLE : Status.CRITICAL);
        return new Evaluation("Credit Score", val, String.valueOf(val), s, "FICO-based creditworthiness.");
    }

    public static Evaluation evalLtv(double val) {
        if (val < 0.01) return new Evaluation("LTV", 0, "N/A", Status.NA, "Unsecured loan.");
        Status s = val <= 0.80 ? Status.STRONG : (val <= 0.95 ? Status.ACCEPTABLE : Status.WEAK);
        return new Evaluation("LTV", val, String.format("%.0f%%", val*100), s, "Loan-to-Value collateral coverage.");
    }

    // -------------------------------------------
    // Risk Scorecard Logic
    // -------------------------------------------

    public static String calculateRiskGrade(StatementAnalysis sa) {
        int points = 0;
        int maxPoints = 0;

        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            points += score(evalDscr(sa.getDscr()).status);
            points += score(evalCurrentRatio(sa.getCurrentRatio()).status);
            points += score(evalDebtEquity(sa.getDebtToEquity()).status);
            maxPoints = 9;
        } else {
            points += score(evalDti(sa.getDti()).status);
            points += score(evalCreditScore(sa.getCreditScore()).status);
            maxPoints = 6;
            if (sa.getLtv() > 0.01) {
                points += score(evalLtv(sa.getLtv()).status);
                maxPoints += 3;
            }
        }

        double scorePct = (double) points / maxPoints;
        if (scorePct >= 0.8) return "Tier 1 (Low Risk)";
        if (scorePct >= 0.5) return "Tier 2 (Moderate)";
        if (scorePct >= 0.3) return "Tier 3 (High Risk)";
        return "Tier 4 (Critical)";
    }

    private static int score(Status s) {
        return switch (s) {
            case STRONG -> 3;
            case ACCEPTABLE -> 2;
            case WEAK -> 1;
            case CRITICAL -> 0;
            default -> 0;
        };
    }
}