package service;

import dao.AdminDao;
import domain.Category;
import domain.Order;
import domain.Product;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminService {

    public List<Category> getAllCategory() throws SQLException {
        AdminDao adminDao = new AdminDao();
        return adminDao.getAllCategory();


    }

    public void addProduct(Product product) throws SQLException {
        AdminDao adminDao = new AdminDao();
        adminDao.addProduct(product);
    }

    public List<Product> getProductList() throws SQLException {
        AdminDao adminDao = new AdminDao();
        return adminDao.getProductList();
    }

    public void deleteProduct(String pid) throws SQLException {
        AdminDao adminDao = new AdminDao();
        adminDao.deleteProduct(pid);
    }

    public void commit() throws SQLException {
        AdminDao adminDao = new AdminDao();
        adminDao.commit();
    }

    public Product getProductInfo(String pid) throws SQLException {
        AdminDao adminDao = new AdminDao();
        return adminDao.getProductInfo(pid);
    }

    public void updateProduct(Product product) throws SQLException {
        AdminDao adminDao = new AdminDao();
        adminDao.updateProduct(product);
    }

    public List<Order> getAllOrder() throws SQLException {
        AdminDao adminDao = new AdminDao();
        return adminDao.getAllOrder();
    }

    public List<Map<String, Object>> getOrderInfoByOid(String oid) throws SQLException {
        AdminDao adminDao = new AdminDao();
        return adminDao.getOrderInfoByOid(oid);
    }
}
