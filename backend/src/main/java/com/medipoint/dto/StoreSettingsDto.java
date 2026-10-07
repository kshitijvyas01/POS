package com.medipoint.dto;

public class StoreSettingsDto {
    private String storeName;
    private String tagline;
    private String address;
    private String phone;
    private String email;
    private String licenseNumber;
    private String gstNumber;
    private String upiId;
    private String qrCodeUrl;
    private String invoicePrefix;
    private String invoiceFooter;
    private String updatedAt;
    private String updatedBy;

    public StoreSettingsDto() {}

    public StoreSettingsDto(String storeName, String tagline, String address, String phone, String email,
                            String licenseNumber, String gstNumber, String upiId, String qrCodeUrl,
                            String invoicePrefix, String invoiceFooter, String updatedAt, String updatedBy) {
        this.storeName = storeName;
        this.tagline = tagline;
        this.address = address;
        this.phone = phone;
        this.email = email;
        this.licenseNumber = licenseNumber;
        this.gstNumber = gstNumber;
        this.upiId = upiId;
        this.qrCodeUrl = qrCodeUrl;
        this.invoicePrefix = invoicePrefix;
        this.invoiceFooter = invoiceFooter;
        this.updatedAt = updatedAt;
        this.updatedBy = updatedBy;
    }

    public String getStoreName() { return storeName; }
    public void setStoreName(String storeName) { this.storeName = storeName; }
    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    public String getGstNumber() { return gstNumber; }
    public void setGstNumber(String gstNumber) { this.gstNumber = gstNumber; }
    public String getUpiId() { return upiId; }
    public void setUpiId(String upiId) { this.upiId = upiId; }
    public String getQrCodeUrl() { return qrCodeUrl; }
    public void setQrCodeUrl(String qrCodeUrl) { this.qrCodeUrl = qrCodeUrl; }
    public String getInvoicePrefix() { return invoicePrefix; }
    public void setInvoicePrefix(String invoicePrefix) { this.invoicePrefix = invoicePrefix; }
    public String getInvoiceFooter() { return invoiceFooter; }
    public void setInvoiceFooter(String invoiceFooter) { this.invoiceFooter = invoiceFooter; }
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
