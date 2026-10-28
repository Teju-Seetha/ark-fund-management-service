package com.ark.fundmgmt.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "investors")
public class Investor {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false, unique = true)
    private String email;
    @ManyToMany(mappedBy = "investors", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Fund> funds = new HashSet<>();
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public Investor() {}
    public Investor(String name, String email) {
        this.id = "inv-" + UUID.randomUUID().toString().substring(0, 8);
        this.name = name;
        this.email = email;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public Set<Fund> getFunds() { return funds; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
