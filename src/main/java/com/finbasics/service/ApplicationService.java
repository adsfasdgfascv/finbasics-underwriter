package com.finbasics.service;

import com.finbasics.model.ApplicationSummary;
import com.finbasics.model.NewApplication;
import com.finbasics.model.StatementAnalysis;
import com.finbasics.persistence.ApplicationRepository;
import com.finbasics.persistence.AuditRepository;
import com.finbasics.persistence.StatementAnalysisRepository;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class ApplicationService {

    private final ApplicationRepository appRepo = new ApplicationRepository();
    private final StatementAnalysisService analysisService = new StatementAnalysisService();
    private final StatementAnalysisRepository analysisRepo = new StatementAnalysisRepository();
    private final AuditRepository audit = new AuditRepository();

    public int submitNewApplication(NewApplication newApp) throws ApplicationException {
        var currentUser = Session.getCurrentUser();
        if (currentUser == null) {
            throw new ApplicationException("No logged-in user in session");
        }

        try {
            // 1) Persist core application + details
            int appId = appRepo.createApplication(newApp, currentUser.getId());

            // 2) Auto statement analysis
            StatementAnalysis s = analysisService.buildAutoAnalysis(newApp, appId);
            s.setCreatedAt(Instant.now().toString());
            analysisRepo.insert(s);

            // 3) Update application status to ANALYZED
            try (var c = com.finbasics.persistence.Database.getConnection();
                 var ps = c.prepareStatement(
                         "UPDATE applications SET status = ?, updated_at = ? WHERE id = ?")) {
                ps.setString(1, "ANALYZED");
                ps.setString(2, Instant.now().toString());
                ps.setInt(3, appId);
                ps.executeUpdate();
            }

            // 4) Audit
            audit.log(currentUser.getId(), "APP_SUBMIT",
                    "application_id=" + appId + "; borrower=" + newApp.getBorrowerName());
            audit.log(currentUser.getId(), "APP_ANALYSIS_AUTO",
                    "application_id=" + appId);

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
