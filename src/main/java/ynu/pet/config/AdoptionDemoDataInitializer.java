package ynu.pet.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ynu.pet.dto.AdoptionPostDTO;
import ynu.pet.entity.User;
import ynu.pet.mapper.AdoptionPostMapper;
import ynu.pet.mapper.ImageMapper;
import ynu.pet.mapper.UserMapper;
import ynu.pet.service.AdoptionPostService;

import java.util.List;

@Component
public class AdoptionDemoDataInitializer {

    @Autowired
    private AdoptionPostMapper adoptionPostMapper;

    @Autowired
    private AdoptionPostService adoptionPostService;

    @Autowired
    private ImageMapper imageMapper;

    @Autowired
    private UserMapper userMapper;

    private static final List<String> REMOVED_DEMO_CONTACT_PHONES = List.of(
            "133-0000-0007",
            "132-0000-0008",
            "131-0000-0009",
            "130-0000-0010"
    );

    @PostConstruct
    public void init() {
        cleanupRemovedDemoPosts();
        seedPost(
                ensurePublisher("13810000001", "Zhang", "https://api.dicebear.com/7.x/avataaars/svg?seed=zhang"),
                "Male", "Terrier & Labrador Retriever", "Beijing", "Chaoyang District",
                "Zhang", "138-0000-0001", "zhang_pet",
                "Friendly and vaccinated, suitable for a family that can provide regular outdoor exercise and patient companionship.",
                "/hamster-pet.svg"
        );
        seedPost(
                ensurePublisher("13810000002", "Li", "https://api.dicebear.com/7.x/avataaars/svg?seed=li"),
                "Female", "Golden Retriever", "Shanghai", "Pudong",
                "Li", "139-0000-0002", null,
                "One and a half years old, gentle and already neutered. Looking for a caring home with enough space and daily interaction.",
                "/bird-pet.svg"
        );
        seedPost(
                ensurePublisher("13810000003", "Wang", "https://api.dicebear.com/7.x/avataaars/svg?seed=wang"),
                "Male", "Alaskan Malamute", "Guangzhou", "Tianhe District",
                "Wang", "137-0000-0003", "wang_alaska",
                "Two years old, strong and energetic. Needs an adopter with large space and experience caring for active medium-to-large dogs.",
                "/snake-pet.svg"
        );
        seedPost(
                ensurePublisher("13810000004", "Chen", "https://api.dicebear.com/7.x/avataaars/svg?seed=chen"),
                "Female", "Ragdoll Cat", "Shenzhen", "Nanshan District",
                "Chen", "136-0000-0004", null,
                "Indoor cat with a calm temperament. Adoption includes litter box and basic supplies, best for a stable and quiet home.",
                "/duck-pet.svg"
        );
        seedPost(
                ensurePublisher("13810000005", "Liu", "https://api.dicebear.com/7.x/avataaars/svg?seed=liu"),
                "Unknown", "Corgi", "Hangzhou", "Xihu District",
                "Liu", "135-0000-0005", "liu_corgi",
                "Short-legged and lively, completed basic vaccination. Suitable for a home that can provide regular walks and play time.",
                "/default-pet.svg"
        );
        seedPost(
                ensurePublisher("13810000006", "Zhao", "https://api.dicebear.com/7.x/avataaars/svg?seed=zhao"),
                "Female", "Samoyed", "Chengdu", "Jinjiang District",
                "Zhao", "134-0000-0006", null,
                "Three years old with a very stable personality. Needs an adopter who can brush and care for a long white coat regularly.",
                "/default-pet.svg"
        );
    }

    private void cleanupRemovedDemoPosts() {
        List<Long> postIds = adoptionPostMapper.selectIdsByContactPhones(REMOVED_DEMO_CONTACT_PHONES);
        if (!postIds.isEmpty()) {
            imageMapper.deleteByAdoptionPostIds(postIds);
            adoptionPostMapper.deleteByContactPhones(REMOVED_DEMO_CONTACT_PHONES);
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

    private void seedPost(Long publisherId, String gender, String breed, String city, String district,
                          String contactName, String contactPhone, String contactWechat,
                          String description, String imageUrl) {
        if (adoptionPostMapper.countByContactPhone(contactPhone) > 0) {
            adoptionPostMapper.updatePublisherByContactPhone(contactPhone, publisherId);
            return;
        }

        AdoptionPostDTO dto = new AdoptionPostDTO();
        dto.setGender(gender);
        dto.setBreed(breed);
        dto.setCity(city);
        dto.setDistrict(district);
        dto.setContactName(contactName);
        dto.setContactPhone(contactPhone);
        dto.setContactWechat(contactWechat);
        dto.setDescription(description);
        dto.setImages(List.of(imageUrl));
        adoptionPostService.createPost(dto, publisherId);
    }
}
