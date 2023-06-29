package com.taiji.opcuabackend.interceptor;

import com.auth0.jwt.exceptions.AlgorithmMismatchException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taiji.opcuabackend.entity.AjaxResult;
import com.taiji.opcuabackend.util.JwtUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author sunzb
 * @date 2023/6/7 17:56
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        AjaxResult ar = null;
        //获取请求头中令牌
//        String token = request.getHeader("token");
        String token = request.getParameter("token");
        if (token == null) {
            //令牌为空，响应json数据
            ar = new AjaxResult(401, "请先获取令牌!");
            //将map转为json
            String json = new ObjectMapper().writeValueAsString(ar);
            // 相应json数据
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().println(json);
            return false;
        }
        try {
            //验证令牌
            JwtUtils.verifyToken(token);
            //验证成功，放行请求
            return true;
        } catch (SignatureVerificationException e) {
            e.printStackTrace();
            ar = new AjaxResult(401, "无效签名!");
        } catch (TokenExpiredException e) {
            e.printStackTrace();
            ar = new AjaxResult(401, "token过期!");
        } catch (AlgorithmMismatchException e) {
            e.printStackTrace();
            ar = new AjaxResult(401, "token算法不一致!");
        } catch (Exception e) {
            e.printStackTrace();
            ar = new AjaxResult(401, "token无效!");

        }
        //将map转为json
        String json = new ObjectMapper().writeValueAsString(ar);
        // 相应json数据
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().println(json);
        return false;
    }
}
