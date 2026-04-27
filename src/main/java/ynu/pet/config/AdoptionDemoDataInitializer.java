package ynu.pet.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ynu.pet.dto.AdoptionPostDTO;
import ynu.pet.entity.User;
import ynu.pet.mapper.AdoptionPostMapper;
import ynu.pet.mapper.UserMapper;
import ynu.pet.service.AdoptionPostService;

import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class AdoptionDemoDataInitializer {
    private static final String DEMO_PHONE = "13900009999";
    private static final String DEMO_USERNAME = "系统演示账号";
    private static final String DEMO_AVATAR = "https://api.dicebear.com/7.x/avataaars/svg?seed=demo";

    @Autowired
    private AdoptionPostMapper adoptionPostMapper;

    @Autowired
    private AdoptionPostService adoptionPostService;

    @Autowired
    private UserMapper userMapper;

    @PostConstruct
    public void init() {
        Long publisherId = ensurePublisher();
        reassignDemoPostsToDemoUser(publisherId);
        seedPost(publisherId, "公", "Terrier & Labrador Retriever", "北京", "朝阳区",
                "张先生", "138-0000-0001", "zhang_pet",
                "活泼亲人，已打疫苗，性格温顺，适合有小孩家庭。这是一只非常可爱的狗狗，喜欢在草地上奔跑，对人非常友好，是家庭的理想选择。",
                "https://images.unsplash.com/photo-1587300003388-59208cc962cb?w=400&h=400&fit=crop");
        seedPost(publisherId, "母", "金毛寻回犬", "上海", "浦东新区",
                "李女士", "139-0000-0002", null,
                "一岁半，非常友好，已绝育，寻找爱心家庭。金毛犬性格温和，是家庭的理想伴侣，特别适合有小孩的家庭。",
                "https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?w=400&h=400&fit=crop");
        seedPost(publisherId, "公", "阿拉斯加", "广州", "天河区",
                "王先生", "137-0000-0003", "wang_alaska",
                "两岁，巨型雪橇犬，习惯良好，需要大空间。阿拉斯加犬精力充沛，需要经常运动，适合有院子的家庭。",
                "https://images.unsplash.com/photo-1583511655857-d19bc40da7e6?w=400&h=400&fit=crop");
        seedPost(publisherId, "母", "布偶猫", "深圳", "南山区",
                "陈女士", "136-0000-0004", null,
                "温柔粘人，纯种布偶，已驱虫，送猫砂盆。布偶猫是理想的室内宠物，性格温顺，喜欢被人抱在怀里。",
                "https://images.unsplash.com/photo-1513245543132-31f507417b26?w=400&h=400&fit=crop");
        seedPost(publisherId, "不详", "柯基", "杭州", "西湖区",
                "刘先生", "135-0000-0005", "liu_corgi",
                "短腿萌犬，精力旺盛，已完成疫苗接种。柯基犬虽然腿短，但非常活泼可爱，是网红犬种。",
                "https://images.unsplash.com/photo-1537151608828-ea2b11777ee8?w=400&h=400&fit=crop");
        seedPost(publisherId, "母", "萨摩耶", "成都", "锦江区",
                "赵女士", "134-0000-0006", null,
                "微笑天使，三岁，性格乖巧，寻找有经验主人。萨摩耶犬有着美丽的白色毛发，需要定期梳理。",
                "https://images.unsplash.com/photo-1518791841217-8f162f1e1131?w=400&h=400&fit=crop");
    }

    private void reassignDemoPostsToDemoUser(Long publisherId) {
        adoptionPostMapper.updatePublisherByContactPhone("138-0000-0001", publisherId);
        adoptionPostMapper.updatePublisherByContactPhone("139-0000-0002", publisherId);
        adoptionPostMapper.updatePublisherByContactPhone("137-0000-0003", publisherId);
        adoptionPostMapper.updatePublisherByContactPhone("136-0000-0004", publisherId);
        adoptionPostMapper.updatePublisherByContactPhone("135-0000-0005", publisherId);
        adoptionPostMapper.updatePublisherByContactPhone("134-0000-0006", publisherId);
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

    private void seedPost(Long publisherId, String gender, String breed, String city, String district,
                          String contactName, String contactPhone, String contactWechat,
                          String description, String imageUrl) {
        if (adoptionPostMapper.countByContactPhone(contactPhone) > 0) {
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
