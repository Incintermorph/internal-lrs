
package com.example.lrs.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Order(1)
public class BasicAuthFilter implements Filter {

    @Value("${lrs.auth.basic.username:admin}")
    private String user;

    @Value("${lrs.auth.basic.password:admin}")
    private String pass;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        if (!req.getRequestURI().startsWith("/xapi/")) {
            chain.doFilter(request, response);
            return;
        }

        String ver = req.getHeader("X-Experience-API-Version");
        if (ver == null || !ver.startsWith("1.0")) {
            res.sendError(400, "Missing or invalid X-Experience-API-Version");
            return;
        }

        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Basic ")) {
            res.setHeader("WWW-Authenticate", "Basic realm=\"Internal LRS\"");
            res.sendError(401, "Unauthorized");
            return;
        }
        String decoded = new String(Base64.getDecoder().decode(auth.substring(6)), StandardCharsets.UTF_8);
        String[] parts = decoded.split(":", 2);
        if (parts.length != 2 || !parts[0].equals(user) || !parts[1].equals(pass)) {
            res.sendError(401, "Unauthorized");
            return;
        }
        chain.doFilter(request, response);
    }
}
