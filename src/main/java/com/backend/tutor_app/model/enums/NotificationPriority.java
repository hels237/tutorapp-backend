package com.backend.tutor_app.model.enums;

/**
 * Niveaux de priorité des notifications
 */
public enum NotificationPriority {
    LOW("Basse", 1),
    MEDIUM("Moyenne", 2),
    HIGH("Haute", 3),
    CRITICAL("Critique", 4);
    
    private final String description;
    private final int level;
    
    NotificationPriority(String description, int level) {
        this.description = description;
        this.level = level;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getLevel() {
        return level;
    }
    
    /**
     * Détermine si cette priorité est plus élevée qu'une autre
     */
    public boolean isHigherThan(NotificationPriority other) {
        return this.level > other.level;
    }
}
