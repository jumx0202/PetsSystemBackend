package ynu.pet.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


@Data
public class PetDTO {
    private Long id;
    private String petName;
    private Integer petType;
    private String petTypeDesc;
    private String breed;
    private String gender;
    private LocalDate birthDate;
    private Integer age;
    private BigDecimal weight;
    private String color;
    private String distinctiveFeatures;
    private String chipNumber;
    private String avatar;
    private List<String> images;
}