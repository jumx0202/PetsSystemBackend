package ynu.pet.controller;

import org.springframework.web.bind.annotation.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController              // ✅ 必须有
@RequestMapping("/api/test") // ✅ 基础路径
@CrossOrigin(origins = "http://localhost:5173")  // 允许前端跨域
public class TestController {

    @GetMapping("/ping")     // ✅ 完整路径: /api/test/ping
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("message", "前后端连接成功！");
        result.put("time", new Date());
        return result;
    }
}