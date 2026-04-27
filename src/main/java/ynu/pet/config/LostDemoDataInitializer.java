package ynu.pet.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ynu.pet.dto.LostPostDTO;
import ynu.pet.entity.User;
import ynu.pet.mapper.LostPostMapper;
import ynu.pet.mapper.UserMapper;
import ynu.pet.service.LostPostService;

import java.util.List;

@Component
public class LostDemoDataInitializer {
    private static final String DEMO_PHONE = "13900009999";
    private static final String DEMO_USERNAME = "系统演示账号";
    private static final String DEMO_AVATAR = "https://api.dicebear.com/7.x/avataaars/svg?seed=demo";

    @Autowired
    private LostPostMapper lostPostMapper;

    @Autowired
    private LostPostService lostPostService;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        Long publisherId = ensurePublisher();
        reassignDemoPostsToDemoUser(publisherId);
        seedPost(publisherId, "Buddy", "公", "Golden Retriever",
                "2026-04-10 15:00", "New York", "Central Park, near the fountain area",
                "Sarah Johnson", "138-0000-0001", "sarah_pet2024",
                "Golden retriever with a red collar, very friendly and responds to the name \"Buddy\". Has a small white patch on the left ear. Very playful and loves to fetch balls. Last seen wearing a blue harness.",
                "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=400&fit=crop");
        seedPost(publisherId, "Milo", "公", "Tabby Cat",
                "2026-04-12 08:30", "Brooklyn", "Heights, Community Garden",
                "Emily Chen", "139-0000-0002", null,
                "Orange tabby cat, very shy, has a bell on the collar. Answers to \"Milo\". Last seen near the community garden. He is afraid of loud noises and may hide in small spaces. Please approach slowly.",
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=400&fit=crop");
        seedPost(publisherId, "Snowy", "母", "Toy Poodle",
                "2026-04-11 18:00", "New York", "Upper West Side, Riverside Park",
                "Michael Brown", "137-0000-0003", "mike_brown88",
                "Small white poodle, elderly with slight vision problems. Wearing a blue harness. Very gentle but may be frightened. She responds to \"Snowy\" and loves treats. Please check under cars and in shaded areas.",
                "https://images.unsplash.com/photo-1583511655857-d19bc40da7e6?w=400&h=400&fit=crop");
    }

    private void reassignDemoPostsToDemoUser(Long publisherId) {
        lostPostMapper.updatePublisherByContactPhone("138-0000-0001", publisherId);
        lostPostMapper.updatePublisherByContactPhone("139-0000-0002", publisherId);
        lostPostMapper.updatePublisherByContactPhone("137-0000-0003", publisherId);
    }

    private Long ensurePublisher() {
        User demoUser = userMapper.findByPhone(DEMO_PHONE);
        if (demoUser != null && demoUser.getId() != null) {
            return demoUser.getId();
        }

        User user = new User();
        user.setUsername(DEMO_USERNAME);
        user.setPhone(DEMO_PHONE);
        user.setPassword("123456");
        user.setAvatar(DEMO_AVATAR);
        userMapper.insert(user);
        return user.getId();
    }

    private void seedPost(Long publisherId, String petName, String gender, String breed,
                          String lostTime, String city, String lostLocation,
                          String contactName, String contactPhone, String contactWechat,
                          String description, String imageUrl) {
        if (lostPostMapper.countByContactPhone(contactPhone) > 0) {
            return;
        }

        LostPostDTO dto = new LostPostDTO();
        dto.setPetName(petName);
        dto.setGender(gender);
        dto.setBreed(breed);
        dto.setLostTime(lostTime);
        dto.setCity(city);
        dto.setLostLocation(lostLocation);
        dto.setDistrict(lostLocation);
        dto.setContactName(contactName);
        dto.setContactPhone(contactPhone);
        dto.setContactWechat(contactWechat);
        dto.setDescription(description);
        dto.setImages(List.of(imageUrl));
        lostPostService.createPost(dto, publisherId);
    }
}
