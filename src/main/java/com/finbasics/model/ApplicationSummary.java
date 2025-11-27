package com.finbasics.model;

import javafx.beans.property.*;

/**
 * Lightweight view model for the Dashboard applicants list.
 */
public class ApplicationSummary {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty applicationNumber = new SimpleStringProperty();
    private final StringProperty borrowerType = new SimpleStringProperty();
    private final StringProperty borrowerName = new SimpleStringProperty();
    private final StringProperty productType = new SimpleStringProperty();
    private final DoubleProperty requestedAmount = new SimpleDoubleProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty createdAt = new SimpleStringProperty();

    // --- Properties

    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }
    public IntegerProperty idProperty() { return id; }

    public String getApplicationNumber() { return applicationNumber.get(); }
    public void setApplicationNumber(String value) { applicationNumber.set(value); }
    public StringProperty applicationNumberProperty() { return applicationNumber; }

    public String getBorrowerType() { return borrowerType.get(); }
    public void setBorrowerType(String value) { borrowerType.set(value); }
    public StringProperty borrowerTypeProperty() { return borrowerType; }

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
}
