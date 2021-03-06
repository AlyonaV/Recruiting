package ua.kpi.nc.service.impl;

import ua.kpi.nc.persistence.dao.UserTimePriorityDao;
import ua.kpi.nc.persistence.dto.UserTimePriorityDto;
import ua.kpi.nc.persistence.model.ScheduleTimePoint;
import ua.kpi.nc.persistence.model.TimePriorityType;
import ua.kpi.nc.persistence.model.User;
import ua.kpi.nc.persistence.model.UserTimePriority;
import ua.kpi.nc.persistence.model.enums.TimePriorityTypeEnum;
import ua.kpi.nc.service.ScheduleTimePointService;
import ua.kpi.nc.service.ServiceFactory;
import ua.kpi.nc.service.TimePriorityTypeService;
import ua.kpi.nc.service.UserTimePriorityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Korzh
 */
public class UserTimePriorityServiceImpl implements UserTimePriorityService {
	private UserTimePriorityDao userTimePriorityDao;
	private ScheduleTimePointService timePointService = ServiceFactory.getScheduleTimePointService();
	private TimePriorityTypeService priorityTypeService = ServiceFactory.getTimePriorityTypeService();
	private TimePriorityType defaultPriorityType = priorityTypeService.getByID(TimePriorityTypeEnum.CAN.getId());

	public UserTimePriorityServiceImpl(UserTimePriorityDao userTimePriorityDao) {
		this.userTimePriorityDao = userTimePriorityDao;
	}

	@Override
	public UserTimePriority getByUserTime(User user, ScheduleTimePoint scheduleTimePoint) {
		return userTimePriorityDao.getByUserTime(user, scheduleTimePoint);
	}

	@Override
	public Long insertUserPriority(UserTimePriority userTimePriority) {
		return userTimePriorityDao.insertUserPriority(userTimePriority);
	}

	@Override
	public int updateUserPriority(UserTimePriority userTimePriority) {
		return userTimePriorityDao.updateUserPriority(userTimePriority);
	}

	@Override
	public int deleteUserPriority(UserTimePriority userTimePriority) {
		return userTimePriorityDao.deleteUserPriority(userTimePriority);
	}

	@Override
	public List<UserTimePriority> getAllUserTimePriorities(Long userId) {
		return userTimePriorityDao.getAllUserTimePriorities(userId);
	}

	@Override
	public int[] batchUpdateUserPriority(List<UserTimePriority> userTimePriorities) {
		return userTimePriorityDao.batchUpdateUserPriority(userTimePriorities);
	}

	@Override
	public int[] batchCreateUserPriority(List<UserTimePriority> userTimePriorities) {
		return userTimePriorityDao.batchCreateUserPriority(userTimePriorities);
	}

	@Override
	public int[] createStaffTimePriorities(Set<User> staffList) {

		List<ScheduleTimePoint> timePoints = timePointService.getAll();
		List<UserTimePriority> staffTimePriorities = new ArrayList<>();
		for (ScheduleTimePoint timePoint : timePoints){
		for (User staff : staffList){
			UserTimePriority staffPriority = new UserTimePriority();
			staffPriority.setUser(staff);
			staffPriority.setScheduleTimePoint(timePoint);
			staffPriority.setTimePriorityType(defaultPriorityType);
			staffTimePriorities.add(staffPriority);
		}}
		return userTimePriorityDao.batchCreateUserPriority(staffTimePriorities);
	}


	@Override
	public void createStudentTimePriotities(User student) {
		ScheduleTimePointService timePointService = ServiceFactory.getScheduleTimePointService();
		TimePriorityTypeService priorityTypeService = ServiceFactory.getTimePriorityTypeService();
		TimePriorityType defaultPriorityType = priorityTypeService.getByID(TimePriorityTypeEnum.CAN.getId());
		List<ScheduleTimePoint> timePoints = timePointService.getAll();
		for (ScheduleTimePoint scheduleTimePoint : timePoints) {
			UserTimePriority userTimePriority = new UserTimePriority(student, scheduleTimePoint, defaultPriorityType);
			insertUserPriority(userTimePriority);
		}
	}

	@Override
	public boolean isSchedulePrioritiesExistStaff() {
		return userTimePriorityDao.isSchedulePrioritiesExistStaff();
	}

	@Override
	public boolean isSchedulePrioritiesExistStudent() {
		return userTimePriorityDao.isSchedulePrioritiesExistStudent();
	}

	@Override
	public List<UserTimePriorityDto> getAllTimePriorityForUserById(Long userId) {
		return userTimePriorityDao.getAllTimePriorityForUserById(userId);
	}
}
