package com.finbasics.service;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.NewApplication;
import com.finbasics.model.StatementAnalysis;
import com.finbasics.persistence.ApplicationRepository;
import com.finbasics.persistence.AuditRepository;
import com.finbasics.persistence.Database;
import com.finbasics.persistence.StatementAnalysisRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

/**
 * Orchestrates submission -> storage -> automatic analysis.
 */
public class ApplicationService {

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisService analysisService = new StatementAnalysisService();
    private final StatementAnalysisRepository saRepo = new StatementAnalysisRepository();
    private final AuditRepository audit = new AuditRepository();

    public int submitNewApplication(NewApplication newApp) throws ApplicationException {
        var user = Session.getCurrentUser();
        if (user == null) {
            throw new ApplicationException("No logged-in user.");
        }

        try {
            int appId = appRepo.createApplication(newApp, user.getId());

            StatementAnalysis sa = analysisService.buildAutoAnalysis(newApp, appId);
            saRepo.insert(sa);

            try (Connection c = Database.getConnection();
                 PreparedStatement ps = c.prepareStatement(
                         "UPDATE applications SET status = ?, updated_at = ? WHERE id = ?")) {
                ps.setString(1, "ANALYZED");
                ps.setString(2, Instant.now().toString());
                ps.setInt(3, appId);
                ps.executeUpdate();
            }

            audit.log(user.getId(), "APP_SUBMIT", "application_id=" + appId);
            audit.log(user.getId(), "APP_ANALYSIS_AUTO", "application_id=" + appId);

            return appId;
        } catch (SQLException e) {
            throw new ApplicationException("Failed to submit application", e);
        }
    }

    public ObservableList<ApplicationSummary> loadApplicationSummaries() throws SQLException {
        List<ApplicationSummary> list = appRepo.findAllSummaries();
        return FXCollections.observableArrayList(list);
    }
}
