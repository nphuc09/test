package com.viettel.importwiz.util;

import com.viettel.importwiz.context.SpringApplicationContext;
import com.viettel.importwiz.security.jwt.AJwtTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;


public class LogUtil {

    protected static final Logger logger = LoggerFactory.getLogger(LogUtil.class);
    static String SYS_NAME = "VNM_IT_IMPORT_WIZ";
    static String serviceCode = "VNM_IT_IMPORT_WIZ";

    static AJwtTokenUtil jwtTokenUtil = SpringApplicationContext.bean(AJwtTokenUtil.class);

    public static void setSysName(String sysName) {
        SYS_NAME = sysName;
    }

    public static void setServiceCode(String serviceCode) {
        LogUtil.serviceCode = serviceCode;
    }

    public static void info(HttpServletRequest request, String className, Date startTime, Date endTime, String actionName, String actionType, String requestBody, String account, Object param, String responseContent, String dataType, int numberOfRecord, String vsa, String dataExtend) {

        String startTimeStr = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(startTime);
        String endTimeStr = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(endTime);
        String userName = null;
        StringBuilder pattern = new StringBuilder("{0}");
        for (int i = 1; i < 33; i++) {
            pattern.append("|{").append(i).append("}");
        }
        try {
            String sessionId = null;
            String authorization = request.getHeader("Authorization");
            if (authorization != null && authorization.startsWith("Bearer ")) authorization = authorization.substring(7);
            if (authorization != null) {
                try {
                    userName = jwtTokenUtil.getUsernameFromToken(authorization);
                    Map<String, Object> claims = jwtTokenUtil.getClaimsMapFromToken(authorization);
                    String startTime2 = (String) claims.get("auth_token_session_create");
                    String jti = (String) claims.get("jti");
                    sessionId = userName + "_" + jti + "_" + startTime2;
                } catch (Exception e) {
                    logger.debug(e.getMessage(), e);
                }
            }
            String ipClient = getRemoteHost(request);
            String systemIpAddress = getSystemIpAddress();
            String path = request.getServerName() + ":" + request.getServerPort() + "_" + systemIpAddress + ":";
            String uri = request.getRequestURI();
            String requestId = request.getRequestedSessionId();
            if (requestId == null) {
                try {
                    requestId = request.getSession(true).getId();
                } catch (IllegalStateException e) {
                    logger.debug(e.getMessage(), e);
                }
            }
            String errorCode = "";
            String errorDesc = "";
            String transactionStatus = "0";
            String transactionType = "01";
            String logEnd = MessageFormat.format(pattern.toString(), SYS_NAME, serviceCode, serviceCode.split("_")[serviceCode.split("_").length - 1], requestId, sessionId, request.getServerName() + "," + request.getServerPort(), systemIpAddress + "," + request.getServerPort(), requestBody, startTimeStr, endTimeStr, (endTime.getTime() - startTime.getTime()) + "", errorCode, errorDesc, transactionStatus, actionName, userName, account, Thread.currentThread().getName(), className, "0", actionName, request.getRequestedSessionId(), className, ipClient, sessionId, responseContent, transactionType, SYS_NAME, actionType, dataType, numberOfRecord + "", vsa, dataExtend);
            logger.info(logEnd);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static String getRemoteHost(HttpServletRequest request) {
        String[] HEADERS_LIST = {"X-Forwarded-For", "Proxy-Client-IP", "WL-Proxy-Client-IP", "HTTP_X_FORWARDED_FOR", "HTTP_X_FORWARDED", "HTTP_X_CLUSTER_CLIENT_IP", "HTTP_CLIENT_IP", "HTTP_FORWARDED_FOR", "HTTP_FORWARDED", "HTTP_VIA", "REMOTE_ADDR"};
        Enumeration<String> headerNames = request.getHeaderNames();

        if (headerNames != null) {
            while (headerNames.hasMoreElements()) {
                String element = headerNames.nextElement();
                if ("singularityheader".equals(element)) continue;
            }
        }

        for (String header : HEADERS_LIST) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip;
            }
        }
        return request.getRemoteAddr();
    }

    public static String getSystemIpAddress() {
        InetAddress addr = null;
        try {
            addr = InetAddress.getLocalHost();
            // OK - is this the loopback addr ?
            if (!addr.isLoopbackAddress()) {
                return addr.getHostAddress();
            }
            // plan B - enumerate the network interfaces
            Enumeration ifaces = NetworkInterface.getNetworkInterfaces();
            while (ifaces.hasMoreElements()) {
                NetworkInterface netIf = (NetworkInterface) ifaces.nextElement();
                Enumeration addrs = netIf.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    addr = (InetAddress) addrs.nextElement();
                    if (addr instanceof Inet6Address) {
                        // probably not what we want - keep looking
                        continue;
                    }
                    // chose (arbitrarily?) first non-loopback addr
                    if (!addr.isLoopbackAddress()) {
                        return addr.getHostAddress();
                    }
                }
            }
            // nothing so far - last resort
            return "";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return "";
    }

}
