package com.macedxs.mx.identity.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "user_preferences")
public class UserPreferenceEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private UserEntity user;

    @Column(name = "learning_style", nullable = false, length = 40)
    private String learningStyle = "balanced";

    @Column(name = "knowledge_level", nullable = false, length = 40)
    private String knowledgeLevel = "beginner";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preference_topics", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "topic", nullable = false, length = 80)
    private Set<String> topicsOfInterest = new LinkedHashSet<>();

    @Column(name = "last_active_at")
    private LocalDateTime lastActiveAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (learningStyle == null || learningStyle.isBlank()) {
            learningStyle = "balanced";
        }
        if (knowledgeLevel == null || knowledgeLevel.isBlank()) {
            knowledgeLevel = "beginner";
        }
        if (topicsOfInterest == null) {
            topicsOfInterest = new LinkedHashSet<>();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getLearningStyle() {
        return learningStyle;
    }

    public void setLearningStyle(String learningStyle) {
        this.learningStyle = learningStyle;
    }

    public String getKnowledgeLevel() {
        return knowledgeLevel;
    }

    public void setKnowledgeLevel(String knowledgeLevel) {
        this.knowledgeLevel = knowledgeLevel;
    }

    public Set<String> getTopicsOfInterest() {
        if (topicsOfInterest == null) {
            topicsOfInterest = new LinkedHashSet<>();
        }
        return topicsOfInterest;
    }

    public void setTopicsOfInterest(Set<String> topicsOfInterest) {
        this.topicsOfInterest = topicsOfInterest == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(topicsOfInterest);
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
