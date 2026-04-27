package ynu.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@SpringBootApplication
@MapperScan("ynu.pet.mapper")
@EnableTransactionManagement
public class PetManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetManagementApplication.class, args);
    }
}