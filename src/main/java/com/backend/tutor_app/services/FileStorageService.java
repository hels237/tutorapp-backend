package com.backend.tutor_app.services;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Service de stockage de fichiers pour TutorApp
 * Gère l'upload, le stockage et la récupération de fichiers (photos de profil, documents, etc.)
 */
public interface FileStorageService {
    
    // ==================== FILE UPLOAD ====================
    
    /**
     * Upload un fichier
     * @param file Fichier à uploader
     * @param directory Répertoire de destination
     * @return URL du fichier uploadé
     */
    String uploadFile(MultipartFile file, String directory);
    
    /**
     * Upload un fichier avec un nom personnalisé
     * @param file Fichier à uploader
     * @param directory Répertoire de destination
     * @param fileName Nom personnalisé du fichier
     * @return URL du fichier uploadé
     */
    String uploadFile(MultipartFile file, String directory, String fileName);
    
    /**
     * Upload une photo de profil utilisateur
     * @param file Fichier image
     * @param userId ID de l'utilisateur
     * @return URL de la photo de profil
     */
    String uploadProfilePicture(MultipartFile file, Long userId);
    
    /**
     * Upload un document de vérification pour un tuteur
     * @param file Fichier document
     * @param tutorId ID du tuteur
     * @param documentType Type de document (diplôme, casier judiciaire, etc.)
     * @return URL du document
     */
    String uploadTutorDocument(MultipartFile file, Long tutorId, String documentType);
    
    /**
     * Upload multiple files
     * @param files Liste des fichiers à uploader
     * @param directory Répertoire de destination
     * @return Liste des URLs des fichiers uploadés
     */
    List<String> uploadMultipleFiles(List<MultipartFile> files, String directory);

    /**
     * Upload de plusieurs documents pour un tuteur (convenience pour le contrôleur)
     * @param files Fichiers à uploader
     * @param userId ID du tuteur/utilisateur
     * @return Tableau d'URLs des documents uploadés
     */
    String[] uploadTutorDocuments(MultipartFile[] files, Long userId);
    
    // ==================== FILE DOWNLOAD ====================
    
    /**
     * Télécharge un fichier par son nom
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @return Resource du fichier
     */
    Resource downloadFile(String fileName, String directory);
    
    /**
     * Récupère un fichier comme InputStream
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @return InputStream du fichier
     */
    InputStream getFileAsInputStream(String fileName, String directory);
    
    /**
     * Récupère les bytes d'un fichier
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @return Bytes du fichier
     */
    byte[] getFileBytes(String fileName, String directory);
    
    // ==================== FILE MANAGEMENT ====================
    
    /**
     * Supprime un fichier
     * @param fileName Nom du fichier à supprimer
     * @param directory Répertoire du fichier
     * @return true si la suppression a réussi
     */
    boolean deleteFile(String fileName, String directory);
    
    /**
     * Supprime un fichier par son URL
     * @param fileUrl URL du fichier à supprimer
     * @return true si la suppression a réussi
     */
    boolean deleteFileByUrl(String fileUrl);
    
    /**
     * Vérifie si un fichier existe
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @return true si le fichier existe
     */
    boolean fileExists(String fileName, String directory);
    
    /**
     * Renomme un fichier
     * @param oldFileName Ancien nom du fichier
     * @param newFileName Nouveau nom du fichier
     * @param directory Répertoire du fichier
     * @return true si le renommage a réussi
     */
    boolean renameFile(String oldFileName, String newFileName, String directory);
    
    /**
     * Copie un fichier
     * @param sourceFileName Nom du fichier source
     * @param targetFileName Nom du fichier cible
     * @param sourceDirectory Répertoire source
     * @param targetDirectory Répertoire cible
     * @return true si la copie a réussi
     */
    boolean copyFile(String sourceFileName, String targetFileName, String sourceDirectory, String targetDirectory);
    
    // ==================== FILE VALIDATION ====================
    
    /**
     * Valide un fichier (taille, type, etc.)
     * @param file Fichier à valider
     * @param allowedTypes Types de fichiers autorisés
     * @param maxSizeInMB Taille maximale en MB
     * @return true si le fichier est valide
     */
    boolean validateFile(MultipartFile file, List<String> allowedTypes, long maxSizeInMB);
    
    /**
     * Valide une image
     * @param file Fichier image à valider
     * @param maxSizeInMB Taille maximale en MB
     * @return true si l'image est valide
     */
    boolean validateImage(MultipartFile file, long maxSizeInMB);
    
    /**
     * Valide un document
     * @param file Fichier document à valider
     * @param maxSizeInMB Taille maximale en MB
     * @return true si le document est valide
     */
    boolean validateDocument(MultipartFile file, long maxSizeInMB);
    
    /**
     * Récupère le type MIME d'un fichier
     * @param file Fichier
     * @return Type MIME
     */
    String getFileContentType(MultipartFile file);
    
    /**
     * Récupère l'extension d'un fichier
     * @param fileName Nom du fichier
     * @return Extension du fichier
     */
    String getFileExtension(String fileName);
    
    // ==================== DIRECTORY MANAGEMENT ====================
    
    /**
     * Crée un répertoire s'il n'existe pas
     * @param directory Chemin du répertoire
     * @return true si le répertoire a été créé ou existe déjà
     */
    boolean createDirectoryIfNotExists(String directory);
    
    /**
     * Liste les fichiers d'un répertoire
     * @param directory Répertoire à lister
     * @return Liste des noms de fichiers
     */
    List<String> listFiles(String directory);
    
    /**
     * Supprime un répertoire et son contenu
     * @param directory Répertoire à supprimer
     * @return true si la suppression a réussi
     */
    boolean deleteDirectory(String directory);
    
    // ==================== URL GENERATION ====================
    
    /**
     * Génère l'URL publique d'un fichier
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @return URL publique du fichier
     */
    String generateFileUrl(String fileName, String directory);
    
    /**
     * Génère une URL temporaire sécurisée pour un fichier
     * @param fileName Nom du fichier
     * @param directory Répertoire du fichier
     * @param expirationMinutes Durée d'expiration en minutes
     * @return URL temporaire sécurisée
     */
    String generateSecureFileUrl(String fileName, String directory, int expirationMinutes);
    
    // ==================== STATISTICS ====================
    
    /**
     * Récupère les statistiques de stockage
     * @return Map avec les statistiques (espace utilisé, nombre de fichiers, etc.)
     */
    Map<String, Object> getStorageStatistics();
    
    /**
     * Récupère la taille d'un répertoire
     * @param directory Répertoire
     * @return Taille en bytes
     */
    long getDirectorySize(String directory);
    
    /**
     * Récupère le nombre de fichiers dans un répertoire
     * @param directory Répertoire
     * @return Nombre de fichiers
     */
    int getFileCount(String directory);
    
    // ==================== CLEANUP ====================
    
    /**
     * Nettoie les fichiers temporaires anciens
     * @param olderThanDays Fichiers plus anciens que X jours
     * @return Nombre de fichiers supprimés
     */
    int cleanupOldTempFiles(int olderThanDays);
    
    /**
     * Nettoie les fichiers orphelins (non référencés en base)
     * @return Nombre de fichiers supprimés
     */
    int cleanupOrphanedFiles();
}
