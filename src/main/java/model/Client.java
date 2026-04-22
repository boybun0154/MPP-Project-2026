package model;

import java.util.HashSet;
import java.util.Set;
import java.util.Objects;

/**
 * Each client is defined by their name, the industry they operate in,
 * and the primary contact person's name and contact information (phone and email).
 * A single project can be tied to several different clients, and conversely,
 * a single client may sponsor or be associated with numerous different projects across the company.
 */
public class Client {
    private Integer id;
    private String name;
    private String industry;
    private String primaryContactName;
    private String phone;
    private String email;
    private Set<Project> projects = new HashSet<>();

    public Client() {
    }

    // region Getters and Setters

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getPrimaryContactName() {
        return primaryContactName;
    }

    public void setPrimaryContactName(String primaryContactName) {
        this.primaryContactName = primaryContactName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    // endregion Getters and Setters

    // region Overridden Methods

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return Objects.equals(id, client.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // endregion Overridden Methods
}