package backend.dto;

public class ProfileDTO {

    private String name;
    private String email;
    private String role;
    private String profileImage;
    private String theme;
    private Boolean emailNotificationsEnabled;
    private Boolean inAppNotificationsEnabled;

    public ProfileDTO() {
    }

    public ProfileDTO(String name, String email, String role, String profileImage, String theme,
                      Boolean emailNotificationsEnabled, Boolean inAppNotificationsEnabled) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImage = profileImage;
        this.theme = theme;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.inAppNotificationsEnabled = inAppNotificationsEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getTheme() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }

    public Boolean getEmailNotificationsEnabled() {
        return emailNotificationsEnabled;
    }

    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) {
        this.emailNotificationsEnabled = emailNotificationsEnabled;
    }

    public Boolean getInAppNotificationsEnabled() {
        return inAppNotificationsEnabled;
    }

    public void setInAppNotificationsEnabled(Boolean inAppNotificationsEnabled) {
        this.inAppNotificationsEnabled = inAppNotificationsEnabled;
    }
}
