package backend.dto;

public class SystemSectionSettingsDTO {

    private String organizationName;
    private String timezone;

    public SystemSectionSettingsDTO() {
    }

    public SystemSectionSettingsDTO(String organizationName, String timezone) {
        this.organizationName = organizationName;
        this.timezone = timezone;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
