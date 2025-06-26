package com.clinicapp.backend.service.core;

import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.model.core.HospitalInfo;
import com.clinicapp.backend.repository.core.HospitalInfoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HospitalInfoService {
    
    private final HospitalInfoRepository repo;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "svg");


    public HospitalInfo getInfo() {
        return repo.findAll().stream().findFirst().orElse(null);
    }

    public HospitalInfo saveInfo(HospitalInfo info) {
        return repo.save(info);
    }

    public HospitalInfo saveInfoAndLogo(String name, String address, String phone, String email, MultipartFile logo) throws IOException {
        HospitalInfo info = getInfo();
        if (info == null) {
            info = new HospitalInfo();
        }

        // Mise à jour des informations
        if (name != null) info.setName(name);
        if (address != null) info.setAddress(address);
        if (phone != null) info.setPhone(phone);
        if (email != null) info.setEmail(email);

        // Gestion du logo si fourni
        if (logo != null && !logo.isEmpty()) {
            // Validation du format de fichier
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

            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "logo" + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = uploadDir + logo.getOriginalFilename();
            logo.transferTo(new File(filePath));
            info.setLogoPath(filePath);
        }

        return repo.save(info);
    }

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
            // Validation du format de fichier
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

            // Supprimer l'ancien logo s'il existe
            if (info.getLogoPath() != null) {
                File oldLogo = new File(info.getLogoPath());
                if (oldLogo.exists()) {
                    oldLogo.delete();
                }
            }

            String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "logo" + File.separator;
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String filePath = uploadDir + logo.getOriginalFilename();
            logo.transferTo(new File(filePath));
            info.setLogoPath(filePath);
        }

        return repo.save(info);
    }

    public void deleteInfo() {
        HospitalInfo info = getInfo();
        if (info == null) {
            throw new ApiException("Aucune information d'hôpital trouvée", HttpStatus.NOT_FOUND, "HOSPITAL_INFO_NOT_FOUND");
        }

        // Supprimer le logo s'il existe
        if (info.getLogoPath() != null) {
            File logo = new File(info.getLogoPath());
            if (logo.exists()) {
                logo.delete();
            }
        }

        repo.delete(info);
    }

    public HospitalInfo uploadLogo(MultipartFile file) throws IOException {
        // Validation du format de fichier
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

        String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "logo" + File.separator;
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = uploadDir + file.getOriginalFilename();
        file.transferTo(new File(filePath));
        info.setLogoPath(filePath);
        return repo.save(info);
    }
} 