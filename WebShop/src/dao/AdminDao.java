package dao;

import domain.Category;
import domain.Order;
import domain.Product;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import utils.JdbcUtilsConfig;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class AdminDao {
    public List<Category> getAllCategory() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from category";
        return queryRunner.query(sql, new BeanListHandler<Category>(Category.class));
    }

    public void addProduct(Product product) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "INSERT INTO product VALUES (?,?,?,?,?,?,?,?,?,?)";
        queryRunner.update(sql, product.getPid(), product.getPname(), product.getMarket_price(),
                product.getShop_price(), product.getPimage(), product.getPdate(), product.getIs_hot(),
                product.getPdesc(), product.getPflag(), product.getCid());
    }

    public List<Product> getProductList() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class));
    }

    public void deleteProduct(String pid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "delete from product where pid=?";
        queryRunner.update(sql, pid);
    }

    public void commit() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "commit";
        queryRunner.update(sql);
    }

    public Product getProductInfo(String pid) throws SQLException {
        String sql = "select * from product where pid=?";
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        return queryRunner.query(sql, new BeanHandler<Product>(Product.class), pid);
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = "update product set pname=?, market_price=?, shop_price=?, pimage=?, is_hot=?, pdesc=?, cid=? where pid=?";
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        queryRunner.update(sql, product.getPname(), product.getMarket_price(),
                product.getShop_price(), product.getPimage(), product.getIs_hot(),
                product.getPdesc(), product.getCid(), product.getPid());
    }

    public List<Order> getAllOrder() throws SQLException {
        String sql = "select * from orders";
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        return queryRunner.query(sql, new BeanListHandler<Order>(Order.class));

    }

    public List<Map<String, Object>> getOrderInfoByOid(String oid) throws SQLException {
        String sql = "select p.pimage, p.pname, p.shop_price, i.quantity, i.subtotal from orderitem i, product p where i.pid=p.pid and i.oid=?";
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        return queryRunner.query(sql, new MapListHandler(), oid);
    }
}
