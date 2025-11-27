package com.finbasics.persistence;

import com.finbasics.model.StatementAnalysis;

import java.sql.*;

/**
 * Access to the statement_analysis table.
 */
public class StatementAnalysisRepository {

    public void insert(StatementAnalysis s) throws SQLException {
        String sql = """
            INSERT INTO statement_analysis(
                application_id,
                borrower_type,
                period_start,
                period_end,
                revenue,
                ebitda,
                net_income,
                total_assets,
                total_liabilities,
                current_assets,
                current_liabilities,
                cash,
                interest_expense,
                debt_service,
                ebitda_margin,
                net_margin,
                current_ratio,
                quick_ratio,
                debt_to_equity,
                dscr,
                roa,
                roe,
                dso,
                inventory_turnover,
                asset_turnover,
                monthly_income,
                monthly_debt_payments,
                dti,
                ltv,
                credit_score,
                created_at
            ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, s.getApplicationId());
            ps.setString(2, s.getBorrowerType());
            ps.setString(3, s.getPeriodStart());
            ps.setString(4, s.getPeriodEnd());
            ps.setDouble(5, s.getRevenue());
            ps.setDouble(6, s.getEbitda());
            ps.setDouble(7, s.getNetIncome());
            ps.setDouble(8, s.getTotalAssets());
            ps.setDouble(9, s.getTotalLiabilities());
            ps.setDouble(10, s.getCurrentAssets());
            ps.setDouble(11, s.getCurrentLiabilities());
            ps.setDouble(12, s.getCash());
            ps.setDouble(13, s.getInterestExpense());
            ps.setDouble(14, s.getDebtService());
            ps.setDouble(15, s.getEbitdaMargin());
            ps.setDouble(16, s.getNetMargin());
            ps.setDouble(17, s.getCurrentRatio());
            ps.setDouble(18, s.getQuickRatio());
            ps.setDouble(19, s.getDebtToEquity());
            ps.setDouble(20, s.getDscr());
            ps.setDouble(21, s.getRoa());
            ps.setDouble(22, s.getRoe());
            ps.setDouble(23, s.getDso());
            ps.setDouble(24, s.getInventoryTurnover());
            ps.setDouble(25, s.getAssetTurnover());
            ps.setDouble(26, s.getMonthlyIncome());
            ps.setDouble(27, s.getMonthlyDebtPayments());
            ps.setDouble(28, s.getDti());
            ps.setDouble(29, s.getLtv());
            ps.setInt(30, s.getCreditScore());
            ps.setString(31, s.getCreatedAt());

            ps.executeUpdate();
        }
    }

    public StatementAnalysis findByApplicationId(int appId) throws SQLException {
        String sql = "SELECT * FROM statement_analysis WHERE application_id = ?";

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, appId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;

                StatementAnalysis s = new StatementAnalysis();
                s.setApplicationId(rs.getInt("application_id"));
                s.setBorrowerType(rs.getString("borrower_type"));
                s.setPeriodStart(rs.getString("period_start"));
                s.setPeriodEnd(rs.getString("period_end"));
                s.setRevenue(rs.getDouble("revenue"));
                s.setEbitda(rs.getDouble("ebitda"));
                s.setNetIncome(rs.getDouble("net_income"));
                s.setTotalAssets(rs.getDouble("total_assets"));
                s.setTotalLiabilities(rs.getDouble("total_liabilities"));
                s.setCurrentAssets(rs.getDouble("current_assets"));
                s.setCurrentLiabilities(rs.getDouble("current_liabilities"));
                s.setCash(rs.getDouble("cash"));
                s.setInterestExpense(rs.getDouble("interest_expense"));
                s.setDebtService(rs.getDouble("debt_service"));
                s.setEbitdaMargin(rs.getDouble("ebitda_margin"));
                s.setNetMargin(rs.getDouble("net_margin"));
                s.setCurrentRatio(rs.getDouble("current_ratio"));
                s.setQuickRatio(rs.getDouble("quick_ratio"));
                s.setDebtToEquity(rs.getDouble("debt_to_equity"));
                s.setDscr(rs.getDouble("dscr"));
                s.setRoa(rs.getDouble("roa"));
                s.setRoe(rs.getDouble("roe"));
                s.setDso(rs.getDouble("dso"));
                s.setInventoryTurnover(rs.getDouble("inventory_turnover"));
                s.setAssetTurnover(rs.getDouble("asset_turnover"));
                s.setMonthlyIncome(rs.getDouble("monthly_income"));
                s.setMonthlyDebtPayments(rs.getDouble("monthly_debt_payments"));
                s.setDti(rs.getDouble("dti"));
                s.setLtv(rs.getDouble("ltv"));
                s.setCreditScore(rs.getInt("credit_score"));
                s.setCreatedAt(rs.getString("created_at"));

                return s;
            }
        }
    }
}
