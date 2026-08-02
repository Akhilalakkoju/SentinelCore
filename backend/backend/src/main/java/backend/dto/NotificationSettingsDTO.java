package backend.dto;

public class NotificationSettingsDTO {

    private Boolean emailEnabled;
    private String smtpHost;
    private Integer smtpPort;
    private String senderEmail;
    private String senderPassword;
    private Boolean webhookEnabled;
    private String webhookUrl;
    private Integer notificationThrottleMinutes;
    private Integer alertEscalationMinutes;

    public NotificationSettingsDTO() {
    }

    public NotificationSettingsDTO(Boolean emailEnabled, String smtpHost, Integer smtpPort, String senderEmail, String senderPassword,
                                   Boolean webhookEnabled, String webhookUrl, Integer notificationThrottleMinutes, Integer alertEscalationMinutes) {
        this.emailEnabled = emailEnabled;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.senderEmail = senderEmail;
        this.senderPassword = senderPassword;
        this.webhookEnabled = webhookEnabled;
        this.webhookUrl = webhookUrl;
        this.notificationThrottleMinutes = notificationThrottleMinutes;
        this.alertEscalationMinutes = alertEscalationMinutes;
    }

    public Boolean getEmailEnabled() {
        return emailEnabled;
    }

    public void setEmailEnabled(Boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }

    public String getSmtpHost() {
        return smtpHost;
    }

    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }

    public Integer getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(Integer smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderPassword() {
        return senderPassword;
    }

    public void setSenderPassword(String senderPassword) {
        this.senderPassword = senderPassword;
    }

    public Boolean getWebhookEnabled() {
        return webhookEnabled;
    }

    public void setWebhookEnabled(Boolean webhookEnabled) {
        this.webhookEnabled = webhookEnabled;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public Integer getNotificationThrottleMinutes() {
        return notificationThrottleMinutes;
    }

    public void setNotificationThrottleMinutes(Integer notificationThrottleMinutes) {
        this.notificationThrottleMinutes = notificationThrottleMinutes;
    }

    public Integer getAlertEscalationMinutes() {
        return alertEscalationMinutes;
    }

    public void setAlertEscalationMinutes(Integer alertEscalationMinutes) {
        this.alertEscalationMinutes = alertEscalationMinutes;
    }
}
