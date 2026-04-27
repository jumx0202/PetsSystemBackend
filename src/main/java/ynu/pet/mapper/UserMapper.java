package ynu.pet.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import ynu.pet.entity.User;

@Mapper
@Repository
public interface UserMapper {
    Long selectFirstUserId();

    User findByPhone(String phone);

    User selectById(Long id);

    void insert(User user);

    void update(User user);
}