package web.filter;

import domain.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(filterName = "UserLoginPrivilegeFilter")
public class UserLoginPrivilegeFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession();
        //判断用户是否登陆
        User user = (User) session.getAttribute("user");
        if (user == null) {
            //没有登陆
            response.sendRedirect(request.getContextPath() + "login.jsp");
            return;
        }

        chain.doFilter(request, response);
    }

    public void init(FilterConfig config) throws ServletException {

    }
}
