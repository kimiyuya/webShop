package service;

import dao.UserDao;
import domain.User;

import java.sql.SQLException;

public class UserService {
    public boolean register(User user) throws SQLException {
        UserDao userDao = new UserDao();
        int row = userDao.register(user);
        return row > 0;
    }

    public void active(String activeCode) throws SQLException {
        UserDao userDao = new UserDao();
        userDao.active(activeCode);
    }

    public boolean checkUsername(String username) throws SQLException {
        UserDao userDao = new UserDao();
        Long isExist =  userDao.checkUsername(username);
        return isExist > 0;
    }

    public User login(String username, String password) throws SQLException {
        UserDao userDao = new UserDao();
        return userDao.login(username, password);
    }
}
