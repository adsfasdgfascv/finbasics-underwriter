package com.finbasics.model;

import javafx.beans.property.*;

public class ApplicationSummary {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty applicationNumber = new SimpleStringProperty();
    private final StringProperty borrowerName = new SimpleStringProperty();
    private final StringProperty productType = new SimpleStringProperty();
    private final DoubleProperty requestedAmount = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty createdAt = new SimpleStringProperty();

    // Key ratios from statement_analysis
    private final DoubleProperty dscr = new SimpleDoubleProperty();
    private final DoubleProperty currentRatio = new SimpleDoubleProperty();
    private final DoubleProperty debtToEquity = new SimpleDoubleProperty();
    private final DoubleProperty netMargin = new SimpleDoubleProperty();

    // --- Getters / setters / properties ---

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getApplicationNumber() { return applicationNumber.get(); }
    public void setApplicationNumber(String value) { applicationNumber.set(value); }
    public StringProperty applicationNumberProperty() { return applicationNumber; }

    public String getBorrowerName() { return borrowerName.get(); }
    public void setBorrowerName(String value) { borrowerName.set(value); }
    public StringProperty borrowerNameProperty() { return borrowerName; }

    public String getProductType() { return productType.get(); }
    public void setProductType(String value) { productType.set(value); }
    public StringProperty productTypeProperty() { return productType; }

    public double getRequestedAmount() { return requestedAmount.get(); }
    public void setRequestedAmount(double value) { requestedAmount.set(value); }
    public DoubleProperty requestedAmountProperty() { return requestedAmount; }

    public String getStatus() { return status.get(); }
    public void setStatus(String value) { status.set(value); }
    public StringProperty statusProperty() { return status; }

    public String getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(String value) { createdAt.set(value); }
    public StringProperty createdAtProperty() { return createdAt; }

    public double getDscr() { return dscr.get(); }
    public void setDscr(double value) { dscr.set(value); }
    public DoubleProperty dscrProperty() { return dscr; }

    public double getCurrentRatio() { return currentRatio.get(); }
    public void setCurrentRatio(double value) { currentRatio.set(value); }
    public DoubleProperty currentRatioProperty() { return currentRatio; }

    public double getDebtToEquity() { return debtToEquity.get(); }
    public void setDebtToEquity(double value) { debtToEquity.set(value); }
    public DoubleProperty debtToEquityProperty() { return debtToEquity; }

    public double getNetMargin() { return netMargin.get(); }
    public void setNetMargin(double value) { netMargin.set(value); }
    public DoubleProperty netMarginProperty() { return netMargin; }
}
