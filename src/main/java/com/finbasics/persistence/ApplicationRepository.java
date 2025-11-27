package com.finbasics.persistence;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.NewApplication;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ApplicationRepository {

    public int createApplication(NewApplication app, int userId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String applicationNumber = nextApplicationNumber(c);
                String now = Instant.now().toString();

                // 1) applications
                int appId;
                try (PreparedStatement ps = c.prepareStatement(
                        """
                        INSERT INTO applications(
                            application_number,
                            borrower_type,
                            borrower_name,
                            borrower_id_number,
                            product_type,
                            requested_amount,
                            status,
                            sla_hours,
                            created_by,
                            created_at,
                            updated_at
                        ) VALUES(?,?,?,?,?,?, 'ANALYSIS_PENDING', 72, ?, ?, ?)
                        """,
                        Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, app.getBorrowerType());
                    ps.setString(2, app.getBorrowerType());
                    ps.setString(3, app.getBorrowerName());
                    ps.setString(4, app.getBorrowerIdNumber());
                    ps.setString(5, app.getProductType());
                    ps.setDouble(6, app.getRequestedAmount());
                    ps.setInt(7, userId);
                    ps.setString(8, now);
                    ps.setString(9, now);

                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) {
                            throw new SQLException("No key returned for applications insert");
                        }
                        appId = keys.getInt(1);
                    }
                }

                // 2) application_details
                try (PreparedStatement ps = c.prepareStatement(
                        """
                        INSERT INTO application_details(
                            application_id,
                            business_name,
                            ein,
                            naics_code,
                            date_established,
                            guarantor_name,
                            consumer_name,
                            ssn,
                            employer,
                            annual_income
                        ) VALUES(?,?,?,?,?,?,?,?,?,?)
                        """)) {

                    ps.setInt(1, appId);
                    ps.setString(2, app.getBusinessName());
                    ps.setString(3, app.getEin());
                    ps.setString(4, app.getNaicsCode());
                    ps.setString(5, app.getDateEstablishedIso());
                    ps.setString(6, app.getGuarantorName());
                    ps.setString(7, app.getConsumerName());
                    ps.setString(8, app.getSsn());
                    ps.setString(9, app.getEmployer());
                    if (app.getAnnualIncome() != null) {
                        ps.setDouble(10, app.getAnnualIncome());
                    } else {
                        ps.setNull(10, Types.REAL);
                    }

                    ps.executeUpdate();
                }

                c.commit();
                return appId;
            } catch (Exception e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private String nextApplicationNumber(Connection c) throws SQLException {
        // APP-YYYY-#### based on next id
        int year = LocalDate.now().getYear();
        int nextId;
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id),0) + 1 FROM applications")) {
            rs.next();
            nextId = rs.getInt(1);
        }
        return "APP-" + year + "-" + String.format("%04d", nextId);
    }

    public List<ApplicationSummary> findAllSummaries() throws SQLException {
        String sql = """
            SELECT
                a.id,
                a.application_number,
                a.borrower_name,
                a.product_type,
                a.requested_amount,
                a.status,
                a.created_at,
                sa.dscr,
                sa.current_ratio,
                sa.debt_to_equity,
                sa.net_margin
            FROM applications a
            LEFT JOIN statement_analysis sa ON sa.application_id = a.id
            ORDER BY a.created_at DESC
            """;

        List<ApplicationSummary> result = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ApplicationSummary s = new ApplicationSummary();
                s.setId(rs.getInt("id"));
                s.setApplicationNumber(rs.getString("application_number"));
                s.setBorrowerName(rs.getString("borrower_name"));
                s.setProductType(rs.getString("product_type"));
                s.setRequestedAmount(rs.getDouble("requested_amount"));
                s.setStatus(rs.getString("status"));
                s.setCreatedAt(rs.getString("created_at"));
                s.setDscr(rs.getDouble("dscr"));
                s.setCurrentRatio(rs.getDouble("current_ratio"));
                s.setDebtToEquity(rs.getDouble("debt_to_equity"));
                s.setNetMargin(rs.getDouble("net_margin"));

                result.add(s);
            }
        }
        return result;
    }
}
