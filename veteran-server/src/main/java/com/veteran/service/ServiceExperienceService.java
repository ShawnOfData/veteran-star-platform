package com.veteran.service;

import com.veteran.dto.MilitaryHonorDTO;
import com.veteran.dto.ServiceExperienceDTO;
import com.veteran.vo.MilitaryHonorVO;
import com.veteran.vo.ServiceExperienceVO;

import java.util.List;

public interface ServiceExperienceService {

    ServiceExperienceVO create(ServiceExperienceDTO dto);

    ServiceExperienceVO update(Long id, ServiceExperienceDTO dto);

    void delete(Long id);

    List<ServiceExperienceVO> listByStudentId(Long studentId);

    MilitaryHonorVO addHonor(MilitaryHonorDTO dto);

    void removeHonor(Long honorId);

    List<MilitaryHonorVO> listHonors(Long serviceExperienceId);
}