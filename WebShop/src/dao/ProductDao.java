package dao;

import domain.*;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.MapListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import utils.JdbcUtilsConfig;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ProductDao {
    public List<Product> getHotProductList() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where is_hot=1 limit 0,9";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class));
    }

    public List<Product> getNewProductList() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product order by pdate desc limit 0,9";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class));
    }

    public List<Category> getAllCategory() throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from category";
        return queryRunner.query(sql, new BeanListHandler<Category>(Category.class));
    }


    public int getCount(String cid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select count(*) from product where cid=?";
        Long query = (Long) queryRunner.query(sql, new ScalarHandler(), cid);
        return query.intValue();
    }

    public List<Product> findProductByPage(String cid, int index, int currentCount) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where cid=? limit ?,?";
        return queryRunner.query(sql, new BeanListHandler<Product>(Product.class), cid, index, currentCount);
    }

    public Product getProductInfoByPid(String pid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from product where pid=?";
        return queryRunner.query(sql, new BeanHandler<Product>(Product.class), pid);
    }

    //向orders表插入数据
    public void submitOrder(Order order) throws SQLException {
        QueryRunner queryRunner = new QueryRunner();
        Connection connection = JdbcUtilsConfig.getConnection();
        //connection.setAutoCommit(false);

        String sql = "insert into  orders values(?,?,?,?,?,?,?,?)";
        queryRunner.update(connection, sql, order.getOid(), order.getOrderTime(),
                order.getTotal(), order.getState(), order.getAddress(),
                order.getName(), order.getTelephone(), order.getUser().getUid());
    }

    //向orderItem表插入数据
    public void submitOrderItem(Order order) throws SQLException {
        QueryRunner queryRunner = new QueryRunner();
        Connection connection = JdbcUtilsConfig.getConnection();
        //connection.setAutoCommit(false);
        String sql = "insert into orderitem values(?,?,?,?,?)";

        List<OrderItem> orderItemList = order.getOrderItemList();
        for (OrderItem orderItem : orderItemList) {
            queryRunner.update(connection, sql, orderItem.getItemId(),
                    orderItem.getQuantity(), orderItem.getSubtotal(),
                    orderItem.getProduct().getPid(), orderItem.getOrder().getOid());
        }
    }

    public void updatePackageInfo(Order order) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "update orders set address=?, name=?, telephone=? where oid=?";
        queryRunner.update(sql, order.getAddress(), order.getName(), order.getTelephone(), order.getOid());
    }

    public void updateOrderState(String orderId) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "update orders set state=1 where oid=?";
        queryRunner.update(sql, orderId);
    }

    public List<Order> getOrders(User user) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select * from orders where uid=?";
        return queryRunner.query(sql, new BeanListHandler<Order>(Order.class), user.getUid());
    }

    public List<Map<String, Object>> getOrderItems(String oid) throws SQLException {
        QueryRunner queryRunner = new QueryRunner(JdbcUtilsConfig.getDataSource());
        String sql = "select i.quantity, i.subtotal, p.pimage,p.pname, p.shop_price from orderItem i, product p where i.pid=p.pid and i.oid=?";
        return queryRunner.query(sql, new MapListHandler(), oid);
    }
}
