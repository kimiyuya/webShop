<%--
  Created by IntelliJ IDEA.
  User: Junrui Gong
  Date: 6/13/2018
  Time: 2:53 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
    <%
        response.sendRedirect(request.getContextPath()+"/product?method=index");
    %>

</body>
</html>
