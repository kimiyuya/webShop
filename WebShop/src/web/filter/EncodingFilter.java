package web.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@WebFilter(filterName = "EncodingFilter")
public class EncodingFilter implements Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {

        final HttpServletRequest request = (HttpServletRequest) req;

        //使用动态代理完成全局编码
        HttpServletRequest enhanceRequest = (HttpServletRequest) Proxy.newProxyInstance(request.getClass().getClassLoader(), request.getClass().getInterfaces(), new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //对getParameter方法进行增强
                String name = method.getName();//获得目标对象的方法名称
                if ("getParameter".equals(name)) {
                    String invoke = (String) method.invoke(request, args);//乱码
                    //转码
                    invoke = new String(invoke.getBytes("iso8859-1"), "UTF-8");
                    return invoke;

                }
                return method.invoke(request, args);
            }
        });
        chain.doFilter(enhanceRequest, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }
}
