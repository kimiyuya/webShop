package web.servlet;

import domain.User;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import service.UserService;
import utils.CommonsUtils;
import utils.MailUtils;

import javax.mail.MessagingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@WebServlet(name = "UserServlet", urlPatterns = {"/user"})
public class UserServlet extends BaseServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void register(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        Map<String, String[]> parameterMap = request.getParameterMap();
        User user = new User();
        try {
            //指定日期转换器(String-->Date)
            ConvertUtils.register(new Converter() {
                @Override
                public Object convert(Class aClass, Object o) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(o.toString());
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    return date;
                }
            }, Date.class);

            BeanUtils.populate(user, parameterMap);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        user.setUid(CommonsUtils.getUUID());
        user.setTelephone(null);
        user.setState(0); //未激活
        user.setCode(CommonsUtils.getUUID());

        UserService userService = new UserService();
        boolean isRegisterSuccess = false;
        try {
            isRegisterSuccess = userService.register(user);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (isRegisterSuccess) {
            //发送激活邮件
            String mailMsg = String.format("恭喜您注册成功，请点击链接进行激活" +
                    "<a href='http://localhost:8080/WebShop/active?activeCode=%s'>" +
                    "http://localhost:8080/WebShop/active?activeCode=%s</a>", user.getCode(), user.getCode());

            try {
                MailUtils.sendMail(user.getEmail(), mailMsg);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
            response.sendRedirect(request.getContextPath() + "/registerSuccess.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/registerFail.jsp");
        }
    }

    public void login(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession();

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        UserService userService = new UserService();
        User user = null;
        try {
            user = userService.login(username, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (user != null) {
            //登陆成功
            //判断用户是否勾选自动登陆
            String autoLogin = request.getParameter("autoLogin");
            if (autoLogin != null) {
                //要对中文名进行编码
                String username_code = URLEncoder.encode(username, "UTF-8");

                Cookie cookie_username = new Cookie("cookie_username", username_code);
                Cookie cookie_password = new Cookie("cookie_password", password);
                cookie_username.setMaxAge(60 * 60);
                cookie_password.setMaxAge(60 * 60);
                cookie_username.setPath(request.getContextPath());
                cookie_password.setPath(request.getContextPath());
                response.addCookie(cookie_username);
                response.addCookie(cookie_password);
            }

            //将用户的user对象存在session中
            session.setAttribute("user", user);
            //重定向到首页
            response.sendRedirect(request.getContextPath() + "/index.jsp");
        } else {
            //失败
            request.setAttribute("loginInfo", "Login failed!");
            request.getRequestDispatcher("/login.jsp").forward(request, response);
        }
    }

    public void active(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String activeCode = request.getParameter("activeCode");

        UserService userService = new UserService();
        try {
            userService.active(activeCode);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }

    public void checkUsername(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String username = request.getParameter("username");
        UserService userService = new UserService();
        boolean isExist = false;
        try {
            isExist = userService.checkUsername(username);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String json = "{\"isExist\":" + isExist + "}";
        response.getWriter().write(json);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.removeAttribute("user");

        //要对中文名进行编码

        Cookie cookie_username = new Cookie("cookie_username", "");
        Cookie cookie_password = new Cookie("cookie_password", "");
        cookie_username.setMaxAge(0);
        cookie_password.setMaxAge(0);
        cookie_username.setPath(request.getContextPath());
        cookie_password.setPath(request.getContextPath());
        response.addCookie(cookie_username);
        response.addCookie(cookie_password);

        response.sendRedirect(request.getContextPath() + "/login.jsp");
    }


}
