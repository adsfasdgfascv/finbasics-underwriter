package com.finbasics.persistence;

import com.finbasics.model.StatementAnalysis;

import java.sql.*;
import java.time.Instant;

public class StatementAnalysisRepository {

    public void insert(StatementAnalysis a) throws SQLException {
        String sql = """
            INSERT INTO statement_analysis(
                application_id,
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
                created_at
            ) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
            """;

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, a.getApplicationId());
            ps.setString(2, a.getPeriodStart());
            ps.setString(3, a.getPeriodEnd());
            ps.setDouble(4, a.getRevenue());
            ps.setDouble(5, a.getEbitda());
            ps.setDouble(6, a.getNetIncome());
            ps.setDouble(7, a.getTotalAssets());
            ps.setDouble(8, a.getTotalLiabilities());
            ps.setDouble(9, a.getCurrentAssets());
            ps.setDouble(10, a.getCurrentLiabilities());
            ps.setDouble(11, a.getCash());
            ps.setDouble(12, a.getInterestExpense());
            ps.setDouble(13, a.getDebtService());
            ps.setDouble(14, a.getEbitdaMargin());
            ps.setDouble(15, a.getNetMargin());
            ps.setDouble(16, a.getCurrentRatio());
            ps.setDouble(17, a.getQuickRatio());
            ps.setDouble(18, a.getDebtToEquity());
            ps.setDouble(19, a.getDscr());
            ps.setDouble(20, a.getRoa());
            ps.setDouble(21, a.getRoe());
            ps.setString(22, a.getCreatedAt() != null ? a.getCreatedAt() : Instant.now().toString());

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

                StatementAnalysis a = new StatementAnalysis();
                a.setApplicationId(rs.getInt("application_id"));
                a.setPeriodStart(rs.getString("period_start"));
                a.setPeriodEnd(rs.getString("period_end"));
                a.setRevenue(rs.getDouble("revenue"));
                a.setEbitda(rs.getDouble("ebitda"));
                a.setNetIncome(rs.getDouble("net_income"));
                a.setTotalAssets(rs.getDouble("total_assets"));
                a.setTotalLiabilities(rs.getDouble("total_liabilities"));
                a.setCurrentAssets(rs.getDouble("current_assets"));
                a.setCurrentLiabilities(rs.getDouble("current_liabilities"));
                a.setCash(rs.getDouble("cash"));
                a.setInterestExpense(rs.getDouble("interest_expense"));
                a.setDebtService(rs.getDouble("debt_service"));
                a.setEbitdaMargin(rs.getDouble("ebitda_margin"));
                a.setNetMargin(rs.getDouble("net_margin"));
                a.setCurrentRatio(rs.getDouble("current_ratio"));
                a.setQuickRatio(rs.getDouble("quick_ratio"));
                a.setDebtToEquity(rs.getDouble("debt_to_equity"));
                a.setDscr(rs.getDouble("dscr"));
                a.setRoa(rs.getDouble("roa"));
                a.setRoe(rs.getDouble("roe"));
                a.setCreatedAt(rs.getString("created_at"));
                return a;
            }
        }
    }
}
