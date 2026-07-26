package backend.dto;

public class AlertSettingsDTO {

    private Integer criticalThreshold;
    private Integer highThreshold;
    private Boolean autoCreateIncident;

    public AlertSettingsDTO() {
    }

    public AlertSettingsDTO(Integer criticalThreshold, Integer highThreshold, Boolean autoCreateIncident) {
        this.criticalThreshold = criticalThreshold;
        this.highThreshold = highThreshold;
        this.autoCreateIncident = autoCreateIncident;
    }

    public Integer getCriticalThreshold() {
        return criticalThreshold;
    }

    public void setCriticalThreshold(Integer criticalThreshold) {
        this.criticalThreshold = criticalThreshold;
    }

    public Integer getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(Integer highThreshold) {
        this.highThreshold = highThreshold;
    }

    public Boolean getAutoCreateIncident() {
        return autoCreateIncident;
    }

    public void setAutoCreateIncident(Boolean autoCreateIncident) {
        this.autoCreateIncident = autoCreateIncident;
    }
}
