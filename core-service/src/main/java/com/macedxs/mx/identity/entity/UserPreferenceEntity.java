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

    @Column(name = "preferred_language", nullable = false, length = 10)
    private String preferredLanguage = "pt-BR";

    @Column(name = "communication_style", nullable = false, length = 40)
    private String communicationStyle = "natural";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_preference_vocabulary", joinColumns = @JoinColumn(name = "preference_id"))
    @Column(name = "hint", nullable = false, length = 40)
    private Set<String> vocabularyHints = new LinkedHashSet<>();

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
        defaults();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        defaults();
    }

    private void defaults() {
        if (learningStyle == null || learningStyle.isBlank()) {
            learningStyle = "balanced";
        }
        if (knowledgeLevel == null || knowledgeLevel.isBlank()) {
            knowledgeLevel = "beginner";
        }
        if (preferredLanguage == null || preferredLanguage.isBlank()) {
            preferredLanguage = "pt-BR";
        }
        if (communicationStyle == null || communicationStyle.isBlank()) {
            communicationStyle = "natural";
        }
        if (topicsOfInterest == null) {
            topicsOfInterest = new LinkedHashSet<>();
        }
        if (vocabularyHints == null) {
            vocabularyHints = new LinkedHashSet<>();
        }
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

    public String getPreferredLanguage() {
        return preferredLanguage;
    }

    public void setPreferredLanguage(String preferredLanguage) {
        this.preferredLanguage = preferredLanguage;
    }

    public String getCommunicationStyle() {
        return communicationStyle;
    }

    public void setCommunicationStyle(String communicationStyle) {
        this.communicationStyle = communicationStyle;
    }

    public Set<String> getVocabularyHints() {
        if (vocabularyHints == null) {
            vocabularyHints = new LinkedHashSet<>();
        }
        return vocabularyHints;
    }

    public void setVocabularyHints(Set<String> vocabularyHints) {
        this.vocabularyHints = vocabularyHints == null
                ? new LinkedHashSet<>()
                : new LinkedHashSet<>(vocabularyHints);
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
