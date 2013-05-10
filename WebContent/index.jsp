<%@ page language="java" pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%-- 
    Document   : index
    Created on : 2013-4-21, 14:57:56
    Author     : oubeichen
--%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
    <head>
        <title>Search</title>
        <link rel="stylesheet" type="text/css" href="css/buttons.css"/>
    </head>
    <body>

        <center>
            <img src="images/banner.png" alt="">
            <br/>
                            <div class="buttons">
                <input type="text" maxlength="2048" size="51" id="content" name="wd" value="" style="font-size:16px;height:25px;"/>
                <br/><br/><br/>

                    <button type="submit" class="positive" onclick="onSearch()">
                        <img src="images/apply2.png" alt=""/>
                        Search
                    </button>
                
            </div>
        </center>
        <script type="text/javascript">
            function onSearch(){
                var content = document.getElementById("content").value;
                content = encodeURI(encodeURI(content));
                window.location.href = "results.jsp?wd=" + content;
            }
        </script>
    </body>
</html>
