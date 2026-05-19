-- PetFace 2.0 闭环功能增量 SQL
-- 适用场景：
-- 1. 已有数据库升级：执行本文件即可补齐 PetFace 个体识别和寻宠匹配记录表结构。
-- 2. 新设备部署：先执行 init.sql，再执行本文件。
--
-- 注意：项目当前宠物表名为 Pet，主键 Pet.id 类型为 INT。

USE petSql;

CREATE TABLE IF NOT EXISTS pet_face_embedding (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  pet_id INT NOT NULL,
  image_url VARCHAR(500),
  image_urls MEDIUMTEXT,
  image_count INT NOT NULL DEFAULT 1,
  model_version VARCHAR(100) NOT NULL DEFAULT 'PetFace-ID-2.0',
  embedding_dim INT NOT NULL DEFAULT 512,
  embedding_json MEDIUMTEXT NOT NULL,
  embedding_items_json LONGTEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_pet_face_pet_id (pet_id),
  INDEX idx_pet_face_model_version (model_version),
  CONSTRAINT fk_pet_face_pet
    FOREIGN KEY (pet_id) REFERENCES Pet(id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物个体识别特征表';

CREATE TABLE IF NOT EXISTS pet_match_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  lost_post_id INT,
  query_image_url VARCHAR(500),
  matched_pet_id INT,
  similarity DOUBLE NOT NULL,
  confidence_level VARCHAR(20) DEFAULT 'low',
  model_version VARCHAR(100) NOT NULL DEFAULT 'PetFace-ID-2.0',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_match_lost_post_id (lost_post_id),
  INDEX idx_match_pet_id (matched_pet_id),
  INDEX idx_match_similarity (similarity),
  CONSTRAINT fk_pet_match_lost_post
    FOREIGN KEY (lost_post_id) REFERENCES PostLost(postlost_id)
    ON DELETE CASCADE,
  CONSTRAINT fk_pet_match_pet
    FOREIGN KEY (matched_pet_id) REFERENCES Pet(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物相似匹配记录表';

DELIMITER //

CREATE PROCEDURE petface_add_column_if_missing(
  IN table_name_in VARCHAR(64),
  IN column_name_in VARCHAR(64),
  IN ddl_in TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name_in
      AND COLUMN_NAME = column_name_in
  ) THEN
    SET @ddl = ddl_in;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //

CREATE PROCEDURE petface_add_index_if_missing(
  IN table_name_in VARCHAR(64),
  IN index_name_in VARCHAR(64),
  IN ddl_in TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name_in
      AND INDEX_NAME = index_name_in
  ) THEN
    SET @ddl = ddl_in;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //

CREATE PROCEDURE petface_add_fk_if_missing(
  IN table_name_in VARCHAR(64),
  IN constraint_name_in VARCHAR(64),
  IN ddl_in TEXT
)
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME = table_name_in
      AND CONSTRAINT_NAME = constraint_name_in
  ) THEN
    SET @ddl = ddl_in;
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END //

DELIMITER ;

CALL petface_add_column_if_missing(
  'pet_face_embedding',
  'image_urls',
  'ALTER TABLE pet_face_embedding ADD COLUMN image_urls MEDIUMTEXT NULL AFTER image_url'
);

CALL petface_add_column_if_missing(
  'pet_face_embedding',
  'image_count',
  'ALTER TABLE pet_face_embedding ADD COLUMN image_count INT NOT NULL DEFAULT 1 AFTER image_urls'
);

CALL petface_add_column_if_missing(
  'pet_face_embedding',
  'embedding_items_json',
  'ALTER TABLE pet_face_embedding ADD COLUMN embedding_items_json LONGTEXT NULL AFTER embedding_json'
);

CALL petface_add_column_if_missing(
  'pet_match_record',
  'lost_post_id',
  'ALTER TABLE pet_match_record ADD COLUMN lost_post_id INT NULL AFTER id'
);

CALL petface_add_column_if_missing(
  'pet_match_record',
  'confidence_level',
  'ALTER TABLE pet_match_record ADD COLUMN confidence_level VARCHAR(20) DEFAULT ''low'' AFTER similarity'
);

CALL petface_add_index_if_missing(
  'pet_face_embedding',
  'uk_pet_face_pet_id',
  'ALTER TABLE pet_face_embedding ADD UNIQUE KEY uk_pet_face_pet_id (pet_id)'
);

CALL petface_add_index_if_missing(
  'pet_match_record',
  'idx_match_lost_post_id',
  'ALTER TABLE pet_match_record ADD INDEX idx_match_lost_post_id (lost_post_id)'
);

CALL petface_add_index_if_missing(
  'pet_match_record',
  'idx_match_pet_id',
  'ALTER TABLE pet_match_record ADD INDEX idx_match_pet_id (matched_pet_id)'
);

CALL petface_add_index_if_missing(
  'pet_match_record',
  'idx_match_similarity',
  'ALTER TABLE pet_match_record ADD INDEX idx_match_similarity (similarity)'
);

CALL petface_add_fk_if_missing(
  'pet_face_embedding',
  'fk_pet_face_pet',
  'ALTER TABLE pet_face_embedding ADD CONSTRAINT fk_pet_face_pet FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE'
);

CALL petface_add_fk_if_missing(
  'pet_match_record',
  'fk_pet_match_lost_post',
  'ALTER TABLE pet_match_record ADD CONSTRAINT fk_pet_match_lost_post FOREIGN KEY (lost_post_id) REFERENCES PostLost(postlost_id) ON DELETE CASCADE'
);

CALL petface_add_fk_if_missing(
  'pet_match_record',
  'fk_pet_match_pet',
  'ALTER TABLE pet_match_record ADD CONSTRAINT fk_pet_match_pet FOREIGN KEY (matched_pet_id) REFERENCES Pet(id) ON DELETE SET NULL'
);

DROP PROCEDURE petface_add_column_if_missing;
DROP PROCEDURE petface_add_index_if_missing;
DROP PROCEDURE petface_add_fk_if_missing;

DESC pet_face_embedding;
DESC pet_match_record;
