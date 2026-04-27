import lombok.Data;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Data
public class test {
    @RestController
    @RequestMapping("/api/test")
    @CrossOrigin(origins = "http://localhost:5173")  // 前端地址
    public class TestController {

        @GetMapping("/ping")
        public Map<String, Object> ping() {
            Map<String, Object> result = new HashMap<>();
            result.put("code", 200);
            result.put("message", "前后端连接成功！");
            result.put("time", new Date());
            return result;
        }
    }
}
