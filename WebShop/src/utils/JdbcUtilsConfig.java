//package utils;
//
//
///**
// * 采用连接池、读取配置文件方式使用JDBC。
// */
//
//import org.apache.commons.dbcp.BasicDataSource;
//
//import javax.sql.DataSource;
//import java.io.InputStream;
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.Properties;
//
//public class JdbcUtilsConfig {
//
//    private static BasicDataSource dataSource = new BasicDataSource();
//    private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();
//    private static String driverClass;
//    private static String url;
//    private static String username;
//    private static String password;
//
//    static {
//        readConfig();
//        dataSource.setDriverClassName(driverClass);
//        dataSource.setUrl(url);
//        dataSource.setUsername(username);
//        dataSource.setPassword(password);
//
//        dataSource.setInitialSize(10);
//        dataSource.setMaxActive(8);
//        dataSource.setMaxIdle(5);
//        dataSource.setMinIdle(1);
//
//        dataSource.setTestOnBorrow(true);
//        dataSource.setTestOnReturn(true);
//        dataSource.setTestWhileIdle(true);
//    }
//
//    private static void readConfig() {
//        try {
//            InputStream inputStream = JdbcUtilsConfig.class.getResourceAsStream("database.properties");
//            Properties properties = new Properties();
//            properties.load(inputStream);
//            driverClass = properties.getProperty("driverClass");
//            url = properties.getProperty("url");
//
//            username = properties.getProperty("username");
//            password = properties.getProperty("password");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static DataSource getDataSource() {
//        return dataSource;
//    }
//
//    public static Connection getConnection() throws SQLException {
//        return dataSource.getConnection();
//    }
//
//    // 开启事务
//    public static void startTransaction() throws SQLException {
//        Connection con = getConnection();
//        if (con != null) {
//            con.setAutoCommit(false);
//        }
//    }
//
//    // 事务回滚
//    public static void rollback() throws SQLException {
//        Connection con = getConnection();
//        if (con != null) {
//            con.rollback();
//        }
//    }
//
//    // 提交并且 关闭资源及从ThreadLocal中释放
//    public static void commitAndRelease() throws SQLException {
//        Connection con = getConnection();
//        if (con != null) {
//            con.commit(); // 事务提交
//            con.close();// 关闭资源
//            tl.remove();// 从线程绑定中移除
//        }
//    }
//
//
//}

package utils;

import org.apache.commons.dbcp.BasicDataSource;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;


public class JdbcUtilsConfig {

    private static ThreadLocal<Connection> tl = new ThreadLocal<Connection>();
    private static BasicDataSource dataSource = new BasicDataSource();
    private static String driverClass;
    private static String url;
    private static String username;
    private static String password;


    static {
        readConfig();
        dataSource.setDriverClassName(driverClass);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        dataSource.setInitialSize(10);
        dataSource.setMaxActive(8);
        dataSource.setMaxIdle(5);
        dataSource.setMinIdle(1);

        dataSource.setTestOnBorrow(true);
        dataSource.setTestOnReturn(true);
        dataSource.setTestWhileIdle(true);
    }

    private static void readConfig() {
        try {
            InputStream inputStream = JdbcUtilsConfig.class.getResourceAsStream("database.properties");
            Properties properties = new Properties();
            properties.load(inputStream);
            driverClass = properties.getProperty("driverClass");
            url = properties.getProperty("url");

            username = properties.getProperty("username");
            password = properties.getProperty("password");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 直接可以获取一个连接池
    public static DataSource getDataSource() {
        return dataSource;
    }

    // 获取连接对象
    public static Connection getConnection() throws SQLException {

        Connection con = tl.get();
        if (con == null) {
            con = dataSource.getConnection();
            tl.set(con);
        }
        return con;
    }

    // 开启事务
    public static void startTransaction() throws SQLException {
        Connection con = getConnection();
        if (con != null) {
            con.setAutoCommit(false);
        }
    }

    // 事务回滚
    public static void rollback() throws SQLException {
        Connection con = getConnection();
        if (con != null) {
            con.rollback();
        }
    }

    // 提交并且 关闭资源及从ThreadLocall中释放
    public static void commitAndRelease() throws SQLException {
        Connection con = getConnection();
        if (con != null) {
            con.commit(); // 事务提交
            con.close();// 关闭资源
            tl.remove();// 从线程绑定中移除
        }
    }

    // 关闭资源方法
    public static void closeConnection() throws SQLException {
        Connection con = getConnection();
        if (con != null) {
            con.close();
        }
    }

    public static void closeStatement(Statement st) throws SQLException {
        if (st != null) {
            st.close();
        }
    }

    public static void closeResultSet(ResultSet rs) throws SQLException {
        if (rs != null) {
            rs.close();
        }
    }

}
