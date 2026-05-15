package ynu.pet.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ynu.pet.dto.LostPostDTO;
import ynu.pet.entity.User;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.LostPostMapper;
import ynu.pet.mapper.UserMapper;
import ynu.pet.service.LostPostService;

import java.util.List;

@Component
public class LostDemoDataInitializer {

    @Autowired
    private LostPostMapper lostPostMapper;

    @Autowired
    private LostPostService lostPostService;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private UserMapper userMapper;

    private static final List<String> REMOVED_DEMO_CONTACT_PHONES = List.of(
            "136-0000-0004",
            "135-0000-0005",
            "134-0000-0006",
            "133-0000-0007"
    );

    @PostConstruct
    public void init() {
        cleanupRemovedDemoPosts();
        seedPost(
                ensurePublisher("13820000001", "Sarah Johnson", "https://api.dicebear.com/7.x/avataaars/svg?seed=sarah"),
                "Buddy", "Male", "Golden Retriever",
                "2026-04-10 15:00", "New York", "Central Park, near the fountain area",
                "Sarah Johnson", "138-0000-0001", "sarah_pet2024",
                "Golden retriever with a red collar, very friendly and responds to the name Buddy. Has a small white patch on the left ear.",
                "/hamster-pet.svg"
        );
        seedPost(
                ensurePublisher("13820000002", "Emily Chen", "https://api.dicebear.com/7.x/avataaars/svg?seed=emily"),
                "Milo", "Male", "Tabby Cat",
                "2026-04-12 08:30", "Brooklyn", "Heights, Community Garden",
                "Emily Chen", "139-0000-0002", null,
                "Orange tabby cat, very shy, has a bell on the collar. Answers to Milo and may hide in small spaces.",
                "/bird-pet.svg"
        );
        seedPost(
                ensurePublisher("13820000003", "Michael Brown", "https://api.dicebear.com/7.x/avataaars/svg?seed=michael"),
                "Snowy", "Female", "Toy Poodle",
                "2026-04-11 18:00", "New York", "Upper West Side, Riverside Park",
                "Michael Brown", "137-0000-0003", "mike_brown88",
                "Small white poodle, elderly with slight vision problems. Wearing a blue harness and responds to Snowy.",
                "/snake-pet.svg"
        );
    }

    private void cleanupRemovedDemoPosts() {
        List<Long> postIds = lostPostMapper.selectIdsByContactPhones(REMOVED_DEMO_CONTACT_PHONES);
        if (!postIds.isEmpty()) {
            imageMapper.deleteByLostPostIds(postIds);
            lostPostMapper.deleteByContactPhones(REMOVED_DEMO_CONTACT_PHONES);
        }
    }

    private Long ensurePublisher(String phone, String username, String avatar) {
        User user = userMapper.findByPhone(phone);
        if (user == null) {
            user = new User();
            user.setPhone(phone);
            user.setPassword("123456");
        }

        user.setUsername(username);
        user.setAvatar(avatar);

        if (user.getId() == null) {
            userMapper.insert(user);
        } else {
            userMapper.update(user);
        }
        return user.getId();
    }

    private void seedPost(Long publisherId, String petName, String gender, String breed,
                          String lostTime, String city, String lostLocation,
                          String contactName, String contactPhone, String contactWechat,
                          String description, String imageUrl) {
        if (lostPostMapper.countByContactPhone(contactPhone) > 0) {
            lostPostMapper.updatePublisherByContactPhone(contactPhone, publisherId);
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
