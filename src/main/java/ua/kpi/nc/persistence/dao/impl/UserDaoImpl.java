package ua.kpi.nc.persistence.dao.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.kpi.nc.persistence.dao.UserDao;
import ua.kpi.nc.persistence.model.Role;
import ua.kpi.nc.persistence.model.ScheduleTimePoint;
import ua.kpi.nc.persistence.model.SocialInformation;
import ua.kpi.nc.persistence.model.User;
import ua.kpi.nc.persistence.model.impl.proxy.RoleProxy;
import ua.kpi.nc.persistence.model.impl.proxy.ScheduleTimePointProxy;
import ua.kpi.nc.persistence.model.impl.proxy.SocialInformationProxy;
import ua.kpi.nc.persistence.model.impl.proxy.UserProxy;
import ua.kpi.nc.persistence.model.impl.real.UserImpl;
import ua.kpi.nc.persistence.util.JdbcTemplate;
import ua.kpi.nc.persistence.util.ResultSetExtractor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Chalienko on 13.04.2016.
 */
public class UserDaoImpl extends JdbcDaoSupport implements UserDao {

    private static Logger log = LoggerFactory.getLogger(UserDaoImpl.class.getName());

    private static final int ROLE_STUDENT = 3;

    private static String direction;

    private ResultSetExtractor<User> extractor = resultSet -> {
        User user = new UserImpl();
        user.setId(resultSet.getLong("id"));
        user.setEmail(resultSet.getString("email"));
        user.setFirstName(resultSet.getString("first_name"));
        user.setLastName(resultSet.getString("last_name"));
        user.setSecondName(resultSet.getString("second_name"));
        user.setPassword(resultSet.getString("password"));
        user.setConfirmToken(resultSet.getString("confirm_token"));
        user.setActive(resultSet.getBoolean("is_active"));
        user.setRegistrationDate(resultSet.getTimestamp("registration_date"));
        user.setRoles(getRoles(resultSet.getLong("id")));
        user.setSocialInformations(getSocialInfomations(resultSet.getLong("id")));
        user.setScheduleTimePoint(getFinalTimePoints(resultSet.getLong("id")));
        return user;
    };

    public UserDaoImpl(DataSource dataSource) {
        this.setJdbcTemplate(new JdbcTemplate(dataSource));
    }

    private static final String SQL_GET_BY_ID = "SELECT u.id, u.email, u.first_name,u.last_name, u.second_name, " +
            "u.password, u.confirm_token, u.is_active, u.registration_date\n" +
            "FROM \"user\" u\n" +
            "WHERE u.id = ?;";

    private static final String SQL_GET_BY_EMAIL = "SELECT u.id, u.email, u.first_name,u.last_name,u.second_name," +
            " u.password,u.confirm_token, u.is_active, u.registration_date\n" +
            "FROM \"user\" u\n" +
            " WHERE u.email = ?;";

    private static final String SQL_EXIST = "select exists(SELECT email from \"user\" where email =?) " +
            " AS \"exist\";";

    private static final String SQL_INSERT = "INSERT INTO \"user\"(email, first_name," +
            " second_name, last_name, password, confirm_token, is_active, registration_date) " +
            "VALUES (?,?,?,?,?,?,?,?);";

    private static final String SQL_UPDATE = "UPDATE \"user\" SET email = ?, first_name  = ?," +
            " second_name = ?, last_name = ?, password = ?, confirm_token = ?, is_active = ?, registration_date = ?" +
            "WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM \"user\" WHERE \"user\".id = ?;";

    private static final String SQL_GET_ALL = "SELECT u.id, u.email, u.first_name,u.last_name,u.second_name," +
            " u.password,u.confirm_token, u.is_active, u.registration_date\n" +
            "FROM \"user\" u\n";
    private static final String INSERT_FINAL_TIME_POINT = "INSERT INTO user_time_final (id_user, id_time_point)" +
            " VALUES (?,?);";

    private static final String DELETE_FINAL_TIME_POINT = "DELETE FROM user_time_final p " +
            "WHERE p.id_user = ? and p.id_time_point = ?;";

    private static final String SQL_GET_FINAL_TIME_POINT = "SELECT sch.time_point from schedule_time_point sch\n" +
            "  join user_time_final utf on sch.id = utf.id_time_point JOIN \"user\" on \"user\".id=utf.id_user where \"user\".id = ?";

    private static final String SQL_GET_USERS_BY_TOKEN = "SELECT u.id, u.email, u.first_name, u.last_name," +
            " u.second_name, u.password, u.confirm_token, u.is_active, u.registration_date\n" +
            " FROM \"user\" u  WHERE u.confirm_token = ?;";

    private static final String SQL_GET_ASSIGNED_STUDENTS_BY_EMP_ID = "SELECT u.id, u.email, u.first_name, u.last_name," +
            " u.second_name, u.password, u.confirm_token, u.is_active, u.registration_date\n" +
            " FROM \"user\" u JOIN application_form a on (a.id_user = u.id and a.is_active = 'true')  " +
            "JOIN interview i ON ( i.id_application_form = a.id )\n" +
            "  JOIN \"user\" us on (us.id = i.id_interviewer) WHERE  us.id = ?;";

    private static final String SQL_GET_ALL_STUDENTS = "SELECT u.id,u.email,u.first_name,u.last_name,u.second_name," +
            "u.password,u.confirm_token,u.is_active, u.registration_date\n" +
            "FROM \"user\" u  INNER JOIN user_role ur ON u.id = ur.id_user\n" +
            "WHERE ur.id_role = " + ROLE_STUDENT;

    private static final String SQL_GET_ALL_EMPLOYEES = "SELECT DISTINCT u.id, u.email, u.first_name,u.last_name," +
            "u.second_name, u.password,u.confirm_token, u.is_active, u.registration_date\n" +
            "FROM \"user\" u  INNER JOIN user_role ur ON u.id = ur.id_user\n" +
            "WHERE ur.id_role <>" + ROLE_STUDENT;

    private static final String SQL_GET_ALL_EMPLOYEES_FOR_ROWS_ASK = "SELECT * FROM (SELECT DISTINCT u.id, u.email, " +
            "u.first_name, u.last_name, u.second_name, u.password,u.confirm_token, u.is_active, u.registration_date" +
            " FROM \"user\" u INNER JOIN user_role ur ON u.id = ur.id_user WHERE ur.id_role <>" + ROLE_STUDENT + " )" +
            " as uuiefnlnsnpctiard ORDER BY ? ASC OFFSET ? LIMIT ?;";


    private static final String SQL_GET_ALL_EMPLOYEES_FOR_ROWS_DESK = "SELECT * FROM (SELECT DISTINCT u.id, u.email, " +
            "u.first_name, u.last_name, u.second_name, u.password,u.confirm_token, u.is_active, u.registration_date" +
            " FROM \"user\" u INNER JOIN user_role ur ON u.id = ur.id_user WHERE ur.id_role <>" + ROLE_STUDENT + " )" +
            " as uuiefnlnsnpctiard ORDER BY ? DESC OFFSET ? LIMIT ?;";

    private static final String SQL_GET_ALL_STUDENTS_FOR_ROWS_ASK = "SELECT u.id,u.email,u.first_name,u.last_name,u.second_name," +
            "u.password,u.confirm_token,u.is_active, u.registration_date\n" +
            "FROM \"user\" u  INNER JOIN user_role ur ON u.id = ur.id_user\n" +
            "WHERE ur.id_role = " + ROLE_STUDENT + " ORDER BY ? ASC OFFSET ? LIMIT ?";

    private static final String SQL_GET_ALL_STUDENTS_FOR_ROWS_DESK = "SELECT u.id,u.email,u.first_name,u.last_name,u.second_name," +
            "u.password,u.confirm_token,u.is_active, u.registration_date\n" +
            "FROM \"user\" u  INNER JOIN user_role ur ON u.id = ur.id_user\n" +
            "WHERE ur.id_role = " + ROLE_STUDENT + " ORDER BY ? DESC OFFSET ? LIMIT ?";

    private static final String SQL_DELETE_TOKEN = "Update \"user\" SET confirm_token = NULL  where id=?";

    private static final String SQL_SEARCH_BY_NAME = "Select * from (Select DISTINCT u.id, u.email, u.first_name, u.last_name, u.second_name, u.password,u.confirm_token, u.is_active, u.registration_date from \"user\" u  INNER JOIN user_role ur ON u.id = ur.id_user\n" +
            "WHERE (ur.id_role = 2 OR ur.id_role = 5) AND u.last_name LIKE ? ) as uuiefnlnsnpctiard ORDER BY 2 ASC OFFSET 0 LIMIT 9";

    @Override
    public User getByID(Long id) {
        log.info("Looking for user with id = {}", id);
        return this.getJdbcTemplate().queryWithParameters(SQL_GET_BY_ID, extractor, id);
    }

    @Override
    public User getByUsername(String email) {
        log.info("Looking for user with email = {}", email);
        return this.getJdbcTemplate().queryWithParameters(SQL_GET_BY_EMAIL, extractor, email);
    }

    @Override
    public boolean isExist(String email) {
        return this.getJdbcTemplate().queryWithParameters(SQL_EXIST, resultSet -> resultSet.getBoolean(1), email);
    }

    @Override
    public Long insertUser(User user, Connection connection) {
        log.info("Insert user with email = {}", user.getEmail());
        return this.getJdbcTemplate().insert(SQL_INSERT, connection, user.getEmail(), user.getFirstName(), user.getSecondName(),
                user.getLastName(), user.getPassword(), user.getConfirmToken(), user.isActive(), user.getRegistrationDate());
    }

    @Override
    public int updateUser(User user) {
        log.info("Update user with id = {}", user.getId());
        return this.getJdbcTemplate().update(SQL_UPDATE, user.getEmail(), user.getFirstName(), user.getSecondName(),
                user.getLastName(), user.getPassword(), user.getConfirmToken(), user.isActive(), user.getRegistrationDate(),
                user.getId());
    }

    @Override
    public int deleteUser(User user) {
        log.info("Delete user with id = ", user.getId());
        return this.getJdbcTemplate().update(SQL_DELETE, user.getId());
    }

    @Override
    public boolean addRole(User user, Role role) {
        if (user.getId() == null) {
            log.warn(String.format("User: %s don`t have id", user.getEmail()));
            return false;
        }
        return this.getJdbcTemplate().insert("INSERT INTO \"user_role\"(id_user, id_role) VALUES (?,?)",
                user.getId(), role.getId()) > 0;
    }

    @Override
    public boolean addRole(User user, Role role, Connection connection) {
        if (user.getId() == null) {
            log.warn("User: don`t have id", user.getEmail());
            return false;
        }
        return this.getJdbcTemplate().insert("INSERT INTO \"user_role\"(id_user, id_role) VALUES (?,?);", connection,
                user.getId(), role.getId()) > 0;
    }

    @Override
    public int deleteRole(User user, Role role) {
        if (user.getId() == null) {
            log.warn("User: don`t have id, {}", user.getEmail());
            return 0;
        }
        return this.getJdbcTemplate().update("DELETE FROM \"user_role\" WHERE id_user= ? AND " +
                "id_role = ?", user.getId(), role.getId());
    }

    @Override
    public Long insertFinalTimePoint(User user, ScheduleTimePoint scheduleTimePoint) {
        log.info("Insert Final Time Point");
        return this.getJdbcTemplate().insert(INSERT_FINAL_TIME_POINT, user.getId(), scheduleTimePoint.getId());
    }

    @Override
    public int deleteFinalTimePoint(User user, ScheduleTimePoint scheduleTimePoint) {
        log.info("Delete Final Time Point");
        return this.getJdbcTemplate().update(DELETE_FINAL_TIME_POINT, user.getId(), scheduleTimePoint.getId());
    }

    @Override
    public User getUserByToken(String token) {
        log.info("Get users by token");
        return this.getJdbcTemplate().queryWithParameters(SQL_GET_USERS_BY_TOKEN, extractor, token);
    }


    @Override
    public Set<User> getAssignedStudents(Long id) {
        log.info("Get Assigned Students");
        return this.getJdbcTemplate().queryForSet(SQL_GET_ASSIGNED_STUDENTS_BY_EMP_ID, extractor, id);
    }

    @Override
    public Set<User> getAllStudents() {
        log.info("Get all Students");
        return this.getJdbcTemplate().queryForSet(SQL_GET_ALL_STUDENTS, extractor);
    }

    @Override
    public List<User> getEmployeesFromToRows(Long fromRows, Long rowsNum, Long sortingCol, boolean increase) {
        log.info("Get Employees From To Rows");
        String sql = increase ? SQL_GET_ALL_EMPLOYEES_FOR_ROWS_ASK : SQL_GET_ALL_EMPLOYEES_FOR_ROWS_DESK;
        return this.getJdbcTemplate().queryForList(sql, extractor, sortingCol,
                fromRows, rowsNum);
    }

    @Override
    public List<User> getStudentsFromToRows(Long fromRows, Long rowsNum, Long sortingCol, boolean increase) {
        log.info("Get Students From To Rows");
        String sql = increase ? SQL_GET_ALL_STUDENTS_FOR_ROWS_ASK : SQL_GET_ALL_STUDENTS_FOR_ROWS_DESK;
        return this.getJdbcTemplate().queryForList(sql, extractor, sortingCol,
                fromRows, rowsNum);
    }

    @Override
    public Set<User> getAllEmploees() {
        log.info("Get all Employees");
        return this.getJdbcTemplate().queryForSet(SQL_GET_ALL_EMPLOYEES, extractor);
    }

    @Override
    public Set<User> getAll() {
        log.info("Get all Users");
        return this.getJdbcTemplate().queryForSet(SQL_GET_ALL, extractor);
    }

    private Set<Role> getRoles(Long userID) {
        return this.getJdbcTemplate().queryWithParameters("SELECT ur.id_role\n" +
                "FROM user_role ur " +
                "WHERE ur.id_user = ?;", resultSet -> {
            Set<Role> roles = new HashSet<>();
            do {
                roles.add(new RoleProxy(resultSet.getLong("id_role")));
            } while (resultSet.next());
            return roles;
        }, userID);
    }

    private Set<SocialInformation> getSocialInfomations(Long userID) {
        return this.getJdbcTemplate().queryWithParameters("SELECT si.id\n" +
                "FROM \"social_information\" si\n" +
                "WHERE si.id_user = ?;", resultSet -> {
            Set<SocialInformation> set = new HashSet<>();
            do {
                set.add(new SocialInformationProxy(resultSet.getLong("id")));
            } while (resultSet.next());
            return set;
        }, userID);
    }

    @Override
    public Long getEmployeeCount() {
        return new Long(String.valueOf(getAllEmploees().size()));
    }

    @Override
    public Long getStudentCount() {
        return new Long(String.valueOf(getAllStudents().size()));
    }

    @Override
    public int deleteToken(Long id) {
        log.info("Delete token user with id = {}", id);
        return this.getJdbcTemplate().update(SQL_DELETE_TOKEN, id);
    }

    @Override
    public List<ScheduleTimePoint> getFinalTimePoints(Long timeID) {
        return this.getJdbcTemplate().queryWithParameters(SQL_GET_FINAL_TIME_POINT, resultSet -> {
            List<ScheduleTimePoint> list = new ArrayList<ScheduleTimePoint>();
            do {
                list.add(new ScheduleTimePointProxy(resultSet.getLong("id")));
            } while (resultSet.next());
            return list;
        }, timeID);
    }

    @Override
    public List<User> getEmployeesByNameFromToRows(String lastName) {
        return this.getJdbcTemplate().queryForList(SQL_SEARCH_BY_NAME, extractor, "%"+lastName+"%");
    }
}
