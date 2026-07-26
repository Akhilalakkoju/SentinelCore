package backend.dto;

public class ProfileDTO {

    private String name;
    private String email;
    private String role;
    private String profileImage;
    private String theme;

    public ProfileDTO() {
    }

    public ProfileDTO(String name, String email, String role, String profileImage, String theme) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.profileImage = profileImage;
        this.theme = theme;
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
}
