package backend.dto;

public class RetentionSettingsDTO {

    private Integer logRetentionDays;
    private Integer auditRetentionDays;

    public RetentionSettingsDTO() {
    }

    public RetentionSettingsDTO(Integer logRetentionDays, Integer auditRetentionDays) {
        this.logRetentionDays = logRetentionDays;
        this.auditRetentionDays = auditRetentionDays;
    }

    public Integer getLogRetentionDays() {
        return logRetentionDays;
    }

    public void setLogRetentionDays(Integer logRetentionDays) {
        this.logRetentionDays = logRetentionDays;
    }

    public Integer getAuditRetentionDays() {
        return auditRetentionDays;
    }

    public void setAuditRetentionDays(Integer auditRetentionDays) {
        this.auditRetentionDays = auditRetentionDays;
    }
}
