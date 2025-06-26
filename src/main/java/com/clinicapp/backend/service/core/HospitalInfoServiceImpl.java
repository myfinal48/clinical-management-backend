package com.clinicapp.backend.service.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.model.core.HospitalInfo;
import com.clinicapp.backend.repository.core.HospitalInfoRepository;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HospitalInfoServiceImpl implements HospitalInfoService {
    
    private final HospitalInfoRepository repo;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "svg");

    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Override
    public HospitalInfo getInfo() {
        return repo.findAll().stream().findFirst().orElse(null);
    }

    @Override
    public HospitalInfo saveInfo(HospitalInfo info) {
        return repo.save(info);
    }

    @Override
    public HospitalInfo saveInfoAndLogo(String name, String address, String phone, String email, MultipartFile logo) throws IOException {
        HospitalInfo info = new HospitalInfo();
        // Mise à jour des informations
        if (name != null) info.setName(name);
        if (address != null) info.setAddress(address);
        if (phone != null) info.setPhone(phone);
        if (email != null) info.setEmail(email);

        // Gestion du logo si fourni
        if (logo != null && !logo.isEmpty()) {
            String originalFilename = logo.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ApiException("Nom de fichier invalide", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ApiException(
                    "Format de fichier non autorisé. Formats acceptés: " + String.join(", ", ALLOWED_EXTENSIONS),
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_FORMAT"
                );
            }

            String uniqueSuffix = UUID.randomUUID().toString();
            String objectName = "logo/" + uniqueSuffix + "_" + originalFilename;
            try {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(logo.getInputStream(), logo.getSize(), -1)
                        .contentType(logo.getContentType())
                        .build()
                );
            } catch (Exception e) {
                throw new ApiException("Erreur lors de l'upload sur Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
            }
            info.setLogoPath(objectName);
        }

        return repo.save(info);
    }

    @Override
    public HospitalInfo updateInfoAndLogo(Long id, String name, String address, String phone, String email, MultipartFile logo) throws IOException {
        HospitalInfo info = repo.findById(id).orElseThrow(() ->
            new ApiException("Aucune information d'hôpital trouvée avec l'id : " + id, HttpStatus.NOT_FOUND, "HOSPITAL_INFO_NOT_FOUND"));

        // Mise à jour des informations
        if (name != null) info.setName(name);
        if (address != null) info.setAddress(address);
        if (phone != null) info.setPhone(phone);
        if (email != null) info.setEmail(email);

        // Gestion du logo si fourni
        if (logo != null && !logo.isEmpty()) {
            String originalFilename = logo.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ApiException("Nom de fichier invalide", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ApiException(
                    "Format de fichier non autorisé. Formats acceptés: " + String.join(", ", ALLOWED_EXTENSIONS),
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_FORMAT"
                );
            }

            // Supprimer l'ancien logo sur Minio s'il existe
            if (info.getLogoPath() != null) {
                try {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(info.getLogoPath())
                            .build()
                    );
                } catch (Exception e) {
                    // Log l'erreur mais continue (le fichier pourrait ne pas exister)
                    System.err.println("Erreur lors de la suppression de l'ancien logo sur Minio: " + e.getMessage());
                }
            }

            String uniqueSuffix = UUID.randomUUID().toString();
            String objectName = "logo/" + uniqueSuffix + "_" + originalFilename;
            try {
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(logo.getInputStream(), logo.getSize(), -1)
                        .contentType(logo.getContentType())
                        .build()
                );
            } catch (Exception e) {
                throw new ApiException("Erreur lors de l'upload sur Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
            }
            info.setLogoPath(objectName);
        }

        return repo.save(info);
    }

    @Override
    public void deleteInfo() {
        HospitalInfo info = getInfo();
        if (info == null) {
            throw new ApiException("Aucune information d'hôpital trouvée", HttpStatus.NOT_FOUND, "HOSPITAL_INFO_NOT_FOUND");
        }

        // Supprimer le logo sur Minio s'il existe
        if (info.getLogoPath() != null) {
            try {
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(info.getLogoPath())
                        .build()
                );
            } catch (Exception e) {
                // Log l'erreur mais continue (le fichier pourrait ne pas exister)
                System.err.println("Erreur lors de la suppression du logo sur Minio: " + e.getMessage());
            }
        }

        repo.delete(info);
    }

    @Override
    public HospitalInfo uploadLogo(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ApiException("Nom de fichier invalide", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(
                "Format de fichier non autorisé. Formats acceptés: " + String.join(", ", ALLOWED_EXTENSIONS),
                HttpStatus.BAD_REQUEST,
                "INVALID_FILE_FORMAT"
            );
        }

        HospitalInfo info = getInfo();
        if (info == null) info = new HospitalInfo();

        String uniqueSuffix = UUID.randomUUID().toString();
        String objectName = "logo/" + uniqueSuffix + "_" + originalFilename;
        try {
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(file.getInputStream(), file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build()
            );
        } catch (Exception e) {
            throw new ApiException("Erreur lors de l'upload sur Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
        }
        info.setLogoPath(objectName);
        return repo.save(info);
    }

    @Override
    public List<HospitalInfo> getAllInfo() {
        return repo.findAll();
    }
} 