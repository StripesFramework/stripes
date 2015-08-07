<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
    <head>
        <script src="//code.jquery.com/jquery-1.11.3.min.js" type="text/javascript"></script>
        <script>
            $(document).ready(function () {

                $("#calculatorForm").submit(function (e)
                {
                    e.preventDefault(); //STOP default action

                    var postData = $(this).serializeArray();
                    var formURL = $(this).attr("action");
                    $.ajax(
                            {
                                url: formURL,
                                type: "POST",
                                data: postData,
                                success: function (data, textStatus, jqXHR)
                                {
                                    $("#result").html(data);
                                },
                                error: function (jqXHR, textStatus, errorThrown)
                                {
                                    $("#result").html(errorThrown);
                                }
                            });
                    return false;
                });
            });
        </script>

        <title>My First Rest Action Bean (with jQuery!)</title>
        <style type="text/css">
            input.error { background-color: yellow; }
        </style>
    </head>
    <body>
        <h1>Stripes Calculator using RestActionBean</h1>

        Hi, I'm the Stripes Calculator. I can only do addition. Maybe, some day, a nice programmer
        will come along and teach me how to do other things?

        <form id="calculatorForm" action="/examples/calculate" focus="" method="post">
            <table>
                <tr>
                    <td>Number 1:</td>
                    <td><input type="text" name="numberOne"/></td>
                </tr>
                <tr>
                    <td>Number 2:</td>
                    <td><input type="text" name="numberTwo"/></td>
                </tr>
                <tr>
                    <td colspan="2">
                        <input type="submit" value="Add"/>
                    </td>
                </tr>
                <tr>
                    <td>Result:</td>
                    <td id="result">${actionBean.result}</td>
                </tr>
            </table>
        </form>
        <p><a href="../index.html"><< Back To Example Listing</a></p>
    </body>
</html>