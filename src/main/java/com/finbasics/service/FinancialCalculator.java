package com.finbasics.service;

import com.finbasics.model.StatementAnalysis;

/**
 * Centralized engine for financial computations and risk evaluation rules.
 */
public class FinancialCalculator {

    public enum Status { STRONG, ACCEPTABLE, WEAK, CRITICAL, NA }

    public record Evaluation(String label, double value, String formattedValue, Status status, String narrative) {}

    // ==========================================
    // SME (Small/Medium Enterprise) Evaluations
    // ==========================================

    public static Evaluation evaluateSmeDscr(double dscr) {
        Status status;
        String narrative;
        if (dscr >= 1.50) {
            status = Status.STRONG;
            narrative = "Excellent cash flow coverage. High capacity for additional debt.";
        } else if (dscr >= 1.25) {
            status = Status.ACCEPTABLE;
            narrative = "Sufficient cash flow to service debt based on standard underwriting criteria.";
        } else if (dscr >= 1.00) {
            status = Status.WEAK;
            narrative = "Tight cash flow. Business is barely covering existing obligations.";
        } else {
            status = Status.CRITICAL;
            narrative = "Negative cash flow coverage. Cannot afford new debt without restructuring.";
        }
        return new Evaluation("DSCR", dscr, String.format("%.2fx", dscr), status, narrative);
    }

    public static Evaluation evaluateSmeCurrentRatio(double ratio) {
        Status status;
        String narrative;
        if (ratio >= 1.5) {
            status = Status.STRONG;
            narrative = "Strong liquidity position. Short-term assets comfortably cover liabilities.";
        } else if (ratio >= 1.0) {
            status = Status.ACCEPTABLE;
            narrative = "Adequate liquidity. Current assets balance with current liabilities.";
        } else {
            status = Status.WEAK;
            narrative = "Potential liquidity shortfall. May struggle to meet short-term obligations.";
        }
        return new Evaluation("Current Ratio", ratio, String.format("%.2fx", ratio), status, narrative);
    }

    public static Evaluation evaluateSmeQuickRatio(double ratio) {
        Status status = ratio >= 1.0 ? Status.STRONG : Status.ACCEPTABLE;
        return new Evaluation("Quick Ratio", ratio, String.format("%.2fx", ratio), status, "Measure of immediate liquidity (excluding inventory).");
    }

    public static Evaluation evaluateSmeLeverage(double debtToEquity) {
        Status status;
        String narrative;
        if (debtToEquity <= 1.5) {
            status = Status.STRONG;
            narrative = "Low leverage. Business is financed primarily by owner equity.";
        } else if (debtToEquity <= 3.0) {
            status = Status.ACCEPTABLE;
            narrative = "Moderate leverage. Acceptable mix of debt and equity financing.";
        } else {
            status = Status.WEAK;
            narrative = "High leverage. Business is heavily reliant on debt financing.";
        }
        return new Evaluation("Debt-to-Equity", debtToEquity, String.format("%.2fx", debtToEquity), status, narrative);
    }

    public static Evaluation evaluateSmeProfitability(double netMargin) {
        Status status = netMargin > 0.10 ? Status.STRONG : (netMargin > 0 ? Status.ACCEPTABLE : Status.WEAK);
        return new Evaluation("Net Margin", netMargin, String.format("%.1f%%", netMargin * 100), status, "Actual profit remaining after all expenses.");
    }

    // ==========================================
    // CONSUMER Evaluations
    // ==========================================

    public static Evaluation evaluateConsumerDti(double dti) {
        Status status;
        String narrative;
        if (dti <= 0.35) {
            status = Status.STRONG;
            narrative = "Excellent affordability. Debt payments are a low percentage of income.";
        } else if (dti <= 0.43) {
            status = Status.ACCEPTABLE;
            narrative = "Acceptable affordability. Within standard mortgage/loan guidelines.";
        } else {
            status = Status.CRITICAL;
            narrative = "High debt burden. New loan may cause financial distress.";
        }
        double displayVal = Math.min(dti, 1.0);
        return new Evaluation("DTI Ratio", displayVal, String.format("%.1f%%", displayVal * 100), status, narrative);
    }

    public static Evaluation evaluateConsumerCreditScore(int score) {
        Status status;
        String narrative;
        if (score >= 720) {
            status = Status.STRONG;
            narrative = "Prime credit profile. Indicates strong history of repayment.";
        } else if (score >= 640) {
            status = Status.ACCEPTABLE;
            narrative = "Near-prime profile. Acceptable history with some potential minor issues.";
        } else {
            status = Status.WEAK;
            narrative = "Sub-prime profile. Significant credit derogatory marks or limited history.";
        }
        return new Evaluation("Credit Score", score, String.valueOf(score), status, narrative);
    }

    public static Evaluation evaluateConsumerLtv(double ltv) {
         if (ltv <= 0.01) return new Evaluation("LTV", 0, "N/A (Unsecured)", Status.NA, "Unsecured facility.");

        Status status;
        String narrative;
        if (ltv <= 0.70) {
            status = Status.STRONG;
            narrative = "Strong collateral coverage. Significant equity cushion.";
        } else if (ltv <= 0.90) {
            status = Status.ACCEPTABLE;
            narrative = "Adequate collateral coverage. Standard risk level.";
        } else {
            status = Status.WEAK;
            narrative = "Weak collateral coverage. High risk of loss in default event.";
        }
        return new Evaluation("LTV", ltv, String.format("%.0f%%", ltv * 100), status, narrative);
    }

    /**
     * Calculates an overall risk tier based on individual metric evaluations.
     */
    public static String calculateOverallRiskGrade(StatementAnalysis sa) {
        if (sa == null) return "N/A";
        int score = 0;
        int maxScore = 0;

        if ("SME".equalsIgnoreCase(sa.getBorrowerType())) {
            score += statusToScore(evaluateSmeDscr(sa.getDscr()).status);
            score += statusToScore(evaluateSmeCurrentRatio(sa.getCurrentRatio()).status);
            score += statusToScore(evaluateSmeLeverage(sa.getDebtToEquity()).status);
            maxScore = 9; // 3 metrics * 3 max points
        } else {
            score += statusToScore(evaluateConsumerDti(sa.getDti()).status);
            score += statusToScore(evaluateConsumerCreditScore(sa.getCreditScore()).status);
            maxScore = 6;
            if (sa.getLtv() > 0.01) {
                score += statusToScore(evaluateConsumerLtv(sa.getLtv()).status);
                maxScore += 3;
            }
        }

        if (maxScore == 0) return "N/A";
        double percentage = (double) score / maxScore;

        if (percentage >= 0.85) return "Tier 1"; // Low Risk
        if (percentage >= 0.65) return "Tier 2"; // Moderate Risk
        if (percentage >= 0.40) return "Tier 3"; // High Risk
        return "Tier 4"; // Critical Risk
    }

    private static int statusToScore(Status s) {
        return switch (s) {
            case STRONG -> 3;
            case ACCEPTABLE -> 2;
            case WEAK -> 1;
            case CRITICAL -> 0;
            default -> 0;
        };
    }
}