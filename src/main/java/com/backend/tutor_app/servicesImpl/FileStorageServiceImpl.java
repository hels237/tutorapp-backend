package com.backend.tutor_app.servicesImpl;

import com.backend.tutor_app.services.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Stream;

/**
 * Implémentation du service de stockage de fichiers pour TutorApp
 * Gère l'upload, le stockage et la récupération de fichiers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageServiceImpl implements FileStorageService {

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:10485760}") // 10MB par défaut
    private long maxFileSize;

    @Value("${app.file.base-url:http://localhost:8080/api/files}")
    private String baseUrl;

    private final List<String> allowedImageTypes = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    private final List<String> allowedDocumentTypes = Arrays.asList(
        "application/pdf", "application/msword", 
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "text/plain"
    );

    // ==================== FILE UPLOAD ====================

    @Override
    public String uploadFile(MultipartFile file, String directory) {
        String fileName = generateUniqueFileName(file.getOriginalFilename());
        return uploadFile(file, directory, fileName);
    }

    @Override
    public String uploadFile(MultipartFile file, String directory, String fileName) {
        log.info("Upload fichier: {} dans répertoire: {}", fileName, directory);
        
        try {
            if (file.isEmpty()) {
                throw new RuntimeException("Fichier vide");
            }

            if (file.getSize() > maxFileSize) {
                throw new RuntimeException("Fichier trop volumineux. Taille max: " + maxFileSize + " bytes");
            }

            // Création du répertoire si nécessaire
            Path directoryPath = Paths.get(uploadDir, directory);
            createDirectoryIfNotExists(directoryPath.toString());

            // Nettoyage du nom de fichier
            String cleanFileName = StringUtils.cleanPath(fileName);
            if (cleanFileName.contains("..")) {
                throw new RuntimeException("Nom de fichier invalide: " + cleanFileName);
            }

            // Sauvegarde du fichier
            Path targetLocation = directoryPath.resolve(cleanFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            String fileUrl = generateFileUrl(cleanFileName, directory);
            log.info("Fichier uploadé avec succès: {}", fileUrl);
            
            return fileUrl;

        } catch (IOException e) {
            log.error("Erreur upload fichier: {} - {}", fileName, e.getMessage());
            throw new RuntimeException("Erreur lors de l'upload: " + e.getMessage());
        }
    }

    @Override
    public String uploadProfilePicture(MultipartFile file, Long userId) {
        log.info("Upload photo de profil pour utilisateur: {}", userId);
        
        try {
            if (!validateImage(file, 5)) { // 5MB max pour les photos de profil
                throw new RuntimeException("Image invalide ou trop volumineuse");
            }

            String directory = "profiles/" + userId;
            String fileName = "profile_" + userId + "_" + System.currentTimeMillis() + 
                            getFileExtension(file.getOriginalFilename());

            return uploadFile(file, directory, fileName);

        } catch (Exception e) {
            log.error("Erreur upload photo de profil pour utilisateur {} - {}", userId, e.getMessage());
            throw new RuntimeException("Erreur upload photo de profil: " + e.getMessage());
        }
    }

    @Override
    public String uploadTutorDocument(MultipartFile file, Long tutorId, String documentType) {
        log.info("Upload document {} pour tuteur: {}", documentType, tutorId);
        
        try {
            if (!validateDocument(file, 10)) { // 10MB max pour les documents
                throw new RuntimeException("Document invalide ou trop volumineux");
            }

            String directory = "tutors/" + tutorId + "/documents";
            String fileName = documentType + "_" + tutorId + "_" + System.currentTimeMillis() + 
                            getFileExtension(file.getOriginalFilename());

            return uploadFile(file, directory, fileName);

        } catch (Exception e) {
            log.error("Erreur upload document {} pour tuteur {} - {}", documentType, tutorId, e.getMessage());
            throw new RuntimeException("Erreur upload document: " + e.getMessage());
        }
    }

    @Override
    public List<String> uploadMultipleFiles(List<MultipartFile> files, String directory) {
        log.info("Upload multiple files dans répertoire: {}", directory);
        
        List<String> uploadedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            try {
                String fileUrl = uploadFile(file, directory);
                uploadedFiles.add(fileUrl);
            } catch (Exception e) {
                log.error("Erreur upload fichier {} - {}", file.getOriginalFilename(), e.getMessage());
                // Continue avec les autres fichiers
            }
        }
        
        return uploadedFiles;
    }

    // ==================== FILE DOWNLOAD ====================

    @Override
    public Resource downloadFile(String fileName, String directory) {
        try {
            Path filePath = Paths.get(uploadDir, directory, fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Fichier non trouvé: " + fileName);
            }
            
        } catch (MalformedURLException e) {
            log.error("Erreur download fichier: {} - {}", fileName, e.getMessage());
            throw new RuntimeException("Erreur download fichier: " + e.getMessage());
        }
    }

    @Override
    public InputStream getFileAsInputStream(String fileName, String directory) {
        try {
            Resource resource = downloadFile(fileName, directory);
            return resource.getInputStream();
        } catch (IOException e) {
            log.error("Erreur récupération InputStream pour fichier: {} - {}", fileName, e.getMessage());
            throw new RuntimeException("Erreur récupération fichier: " + e.getMessage());
        }
    }

    @Override
    public byte[] getFileBytes(String fileName, String directory) {
        try {
            Path filePath = Paths.get(uploadDir, directory, fileName);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Erreur lecture bytes fichier: {} - {}", fileName, e.getMessage());
            throw new RuntimeException("Erreur lecture fichier: " + e.getMessage());
        }
    }

    // ==================== FILE MANAGEMENT ====================

    @Override
    public boolean deleteFile(String fileName, String directory) {
        try {
            Path filePath = Paths.get(uploadDir, directory, fileName);
            boolean deleted = Files.deleteIfExists(filePath);
            
            if (deleted) {
                log.info("Fichier supprimé: {}/{}", directory, fileName);
            }
            
            return deleted;
            
        } catch (IOException e) {
            log.error("Erreur suppression fichier: {} - {}", fileName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteFileByUrl(String fileUrl) {
        try {
            // Extraction du chemin depuis l'URL
            String relativePath = fileUrl.replace(baseUrl + "/", "");
            String[] parts = relativePath.split("/");
            
            if (parts.length < 2) {
                return false;
            }
            
            String fileName = parts[parts.length - 1];
            String directory = String.join("/", Arrays.copyOf(parts, parts.length - 1));
            
            return deleteFile(fileName, directory);
            
        } catch (Exception e) {
            log.error("Erreur suppression fichier par URL: {} - {}", fileUrl, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean fileExists(String fileName, String directory) {
        try {
            Path filePath = Paths.get(uploadDir, directory, fileName);
            return Files.exists(filePath);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean renameFile(String oldFileName, String newFileName, String directory) {
        try {
            Path oldPath = Paths.get(uploadDir, directory, oldFileName);
            Path newPath = Paths.get(uploadDir, directory, newFileName);
            
            Files.move(oldPath, newPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Fichier renommé: {} -> {}", oldFileName, newFileName);
            
            return true;
            
        } catch (IOException e) {
            log.error("Erreur renommage fichier: {} -> {} - {}", oldFileName, newFileName, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean copyFile(String sourceFileName, String targetFileName, String sourceDirectory, String targetDirectory) {
        try {
            Path sourcePath = Paths.get(uploadDir, sourceDirectory, sourceFileName);
            Path targetPath = Paths.get(uploadDir, targetDirectory, targetFileName);
            
            // Création du répertoire cible si nécessaire
            createDirectoryIfNotExists(Paths.get(uploadDir, targetDirectory).toString());
            
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("Fichier copié: {}/{} -> {}/{}", sourceDirectory, sourceFileName, targetDirectory, targetFileName);
            
            return true;
            
        } catch (IOException e) {
            log.error("Erreur copie fichier: {} - {}", sourceFileName, e.getMessage());
            return false;
        }
    }

    // ==================== FILE VALIDATION ====================

    @Override
    public boolean validateFile(MultipartFile file, List<String> allowedTypes, long maxSizeInMB) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        // Vérification de la taille
        long maxSizeInBytes = maxSizeInMB * 1024 * 1024;
        if (file.getSize() > maxSizeInBytes) {
            log.warn("Fichier trop volumineux: {} bytes (max: {} bytes)", file.getSize(), maxSizeInBytes);
            return false;
        }

        // Vérification du type MIME
        String contentType = file.getContentType();
        if (contentType == null || !allowedTypes.contains(contentType.toLowerCase())) {
            log.warn("Type de fichier non autorisé: {}", contentType);
            return false;
        }

        return true;
    }

    @Override
    public boolean validateImage(MultipartFile file, long maxSizeInMB) {
        return validateFile(file, allowedImageTypes, maxSizeInMB);
    }

    @Override
    public boolean validateDocument(MultipartFile file, long maxSizeInMB) {
        return validateFile(file, allowedDocumentTypes, maxSizeInMB);
    }

    @Override
    public String getFileContentType(MultipartFile file) {
        return file.getContentType();
    }

    @Override
    public String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf("."));
    }

    // ==================== DIRECTORY MANAGEMENT ====================

    @Override
    public boolean createDirectoryIfNotExists(String directory) {
        try {
            Path path = Paths.get(directory);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.debug("Répertoire créé: {}", directory);
            }
            return true;
        } catch (IOException e) {
            log.error("Erreur création répertoire: {} - {}", directory, e.getMessage());
            return false;
        }
    }

    @Override
    public List<String> listFiles(String directory) {
        try {
            Path dirPath = Paths.get(uploadDir, directory);
            if (!Files.exists(dirPath)) {
                return new ArrayList<>();
            }

            try (Stream<Path> files = Files.list(dirPath)) {
                return files
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .sorted()
                    .toList();
            }

        } catch (IOException e) {
            log.error("Erreur listage fichiers répertoire: {} - {}", directory, e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean deleteDirectory(String directory) {
        try {
            Path dirPath = Paths.get(uploadDir, directory);
            if (Files.exists(dirPath)) {
                try (Stream<Path> files = Files.walk(dirPath)) {
                    files.sorted(Comparator.reverseOrder())
                         .map(Path::toFile)
                         .forEach(file -> {
                             if (!file.delete()) {
                                 log.warn("Impossible de supprimer: {}", file.getPath());
                             }
                         });
                }
                log.info("Répertoire supprimé: {}", directory);
                return true;
            }
            return false;
        } catch (IOException e) {
            log.error("Erreur suppression répertoire: {} - {}", directory, e.getMessage());
            return false;
        }
    }

    // ==================== URL GENERATION ====================

    @Override
    public String generateFileUrl(String fileName, String directory) {
        return baseUrl + "/" + directory + "/" + fileName;
    }

    @Override
    public String generateSecureFileUrl(String fileName, String directory, int expirationMinutes) {
        // TODO: Implémenter la génération d'URLs sécurisées avec expiration
        // Pour l'instant, on retourne l'URL normale
        return generateFileUrl(fileName, directory);
    }

    // ==================== STATISTICS ====================

    @Override
    public Map<String, Object> getStorageStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            Path uploadPath = Paths.get(uploadDir);
            if (Files.exists(uploadPath)) {
                long totalSize = getDirectorySize(uploadDir);
                int totalFiles = getFileCount(uploadDir);
                
                stats.put("totalSizeBytes", totalSize);
                stats.put("totalSizeMB", totalSize / (1024 * 1024));
                stats.put("totalFiles", totalFiles);
                stats.put("uploadDirectory", uploadDir);
            }
            
            return stats;
            
        } catch (Exception e) {
            log.error("Erreur récupération statistiques stockage - {}", e.getMessage());
            return new HashMap<>();
        }
    }

    @Override
    public long getDirectorySize(String directory) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath)) {
                return 0;
            }

            try (Stream<Path> files = Files.walk(dirPath)) {
                return files
                    .filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
            }

        } catch (IOException e) {
            log.error("Erreur calcul taille répertoire: {} - {}", directory, e.getMessage());
            return 0;
        }
    }

    @Override
    public int getFileCount(String directory) {
        try {
            Path dirPath = Paths.get(directory);
            if (!Files.exists(dirPath)) {
                return 0;
            }

            try (Stream<Path> files = Files.walk(dirPath)) {
                return (int) files.filter(Files::isRegularFile).count();
            }

        } catch (IOException e) {
            log.error("Erreur comptage fichiers répertoire: {} - {}", directory, e.getMessage());
            return 0;
        }
    }

    // ==================== CLEANUP ====================

    @Override
    public int cleanupOldTempFiles(int olderThanDays) {
        log.info("Nettoyage fichiers temporaires plus anciens que {} jours", olderThanDays);
        
        try {
            Path tempDir = Paths.get(uploadDir, "temp");
            if (!Files.exists(tempDir)) {
                return 0;
            }

            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(olderThanDays);
            int deletedCount = 0;

            try (Stream<Path> files = Files.list(tempDir)) {
                for (Path file : files.filter(Files::isRegularFile).toList()) {
                    try {
                        LocalDateTime fileTime = LocalDateTime.ofInstant(
                            Files.getLastModifiedTime(file).toInstant(),
                            java.time.ZoneId.systemDefault()
                        );
                        
                        if (fileTime.isBefore(cutoffDate)) {
                            Files.delete(file);
                            deletedCount++;
                        }
                    } catch (IOException e) {
                        log.warn("Erreur suppression fichier temp: {} - {}", file, e.getMessage());
                    }
                }
            }

            log.info("Nettoyage terminé: {} fichiers temporaires supprimés", deletedCount);
            return deletedCount;

        } catch (IOException e) {
            log.error("Erreur nettoyage fichiers temporaires - {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public int cleanupOrphanedFiles() {
        // TODO: Implémenter le nettoyage des fichiers orphelins
        // Nécessite une vérification en base de données
        log.info("Nettoyage fichiers orphelins (non implémenté)");
        return 0;
    }

    // ==================== MÉTHODES UTILITAIRES ====================

    private String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String extension = getFileExtension(originalFileName);
        String baseName = originalFileName != null ? 
            originalFileName.substring(0, originalFileName.lastIndexOf('.')) : "file";
        
        return baseName + "_" + timestamp + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }
}
