package com.finbasics.persistence;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.NewApplication;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * CRUD operations for applications + simple read models for dashboard and detail.
 */
public class ApplicationRepository {

    public int createApplication(NewApplication app, int userId) throws SQLException {
        try (Connection c = Database.getConnection()) {
            c.setAutoCommit(false);
            try {
                String now = Instant.now().toString();
                String appNumber = nextApplicationNumber(c);

                int appId;
                try (PreparedStatement ps = c.prepareStatement("""
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
                        """, Statement.RETURN_GENERATED_KEYS)) {

                    ps.setString(1, appNumber);
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
                        if (!keys.next()) throw new SQLException("No generated key for applications");
                        appId = keys.getInt(1);
                    }
                }

                try (PreparedStatement ps = c.prepareStatement("""
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
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    private String nextApplicationNumber(Connection c) throws SQLException {
        int year = LocalDate.now().getYear();
        int nextId;
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(id), 0) + 1 FROM applications")) {
            rs.next();
            nextId = rs.getInt(1);
        }
        return "APP-" + year + "-" + String.format("%04d", nextId);
    }

    public List<ApplicationSummary> findAllSummaries() throws SQLException {
        String sql = """
            SELECT id,
                   application_number,
                   borrower_type,
                   borrower_name,
                   product_type,
                   requested_amount,
                   status,
                   created_at
            FROM applications
            ORDER BY created_at DESC
            """;

        List<ApplicationSummary> list = new ArrayList<>();
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ApplicationSummary s = new ApplicationSummary();
                s.setId(rs.getInt("id"));
                s.setApplicationNumber(rs.getString("application_number"));
                s.setBorrowerType(rs.getString("borrower_type"));
                s.setBorrowerName(rs.getString("borrower_name"));
                s.setProductType(rs.getString("product_type"));
                s.setRequestedAmount(rs.getDouble("requested_amount"));
                s.setStatus(rs.getString("status"));
                s.setCreatedAt(rs.getString("created_at"));
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Lightweight header for ApplicantDetail screen.
     */
    public ApplicationSummary findHeader(int id) throws SQLException {
        String sql = """
            SELECT id,
                   application_number,
                   borrower_type,
                   borrower_name,
                   product_type,
                   requested_amount,
                   status,
                   created_at
            FROM applications
            WHERE id = ?
            """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                ApplicationSummary s = new ApplicationSummary();
                s.setId(rs.getInt("id"));
                s.setApplicationNumber(rs.getString("application_number"));
                s.setBorrowerType(rs.getString("borrower_type"));
                s.setBorrowerName(rs.getString("borrower_name"));
                s.setProductType(rs.getString("product_type"));
                s.setRequestedAmount(rs.getDouble("requested_amount"));
                s.setStatus(rs.getString("status"));
                s.setCreatedAt(rs.getString("created_at"));
                return s;
            }
        }
    }
}
