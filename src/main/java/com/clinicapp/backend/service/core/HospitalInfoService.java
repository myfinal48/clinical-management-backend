package com.clinicapp.backend.service.core;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.clinicapp.backend.model.core.HospitalInfo;

public interface HospitalInfoService {
    
    HospitalInfo getInfo();
    
    HospitalInfo saveInfo(HospitalInfo info);
    
    HospitalInfo saveInfoAndLogo(String name, String address, String phone, String email, MultipartFile logo) throws IOException;
    
    HospitalInfo updateInfoAndLogo(Long id, String name, String address, String phone, String email, MultipartFile logo) throws IOException;
    
    void deleteInfo(Long id);
    
    HospitalInfo uploadLogo(MultipartFile file) throws IOException;
    
    List<HospitalInfo> getAllInfo();
} 