package ua.kpi.nc.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ua.kpi.nc.domain.dao.RoleDao;
import ua.kpi.nc.domain.model.Role;
import ua.kpi.nc.service.RoleService;

/**
 * Created by Chalienko on 13.04.2016.
 */
@Repository
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleDao roleDao;

    @Override
    public Role getRoleById(Long id) {
        return roleDao.getByID(id);
    }

    @Override
    public Role getRoleByTitle(String title) {
        return roleDao.getByTitle(title);
    }
}
