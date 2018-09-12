package web.servlet;

import com.google.gson.Gson;
import domain.*;
import org.apache.commons.beanutils.BeanUtils;
import redis.clients.jedis.Jedis;
import service.ProductService;
import utils.CommonsUtils;
import utils.JedisPoolUtils;
import utils.PaymentUtil;
import vo.PageBean;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.*;

@WebServlet(name = "ProductServlet", urlPatterns = {"/product"})
public class ProductServlet extends BaseServlet {

    //显示商品的分类
    public void categoryList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ProductService productService = new ProductService();

        //先从缓存中查询category，如果没有再从数据库中查询，存在缓存中
        Jedis jedis = JedisPoolUtils.getJedis();
        String categoryListJson = jedis.get("categoryListJson");
        if (categoryListJson == null) {
            //商品分类
            List<Category> allCategoryList = null;
            try {
                allCategoryList = productService.getAllCategory();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            Gson gson = new Gson();
            categoryListJson = gson.toJson(allCategoryList);
            jedis.set("categoryListJson", categoryListJson);

        }

        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write(categoryListJson);
    }

    //首页显示热门商品、最新商品
    public void index(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ProductService productService = new ProductService();

        //热门商品
        List<Product> hotProductList = null;
        try {
            hotProductList = productService.getHotProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("hotProductList", hotProductList);

        //最新商品
        List<Product> newProductList = null;
        try {
            newProductList = productService.getNewProductList();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("newProductList", newProductList);

        //商品分类
        List<Category> allCategoryList = null;
        try {
            allCategoryList = productService.getAllCategory();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        request.setAttribute("allCategoryList", allCategoryList);

        request.getRequestDispatcher("/index.jsp").forward(request, response);
    }

    //显示商品的详细信息
    public void productInfo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String currentPage = request.getParameter("currentPage");
        String cid = request.getParameter("cid");
        String pid = request.getParameter("pid");

        ProductService productService = new ProductService();
        Product product = null;
        try {
            product = productService.getProductInfoByPid(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("product", product);
        request.setAttribute("currentPage", currentPage);
        request.setAttribute("cid", cid);

        //获得客户端携带得cookie---pid(s)
        String pids = pid;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("pids".equals(cookie.getName())) {
                    pids = cookie.getValue();
                    String[] split = pids.split("-");
                    List<String> asList = Arrays.asList(split);
                    LinkedList<String> list = new LinkedList<String>(asList);
                    //判断集合中是否存在当前pid
                    if (list.contains(pid)) {
                        list.remove(pid);
                    }
                    list.addFirst(pid);

                    //将集合转成字符串
                    StringBuffer stringBuffer = new StringBuffer();
                    for (int i = 0; i < list.size() && i < 7; i++) {
                        stringBuffer.append(list.get(i));
                        stringBuffer.append("-");
                    }
                    //去掉最后一个“-”
                    pids = stringBuffer.substring(0, stringBuffer.length() - 1);
                }
            }
        }

        Cookie newCookie = new Cookie("pids", pids);
        response.addCookie(newCookie);

        request.getRequestDispatcher("/product_info.jsp").forward(request, response);
    }


    //根据cid获得商品信息
    public void productListByCid(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String cid = request.getParameter("cid");
        ProductService productService = new ProductService();

        //模拟当前页是第一页
        String currentPageStr = request.getParameter("currentPage");
        if (currentPageStr == null) {
            currentPageStr = "1";
        }
        int currentPage = Integer.parseInt(currentPageStr);

        PageBean<Product> pageBean = null;
        try {
            pageBean = productService.findProductListByCid(currentPage, cid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        request.setAttribute("pageBean", pageBean);
        request.setAttribute("cid", cid);

        //定义一个集合用于记录历史商品信息
        List<Product> historyProductList = new ArrayList<>();

        //获得客户端携带的pids的cookies
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("pids".equals(cookie.getName())) {
                    String pids = cookie.getValue();
                    String[] split = pids.split("-");
                    for (String pid : split) {
                        Product product = null;
                        try {
                            product = productService.getProductInfoByPid(pid);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                        historyProductList.add(product);
                    }
                }
            }
        }
        request.setAttribute("historyProductList", historyProductList);

        request.getRequestDispatcher("/product_list.jsp").forward(request, response);
    }

    //将商品添加到购物车
    public void addProductToCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        String pid = request.getParameter("pid");
        int quantity = Integer.parseInt(request.getParameter("quantity"));


        ProductService productService = new ProductService();
        Product product = null;
        try {
            product = productService.getProductInfoByPid(pid);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        double subtotal = quantity * product.getShop_price();

        //封装cartItem
        CartItem cartItem = new CartItem();
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setSubtotal(subtotal);

        //封装购物车----先判断是否已经存在购物车
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
        }

        Map<String, CartItem> cartItems = cart.getCartItems();
        //判断该购物车中是否已经含有此商品
        if (cartItems.containsKey(pid)) {

            //修改数量
            CartItem cartItem1 = cartItems.get(pid);
            int oldQuantity = cartItem1.getQuantity();
            oldQuantity += quantity;
            cartItem1.setQuantity(oldQuantity);

            //修改小计
            cartItem1.setSubtotal(oldQuantity * cartItem1.getProduct().getShop_price());

        } else {
            //将商品放到购物车中
            cart.getCartItems().put(product.getPid(), cartItem);
        }

        //计算总金额
        double total = cart.getTotal() + subtotal;
        cart.setTotal(total);

        session.setAttribute("cart", cart);

        //直接跳转到购物车
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

    //删除购物车商品
    public void deleteProductFromCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        String pid = request.getParameter("pid");
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {

            cart.setTotal(cart.getTotal() - cart.getCartItems().get(pid).getSubtotal());//修改总计
            cart.getCartItems().remove(pid);  //删除该商品
            session.setAttribute("cart", cart);
        }

        //直接跳转到购物车
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

    //清空购物车
    public void clearCart(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("cart");
        response.sendRedirect(request.getContextPath() + "/cart.jsp");
    }

    //提交订单
    public void submitOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        Order order = new Order();
        String oid = CommonsUtils.getUUID();
        order.setOid(oid);

        order.setOrderTime(new Date());

        Cart cart = (Cart) session.getAttribute("cart");
        if (cart != null) {
            order.setTotal(cart.getTotal());
        }

        order.setState(0);
        order.setAddress(null);
        order.setName(null);
        order.setTelephone(null);
        order.setUser(user);

        //获得购物车中购物项的集合
        Map<String, CartItem> cartItems = cart.getCartItems();

        //将每个购物项分别封装到订单项中
        for (Map.Entry<String, CartItem> entry : cartItems.entrySet()) {

            //获得一个购物项
            CartItem cartItem = entry.getValue();

            //创建新的订单项
            OrderItem orderItem = new OrderItem();

            orderItem.setItemId(CommonsUtils.getUUID());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setSubtotal(cartItem.getSubtotal());
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setOrder(order);  //该订单项属于哪个订单

            //将订单项添加到订单中
            order.getOrderItemList().add(orderItem);
        }

        //order已经封装完毕
        ProductService productService = new ProductService();
        try {
            productService.submitOrder(order);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        session.setAttribute("order", order);
        response.sendRedirect(request.getContextPath() + "/order_info.jsp");
    }

    //确认订单
    public void confirmOrder(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InvocationTargetException, IllegalAccessException, SQLException {
        //更新收货人信息
        Map<String, String[]> parameterMap = request.getParameterMap();
        Order order = new Order();
        BeanUtils.populate(order, parameterMap);

        ProductService productService = new ProductService();
        productService.updatePackageInfo(order);

        // 获得 支付必须基本数据
        String orderid = request.getParameter("oid");
        //String money = order.getTotal() + "";
        String money = "0.01";
        // 银行
        String pd_FrpId = request.getParameter("pd_FrpId");

        // 发给支付公司需要哪些数据
        String p0_Cmd = "Buy";
        String p1_MerId = ResourceBundle.getBundle("merchantInfo").getString("p1_MerId");
        String p2_Order = orderid;
        String p3_Amt = money;
        String p4_Cur = "CNY";
        String p5_Pid = "";
        String p6_Pcat = "";
        String p7_Pdesc = "";
        // 支付成功回调地址 ---- 第三方支付公司会访问、用户访问
        // 第三方支付可以访问网址
        String p8_Url = ResourceBundle.getBundle("merchantInfo").getString("callback");
        String p9_SAF = "";
        String pa_MP = "";
        String pr_NeedResponse = "1";
        // 加密hmac 需要密钥
        String keyValue = ResourceBundle.getBundle("merchantInfo").getString(
                "keyValue");
        String hmac = PaymentUtil.buildHmac(p0_Cmd, p1_MerId, p2_Order, p3_Amt,
                p4_Cur, p5_Pid, p6_Pcat, p7_Pdesc, p8_Url, p9_SAF, pa_MP,
                pd_FrpId, pr_NeedResponse, keyValue);


        String url = "https://www.yeepay.com/app-merchant-proxy/node?pd_FrpId=" + pd_FrpId +
                "&p0_Cmd=" + p0_Cmd +
                "&p1_MerId=" + p1_MerId +
                "&p2_Order=" + p2_Order +
                "&p3_Amt=" + p3_Amt +
                "&p4_Cur=" + p4_Cur +
                "&p5_Pid=" + p5_Pid +
                "&p6_Pcat=" + p6_Pcat +
                "&p7_Pdesc=" + p7_Pdesc +
                "&p8_Url=" + p8_Url +
                "&p9_SAF=" + p9_SAF +
                "&pa_MP=" + pa_MP +
                "&pr_NeedResponse=" + pr_NeedResponse +
                "&hmac=" + hmac;

        //重定向到第三方支付平台
        response.sendRedirect(url);
    }

    //获得当前用户的订单
    public void getOrders(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, InvocationTargetException, IllegalAccessException, SQLException {
        HttpSession session = request.getSession();

        ProductService productService = new ProductService();
        User user = (User) session.getAttribute("user");
        //查询该用户的所有订单
        List<Order> orderList = productService.getOrders(user);

        if (orderList != null) {
            for (Order order : orderList) {

                //多个订单项和该订单项中商品的信息
                List<Map<String, Object>> mapList = productService.getOrderItems(order.getOid());
                //将mapList转换成List<OrderItem> orderItems
                for (Map<String, Object> map : mapList) {
                    //从map中取出quantity，subtotal封装到orderItem中
                    OrderItem orderItem = new OrderItem();
                    BeanUtils.populate(orderItem, map);

                    //从map中取出pimage，pname， shop_price封装到Product中
                    Product product = new Product();
                    BeanUtils.populate(product, map);

                    //将product封装到OrderItem
                    orderItem.setProduct(product);

                    //将OrderItem分装到order中的orderItemList
                    order.getOrderItemList().add(orderItem);
                }
            }
        }

        //OrderList封装完毕
        request.setAttribute("orderList", orderList);
        request.getRequestDispatcher("/order_list.jsp").forward(request, response);
    }
}
