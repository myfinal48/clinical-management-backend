package com.clinicapp.backend.service.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.model.core.HospitalInfo;
import com.clinicapp.backend.repository.core.HospitalInfoRepository;
import io.minio.MinioClient;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HospitalInfoServiceImpl implements HospitalInfoService {

    private static final Logger logger = LoggerFactory.getLogger(HospitalInfoServiceImpl.class);
    
    private final HospitalInfoRepository repo;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "svg");

    private final MinioClient minioClient;
    
    @Value("${minio.bucket-name}")
    private String bucketName;

    @Value("${minio.url}")
    private String minioUrl;

    @Override
    public HospitalInfo getInfo() {
        HospitalInfo info = repo.findAll().stream().findFirst().orElse(null);
        buildLogoUrl(info);
        return info;
    }

    @Override
    public HospitalInfo saveInfo(HospitalInfo info) {
        HospitalInfo savedInfo = repo.save(info);
        buildLogoUrl(savedInfo);
        return savedInfo;
    }

    @Override
    public HospitalInfo saveInfoAndLogo(String name, String address, String phone, String email, MultipartFile logo) throws IOException {
        HospitalInfo info = new HospitalInfo();
        if (name != null) info.setName(name);
        if (address != null) info.setAddress(address);
        if (phone != null) info.setPhone(phone);
        if (email != null) info.setEmail(email);

        if (logo != null && !logo.isEmpty()) {
            String originalFilename = logo.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ApiException("Invalid file name", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ApiException(
                    "File format is not allowed. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS),
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
                throw new ApiException("Error uploading to Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
            }
            info.setLogoPath(objectName);
        }

        HospitalInfo savedInfo = repo.save(info);
        buildLogoUrl(savedInfo);
        return savedInfo;
    }

    @Override
    public HospitalInfo updateInfoAndLogo(Long id, String name, String address, String phone, String email, MultipartFile logo) throws IOException {
        HospitalInfo info = repo.findById(id).orElseThrow(() ->
            new ApiException("No hospital information found with id : " + id, HttpStatus.NOT_FOUND, "HOSPITAL_INFO_NOT_FOUND"));

        // Mise à jour des informations
        if (name != null) info.setName(name);
        if (address != null) info.setAddress(address);
        if (phone != null) info.setPhone(phone);
        if (email != null) info.setEmail(email);

        // Gestion du logo si fourni
        if (logo != null && !logo.isEmpty()) {
            String originalFilename = logo.getOriginalFilename();
            if (originalFilename == null || originalFilename.isEmpty()) {
                throw new ApiException("Invalid file name", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
            }

            String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new ApiException(
                    "File format is not allowed. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS),
                    HttpStatus.BAD_REQUEST,
                    "INVALID_FILE_FORMAT"
                );
            }

            if (info.getLogoPath() != null) {
                try {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(info.getLogoPath())
                            .build()
                    );
                } catch (Exception e) {
                    System.err.println("Error deleting old logo from Minio: " + e.getMessage());
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
                throw new ApiException("Error uploading to Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
            }
            info.setLogoPath(objectName);
        }

        HospitalInfo savedInfo = repo.save(info);
        buildLogoUrl(savedInfo);
        return savedInfo;
    }

    @Override
    public void deleteInfo(Long id) {
        HospitalInfo info = repo.findById(id).orElseThrow(() ->
                new ApiException("No hospital information found with id: " + id, HttpStatus.NOT_FOUND, "HOSPITAL_INFO_NOT_FOUND"));

        if (info.getLogoPath() != null) {
            try {
                minioClient.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucketName)
                                .object(info.getLogoPath())
                                .build()
                );
                logger.info("Successfully deleted logo from Minio: {}", info.getLogoPath());
            } catch (Exception e) {
                logger.error("Error deleting logo from Minio: " + e.getMessage());
            }
        }

        repo.delete(info);
        logger.info("Successfully deleted hospital information with id: {}", id);
    }

    @Override
    public HospitalInfo uploadLogo(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ApiException("Invalid file name", HttpStatus.BAD_REQUEST, "INVALID_FILENAME");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new ApiException(
                "File format is not allowed. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS),
                HttpStatus.BAD_REQUEST,
                "INVALID_FILE_FORMAT"
            );
        }

        HospitalInfo info = getInfo();
        if (info == null) info = new HospitalInfo();

        String uniqueSuffix = UUID.randomUUID().toString();
        String objectName = "logos/" + uniqueSuffix + "_" + originalFilename;
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
            throw new ApiException("Error uploading to Minio: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, "MINIO_UPLOAD_ERROR");
        }
        info.setLogoPath(objectName);
        HospitalInfo savedInfo = repo.save(info);
        buildLogoUrl(savedInfo);
        return savedInfo;
    }

    @Override
    public List<HospitalInfo> getAllInfo() {
        List<HospitalInfo> infos = repo.findAll();
        infos.forEach(this::buildLogoUrl);
        return infos;
    }

    private void buildLogoUrl(HospitalInfo info) {
        if (info != null && info.getLogoPath() != null) {
            try {
                String url = minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(bucketName)
                                .object(info.getLogoPath())
                                .expiry(15 * 60)
                                .build());
                info.setLogoUrl(url);
            } catch (Exception e) {
                logger.error("Error generating presigned URL: " + e.getMessage());
                info.setLogoUrl(null);
            }
        }
    }
}