package com.viettel.importwiz.security.vsa;

import lombok.extern.slf4j.Slf4j;
import viettel.passport.client.ObjectToken;
import viettel.passport.client.UserToken;
import viettel.passport.util.Connector;
import viettel.passport.util.VsaFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Iterator;

@Slf4j
public class CustomVsaFilter extends VsaFilter {
    private static final HashSet<String> allMenuURL = new HashSet();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException,
        ServletException {

        log.info("Custom VSA filter");
        HttpServletRequest req = null;
        HttpServletResponse res = null;
        if (request instanceof HttpServletRequest) {
            req = (HttpServletRequest) request;
        }

        if (response instanceof HttpServletResponse) {
            res = (HttpServletResponse) response;
        }
        if (req != null && res != null) {
            Connector cnn = new Connector(req, res);

            if (this.alowURL(req.getRequestURI(), Connector.getAllowedUrls())) {
                chain.doFilter(req, res);
                // To only authenticate when go to "/auth/login"
            } else if (req.getRequestURI().contains("/auth/oauthVsa/home")) {
                if (cnn.hadTicket()) {
                    log.info("Had ticket");
                    if (!cnn.getAuthenticate()) {
                        res.sendRedirect(Connector.getErrorUrl());
                    } else {
                        chain.doFilter(request, response);
                    }
                } else {
                    log.info("No ticket");
                    res.sendRedirect(
                        cnn.getPassportLoginURL() + "?appCode=" + cnn.getDomainCode() + "&service="
                            + URLEncoder.encode(cnn.getServiceURL(),
                            String.valueOf(StandardCharsets.UTF_8)));
                }
            } else if (allMenuURL.contains(req.getServletPath())) {
                if (this.getVsaAllowedServletPath(req).contains(req.getServletPath())) {
                    chain.doFilter(request, response);
                } else {
                    res.sendRedirect(Connector.getErrorUrl());
                }
            } else {
                chain.doFilter(request, response);
            }
        }
    }

    // Typo form original Lib - Don't fix
    private Boolean alowURL(String url, String[] listAlowUrl) {
        @SuppressWarnings("UnnecessaryLocalVariable")
        String[] arr$ = listAlowUrl;
        int len$ = listAlowUrl.length;

        for (int i$ = 0; i$ < len$; ++i$) {
            String str = arr$[i$];
            if (url.equalsIgnoreCase(str)) {
                return true;
            }
        }

        return false;
    }

    private HashSet<String> getVsaAllowedServletPath(HttpServletRequest request) {
        UserToken vsaUserToken =
            (UserToken) request.getSession().getAttribute("vsaUserToken");
        HashSet<String> vsaAllowedURL = new HashSet();
        Iterator i$ = vsaUserToken.getObjectTokens().iterator();

        while (i$.hasNext()) {
            ObjectToken ot = (ObjectToken) i$.next();
            String servletPath = ot.getObjectUrl();
            if (!"#".equals(servletPath)) {
                vsaAllowedURL.add(servletPath.split("\\?")[0]);
            }
        }

        return vsaAllowedURL;
    }
}
