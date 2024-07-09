package com.viettel.importwiz.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class BeanInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {
        if (isInstanceOfHandlerMethod(handler) && HttpMethod.valueOf(request.getMethod()) != HttpMethod.OPTIONS) {
            request.getSession().setAttribute("BEAN_CLASS",
                ((HandlerMethod) handler).getBeanType().getName());
            request.getSession().setAttribute("BEAN_METHOD",
                ((HandlerMethod) handler).getMethod().getName());
        }

        return true;
    }

    public boolean isInstanceOfHandlerMethod(Object handler) {
        String nameHandlerMethodClass = HandlerMethod.class.getName();
        return handler.getClass().getName().equals(nameHandlerMethodClass);
    }
}
