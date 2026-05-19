-- PetFace 2.0 多图建档增量 SQL
-- 用途：在已有 pet_face_embedding 表上补齐多图融合特征所需字段。

USE petSql;

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

DROP PROCEDURE petface_add_column_if_missing;

UPDATE pet_face_embedding
SET image_count = 1
WHERE image_count IS NULL OR image_count < 1;
