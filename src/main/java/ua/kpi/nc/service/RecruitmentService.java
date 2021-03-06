package ua.kpi.nc.service;

import ua.kpi.nc.persistence.model.Recruitment;

import java.util.List;

/**
 * @author Chalienko  22.04.2016.
 */
public interface RecruitmentService {

    Recruitment getRecruitmentById(Long id);

    Recruitment getRecruitmentByName(String name);

    int updateRecruitment(Recruitment recruitment);

    boolean addRecruitment(Recruitment recruitment);

    int deleteRecruitment(Recruitment recruitment);

    List<Recruitment> getAll();
    
    List<Recruitment> getAllSorted();
    
    Recruitment getCurrentRecruitmnet();

    Recruitment getLastRecruitment();
}
