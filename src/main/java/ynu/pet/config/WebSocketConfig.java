package ynu.pet.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类
 *
 * <p>使用 STOMP 协议实现发布/订阅模式的实时消息推送。
 * 前端通过 SockJS 连接 /ws 端点，订阅 /topic/pet/{petId}/location 频道
 * 即可实时接收对应宠物的位置更新。</p>
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                // 允许跨域（前端 Vue 开发服务器地址）
                .setAllowedOriginPatterns("*")
                // 开启 SockJS 降级支持，在不支持 WebSocket 的环境下回退到 HTTP 长轮询
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用内存消息代理，处理以 /topic 开头的订阅目的地
        registry.enableSimpleBroker("/topic");
        // 客户端向服务器发送消息时，目的地前缀为 /app
        registry.setApplicationDestinationPrefixes("/app");
    }
}
