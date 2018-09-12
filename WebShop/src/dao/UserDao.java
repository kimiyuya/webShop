package dao;

import domain.User;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import utils.JdbcUtilsConfig;

import java.sql.SQLException;

public class UserDao {
    public int register(User user) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "insert into user values(?,?,?,?,?,?,?,?,?,?)";
        int update = queryRunner.update(sql, user.getUid(), user.getUsername(),
                user.getPassword(), user.getName(), user.getEmail(), user.getTelephone(),
                user.getBirthday(), user.getSex(), user.getState(), user.getCode());
        return update;
    }

    public void active(String activeCode) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "update user set state=? where code=?";
        int update = queryRunner.update(sql, 1, activeCode);
    }

    public Long checkUsername(String username) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select count(*) from user where username=?";
        Long query = (Long) queryRunner.query(sql, new ScalarHandler(), username);
        return query;
    }

    public User login(String username, String password) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from user where username=? and password=?";
        return queryRunner.query(sql, new BeanHandler<User>(User.class), username, password);
    }
}
