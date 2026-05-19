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
