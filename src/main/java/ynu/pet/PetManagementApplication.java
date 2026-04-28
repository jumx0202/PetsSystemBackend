package ynu.pet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("ynu.pet.mapper")
@EnableTransactionManagement
@EnableScheduling
public class PetManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetManagementApplication.class, args);
    }
}