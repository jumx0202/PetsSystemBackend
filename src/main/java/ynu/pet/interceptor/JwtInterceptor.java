package ynu.pet.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import ynu.pet.dto.Result;
import ynu.pet.exception.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import ynu.pet.utils.JwtUtil;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        // 放行 CORS 预检请求，避免浏览器因非 2xx 预检失败而拦截正式请求
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String uri = request.getRequestURI();
        if (uri.equals("/api/test/ping")) {
            return true;  // 不校验 Token
        }
        // 帖子详情允许游客访问
        if (HttpMethod.GET.matches(request.getMethod())
                && (uri.matches("^/api/adoption/\\d+$") || uri.matches("^/api/lost/\\d+$"))) {
            return true;
        }

        String token = request.getHeader("Authorization");

        if (token == null || !token.startsWith("Bearer ")) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        token = token.substring(7);
        Long userId = JwtUtil.parseToken(token);

        if (userId == null) {
            writeErrorResponse(response, ResultCode.UNAUTHORIZED);
            return false;
        }

        // 将userId放入请求属性
        request.setAttribute("userId", userId);
        return true;
    }



    private void writeErrorResponse(HttpServletResponse response, ResultCode code) throws Exception {
        response.setStatus(code.getCode());
        response.setContentType("application/json;charset=UTF-8");
        Result<Void> result = Result.error(code);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}