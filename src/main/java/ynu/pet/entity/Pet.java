package ynu.pet.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;

import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

@Entity
@Data
@Table(name = "Pet")
public class Pet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "pet_name", nullable = false, length = 50)
    private String petName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "pet_type", nullable = false)
    private PetType petType;  // DOG=1, CAT=2, OTHER=3

    @Column(length = 100)
    private String breed;

    @Column(length = 10)
    private String gender;

    @Column(name = "birth_date")
    private LocalDate birthDate;

    private BigDecimal weight;

    @Column(length = 50)
    private String color;

    @Column(name = "distinctive_features", length = 500)
    private String distinctiveFeatures;

    @Column(name = "chip_number", unique = true, length = 50)
    private String chipNumber;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<BehaviorLog> behaviorLogs;

    /**
     * 计算宠物年龄（岁）
     * @return 年龄，如果出生日期为空则返回null
     */
    public Integer getAge() {
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
    }

    // ========== 宠物类型枚举 ==========
    @Getter
    public enum PetType {
        DOG(1, "狗"),
        CAT(2, "猫"),
        OTHER(3, "其他");

        private final int value;
        private final String description;

        PetType(int value, String description) {
            this.value = value;
            this.description = description;
        }
    }
}