package web.servlet;

import domain.Product;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import service.AdminService;
import utils.CommonsUtils;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


@WebServlet(name = "AdminAddProductServlet", urlPatterns = {"/adminAddProduct"})
public class AdminAddProductServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Product product = new Product();

        //收集数据的容器
        Map<String, Object> map = new HashMap<String, Object>();

        try {
            DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();

            ServletFileUpload servletFileUpload = new ServletFileUpload(diskFileItemFactory);

            List<FileItem> parseRequest = servletFileUpload.parseRequest(request);

            for (FileItem fileItem : parseRequest) {
                boolean formField = fileItem.isFormField();
                if (formField) {
                    //普通表单项,获得表单输入的数据,封装到Product中
                    String fieldName = fileItem.getFieldName();
                    String fieldValue = fileItem.getString("UTF-8");
                    map.put(fieldName, fieldValue);

                } else {
                    //文件上传项，获得文件名称和内容
                    String fileName = fileItem.getName();
                    String path = this.getServletContext().getRealPath("upload");
                    InputStream inputStream = fileItem.getInputStream();
                    OutputStream outputStream = new FileOutputStream(path + "/" + fileName);
                    IOUtils.copy(inputStream, outputStream);
                    inputStream.close();
                    outputStream.close();
                    fileItem.delete();

                    map.put("pimage", "upload/" + fileName);
                }

            }
            BeanUtils.populate(product, map);

            product.setPid(CommonsUtils.getUUID());

            product.setPimage("products/1/c_0001.jpg");

            //private String pdate; //上架日期
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String pdate = simpleDateFormat.format(new Date());
            product.setPdate(pdate);

            //private int pflag;  //下架日期 0代表未下架
            product.setPflag(0);

            AdminService adminService = new AdminService();
            try {
                adminService.addProduct(product);
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } catch (FileUploadException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        response.sendRedirect(request.getContextPath() + "/admin?method=getProductList");


    }
}
