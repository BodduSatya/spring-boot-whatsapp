<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>SpringBoot Mail</title>
    <style type="text/css">
        /* General styles */
        body {
            margin: 0;
            padding: 0;
            font-family: Arial, Helvetica, sans-serif;
            line-height: 1.6;
            background-color: #f0f0f0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        /* Header styles */
        .header {
            background-color: #838383;
            padding: 20px 0;
        }
        .header-content {
            width: 600px;
            margin: 0 auto;
            text-align: center;
        }
        .header-title {
            font-size: 48px;
            color: blue;
        }
        .header-subtitle {
            font-size: 24px;
            color: #555100;
            margin-top: 20px;
        }
        /* Body styles */
        .body-content {
            background-color: #d3be6c;
            padding: 0 15px 10px 15px;
            color: #000000;
            font-size: 13px;
        }
        .body-text {
            font-size: 16px;
            line-height: 1.8;
        }
        .body-link {
            color: #007bff;
            text-decoration: none;
        }
        /* Footer styles */
        .footer {
            background-color: #838383;
            padding: 20px 0;
            text-align: center;
            color: #ffffff;
        }
    </style>
</head>
<body>
<table class="header" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td class="header-content">
            <div class="header-title"><b>SpringBoot</b></div>
            <div class="header-subtitle">Sending Email using Spring Boot with <b>FreeMarker template!!!</b></div>
        </td>
    </tr>
</table>
<table class="body-content" width="600" border="0" cellspacing="0" cellpadding="0" align="center">
    <tr>
        <td>
            <div class="body-text">
                <p>Spring Boot makes it easy to create stand-alone, production-grade Spring-based Applications that you can "just run".</p>
                <p>We take an opinionated view of the Spring platform and third-party libraries so you can get started with minimum fuss. Most Spring Boot applications need minimal Spring configuration.</p>
                <p>If you’re looking for information about a specific version, or instructions about how to upgrade from an earlier release, check out the project <a class="body-link" href="https://spring.io/projects/spring-boot">release notes section on our wiki</a>.</p>
                <br>
                <p><b>${name}</b><br>${location}</p>
            </div>
        </td>
    </tr>
</table>
<table class="footer" border="0" cellspacing="0" cellpadding="0">
    <tr>
        <td>
            &copy; 2024 SpringBoot. All rights reserved.
        </td>
    </tr>
</table>
</body>
</html>
