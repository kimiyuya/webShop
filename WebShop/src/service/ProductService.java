package service;

import dao.ProductDao;
import domain.*;
import utils.JdbcUtilsConfig;
import vo.PageBean;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProductService {
    public List<Product> getHotProductList() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getHotProductList();
    }

    public List<Product> getNewProductList() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getNewProductList();
    }

    public List<Category> getAllCategory() throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getAllCategory();
    }


    public PageBean<Product> findProductListByCid(int currentPage, String cid) throws SQLException {
        PageBean<Product> pageBean = new PageBean<Product>();
        ProductDao productDao = new ProductDao();

        int currentCount = 12;

        pageBean.setCurrentPage(currentPage);
        pageBean.setCurrentCount(currentCount);

        int totalCount = productDao.getCount(cid);
        pageBean.setTotalCount(totalCount);

        int totalPage = (int) Math.ceil(1.0 * totalCount / currentCount);
        pageBean.setTotalPage(totalPage);

        int index = (currentPage - 1) * currentCount;
        List<Product> productList = productDao.findProductByPage(cid, index, currentCount);

        pageBean.setList(productList);

        return pageBean;
    }

    public Product getProductInfoByPid(String pid) throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getProductInfoByPid(pid);
    }

    public void submitOrder(Order order) throws SQLException {
        ProductDao productDao = new ProductDao();
        try {
            JdbcUtilsConfig.startTransaction();
            productDao.submitOrder(order);
            productDao.submitOrderItem(order);
        } catch (SQLException e) {
            JdbcUtilsConfig.rollback();
            e.printStackTrace();
        } finally {
            JdbcUtilsConfig.commitAndRelease();
        }
    }

    public void updatePackageInfo(Order order) throws SQLException {
        ProductDao productDao = new ProductDao();
        productDao.updatePackageInfo(order);
    }

    public void updateOrderState(String orderId) throws SQLException {
        ProductDao productDao = new ProductDao();
        productDao.updateOrderState(orderId);
    }

    public List<Order> getOrders(User user) throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getOrders(user);
    }

    public List<Map<String, Object>> getOrderItems(String oid) throws SQLException {
        ProductDao productDao = new ProductDao();
        return productDao.getOrderItems(oid);
    }
}
