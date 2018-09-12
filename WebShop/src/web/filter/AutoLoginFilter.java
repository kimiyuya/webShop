package web.filter;

import domain.User;
import service.UserService;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLDecoder;
import java.sql.SQLException;

@WebFilter(filterName = "AutoLoginFilter")
public class AutoLoginFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws ServletException, IOException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        String cookieUsername = null;
        String cookiePassword = null;

        //获得cookie中的用户名和密码
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("cookie_username".equals(cookie.getName())) {
                    cookieUsername = cookie.getValue();

                    //进行解码恢复中文
                    cookieUsername = URLDecoder.decode(cookieUsername, "UTF-8");
                }

                if ("cookie_password".equals(cookie.getName())) {
                    cookiePassword = cookie.getValue();
                }
            }
        }

        if (cookieUsername != null && cookiePassword != null) {
            UserService userService = new UserService();
            User user = null;
            try {
                user = userService.login(cookieUsername, cookiePassword);
            } catch (SQLException e) {
                e.printStackTrace();
            }

            //将用户的user对象存在session中
            session.setAttribute("user", user);

        }
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }
}
