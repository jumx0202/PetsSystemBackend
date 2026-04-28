USE petSql;

ALTER TABLE Pet
    MODIFY COLUMN pet_type TINYINT NOT NULL COMMENT '类型：0-狗 1-猫 2-其他',
    MODIFY COLUMN gender VARCHAR(10) NULL COMMENT '性别：公/母/不详',
    MODIFY COLUMN avatar VARCHAR(500) NULL COMMENT '宠物头像';

CREATE TABLE IF NOT EXISTS pet_location (
    id          INT           PRIMARY KEY AUTO_INCREMENT COMMENT '定位记录ID',
    pet_id      INT           NOT NULL               COMMENT '宠物ID',
    latitude    DECIMAL(10,7) NOT NULL               COMMENT '纬度',
    longitude   DECIMAL(10,7) NOT NULL               COMMENT '经度',
    speed       FLOAT         DEFAULT 0              COMMENT '速度(km/h)',
    source      VARCHAR(20)   DEFAULT 'MOCK'         COMMENT '来源：MOCK/GPS/MANUAL',
    recorded_at DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
    FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE,
    INDEX idx_pet_recorded_at (pet_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物定位记录表';
