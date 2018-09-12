package web.servlet;

import com.google.gson.Gson;
import domain.Category;
import domain.Order;
import domain.Product;
import service.AdminService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
public class AdminServlet extends BaseServlet {

    public void getAllCategory(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AdminService adminService = new AdminService();
        List<Category> categoryList = null;
        try {
            categoryList = adminService.getAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        String json = gson.toJson(categoryList);
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(json);

    }

    public void getProductList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AdminService adminService = new AdminService();
        List<Product> productList = null;
        try {
            productList = adminService.getProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //将productList放到request域中
        request.setAttribute("productList", productList);
        request.getRequestDispatcher("/admin/product/list.jsp").forward(request, response);
    }

    public void deleteProduct(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AdminService adminService = new AdminService();
        String pid = request.getParameter("pid");
        try {
            adminService.deleteProduct(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            adminService.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin?method=getProductList");

    }

    public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String pid = request.getParameter("pid");

        AdminService adminService = new AdminService();
        Product productInfo = null;
        try {
            productInfo = adminService.getProductInfo(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        //获得所有商品的类别名称
        List<Category> categoryList = null;
        try {
            categoryList = adminService.getAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("categoryList", categoryList);
        request.setAttribute("productInfo", productInfo);
        request.getRequestDispatcher("/admin/product/edit.jsp").forward(request, response);
    }

    public void getAllOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        AdminService adminService = new AdminService();
        List<Order> orderList = null;
        try {
            orderList = adminService.getAllOrder();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("orderList", orderList);
        request.getRequestDispatcher("/admin/order/list.jsp").forward(request, response);
    }

    public void getOrderInfoByOid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InterruptedException {

        Thread.sleep(3000);
        String oid = request.getParameter("oid");

        AdminService adminService = new AdminService();
        List<Map<String, Object>> mapList = null;
        try {
            mapList = adminService.getOrderInfoByOid(oid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        String json = gson.toJson(mapList);
        System.out.println(json);

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(json);
    }
}
