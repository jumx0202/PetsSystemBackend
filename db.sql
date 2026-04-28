show DATABASES;
CREATE DATABASE petSql;
show databases;
USE petSql;

-- 用户表
CREATE TABLE User (
                      use_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID，主键，自增',
                      username VARCHAR(255) NOT NULL UNIQUE COMMENT '用户昵称，唯一',
                      phone CHAR(11) NOT NULL UNIQUE COMMENT '手机号，唯一，11位',
                      password VARCHAR(255) NOT NULL COMMENT '密码，加密',
                      avatar VARCHAR(500) DEFAULT 'https://api.dicebear.com/7.x/avataaars/svg?seed=default' COMMENT '头像URL',
                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                      updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 软删除：不执行物理DELETE，将is_active设置为0。数据不会丢失；保留历史数据；避免外键级联删引发的连锁反应
                      is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用'
) COMMENT='系统用户表';

DROP TABLE User;

show TABLES;
-- 查看表里的结构
DESC User;

SELECT * FROM User;

INSERT INTO User (username, phone, password) VALUES ('summe', 15576747556, 123456);

-- 2. 宠物档案表 (pets) - 用于行为监测的宠物
CREATE TABLE Pet (
                     id INT PRIMARY KEY AUTO_INCREMENT COMMENT '宠物ID',
                     owner_id INT NOT NULL COMMENT '主人ID',
                     pet_name VARCHAR(50) NOT NULL COMMENT '宠物名字',
                     pet_type TINYINT NOT NULL COMMENT '宠物类型：0-狗 1-猫 2-其他',
                     breed VARCHAR(100) COMMENT '品种',
                     gender VARCHAR(10) COMMENT '性别：公/母/不详',
                     birth_date DATE COMMENT '出生日期',
                     weight DECIMAL(5,2) COMMENT '体重(kg)',
                     color VARCHAR(50) COMMENT '毛色',
                     distinctive_features VARCHAR(500) COMMENT '显著特征',
                     chip_number VARCHAR(50) UNIQUE COMMENT '芯片编号',
                     avatar VARCHAR(500) COMMENT '宠物头像',
                     health_status VARCHAR(200) COMMENT '健康状况',
                     is_neutered TINYINT(1) DEFAULT 0 COMMENT '是否绝育：0-否 1-是',
                     created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                     updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                     FOREIGN KEY (owner_id) REFERENCES User(user_id) ON DELETE CASCADE
) COMMENT='宠物档案表';

DESC Pet;
DROP TABLE Pet;

-- 领养贴子表
CREATE TABLE PostAdoption (
                              postadoption_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '领养帖子ID',
                              postadoption_publisher_id INT NOT NULL COMMENT '发领养贴者id，与user_id相同',
                              gender VARCHAR(10) NOT NULL COMMENT '性别：公/母/不详',
                              breed VARCHAR(100) NOT NULL COMMENT '品种',
                              city VARCHAR(50) NOT NULL COMMENT '所在城市',
                              district VARCHAR(255) NOT NULL COMMENT '详细地址',
                              contact_name VARCHAR(50) NOT NULL COMMENT '联系人昵称',
                              contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
                              contact_wechat VARCHAR(100) COMMENT '微信号，可填可不填',
                              description TEXT COMMENT '描述',
                              status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1-寻找中 2-已找到',
    -- 不确定做不做
                              view_count INT DEFAULT 0 COMMENT '浏览次数',
                              created_at_postadoption DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                              updated_at_postadoption DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    -- 不确定做不做
                              adopted_at DATETIME COMMENT '被领养时间',
    -- 不确定做不做
                              adopter_id INT COMMENT '领养人id',
                              FOREIGN KEY (postadoption_publisher_id) REFERENCES User(user_id) ON DELETE CASCADE ,
                              FOREIGN KEY (adopter_id) REFERENCES User(user_id),
    -- 根据城市名、帖子状态、发布时间建立索引，加速查询
                              INDEX idx_city (city),
                              INDEX idx_status (status),
                              INDEX idx_created_at (created_at_postadoption)
) COMMENT '领养帖子表';

DESC PostAdoption;
DROP TABLE PostAdoption;

-- 寻宠帖子表
CREATE TABLE PostLost (
                          postlost_id INT PRIMARY KEY AUTO_INCREMENT COMMENT '寻宠帖子id',
                          postlost_publisher_id INT NOT NULL COMMENT '发寻宠贴者id，与user_id相同',
                          lostpet_id INT COMMENT '关联的宠物档案id（可选）',
                          lostpet_name VARCHAR(50) NOT NULL COMMENT '丢失宠物名称',
                          gender VARCHAR(10) NOT NULL COMMENT '性别',
                          breed VARCHAR(100) NOT NULL COMMENT '品种',
    -- 丢失时间手动输入是VARCHAR，自己选择时间格式是什么？
                          lost_time VARCHAR(100) NOT NULL COMMENT '丢失时间',
                          lost_location VARCHAR(300) NOT NULL COMMENT '丢失详细地点',
                          lostcity VARCHAR(50) NOT NULL COMMENT '所在城市',
                          contact_name VARCHAR(50) NOT NULL COMMENT '联系人',
                          contact_phone VARCHAR(20) NOT NULL COMMENT '联系电话',
                          contact_wechat VARCHAR(100) COMMENT '微信号，可填可不填',
                          description TEXT COMMENT '描述',
                          status TINYINT NOT NULL DEFAULT 1 COMMENT '帖子状态：1-寻找中 2-已找到',
                          view_count INT DEFAULT 0 COMMENT '浏览次数',
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                          updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                          found_at DATETIME COMMENT '找到时间',
                          FOREIGN KEY (postlost_publisher_id) REFERENCES User(user_id) ON DELETE CASCADE,
                          FOREIGN KEY (lostpet_id) REFERENCES Pet(id) ON DELETE SET NULL,
                          INDEX idx_city (lostcity),
                          INDEX idx_status (status),
                          INDEX idx_lost_time (lost_time),
                          INDEX idx_created_at (created_at)
) COMMENT '寻宠帖子表';

DESC PostLost;
DROP TABLE PostLost;

-- 帖子图片表
CREATE TABLE Image (
                       id INT PRIMARY KEY AUTO_INCREMENT COMMENT '图片id',
                       image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    -- 控制帖子图片的展示顺序
                       sort_order INT DEFAULT 0 COMMENT '排序顺序',
                       postadoption_id INT COMMENT '关联领养帖子id',
                       postlost_id INT COMMENT '关联寻宠帖子id',
                       pet_id INT COMMENT '关联宠物档案id',
                       behavior_log_id INT COMMENT '关联行为监测记录id',
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                       FOREIGN KEY (postadoption_id) REFERENCES PostAdoption(postadoption_id) ON DELETE CASCADE,
                       FOREIGN KEY (postlost_id) REFERENCES PostLost(postlost_id) ON DELETE CASCADE,
                       FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE,
                       INDEX idx_postadoption (postadoption_id),
                       INDEX idx_postlost (postlost_id),
                       INDEX idx_pet (pet_id)
) COMMENT '帖子图片表';

DESC Image;
DROP TABLE Image;

-- 6. 行为分析模型表 (behavior_models)
CREATE TABLE behavior_models (
                                 id INT PRIMARY KEY AUTO_INCREMENT COMMENT '模型ID',
                                 model_name VARCHAR(100) NOT NULL COMMENT '模型名称',
                                 model_version VARCHAR(20) NOT NULL COMMENT '模型版本',
                                 model_type TINYINT NOT NULL COMMENT '模型类型：1-情绪识别 2-行为分类 3-健康监测 4-异常检测',
                                 model_file_path VARCHAR(500) NOT NULL COMMENT '模型文件路径',
                                 model_config JSON COMMENT '模型配置参数',
                                 accuracy DECIMAL(5,2) COMMENT '准确率(%)',
                                 description TEXT COMMENT '模型描述',
                                 is_active TINYINT(1) DEFAULT 1 COMMENT '是否启用：0-禁用 1-启用',
                                 created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI行为分析模型表';

DESC behavior_models;
DROP TABLE behavior_models;

-- 7. 行为监测记录表 (behavior_logs)
CREATE TABLE behavior_logs (
                               id INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                               pet_id INT NOT NULL COMMENT '宠物ID',
                               model_id INT COMMENT '使用的模型ID',
                               record_time DATETIME NOT NULL COMMENT '记录时间',
                               record_type TINYINT NOT NULL COMMENT '记录类型：1-视频 2-音频 3-传感器数据',
                               file_path VARCHAR(500) COMMENT '原始文件路径',
                               behavior_type VARCHAR(50) COMMENT '识别出的行为类型',
                               emotion_type VARCHAR(50) COMMENT '情绪类型：开心/焦虑/恐惧/平静等',
                               confidence DECIMAL(5,2) COMMENT '置信度(%)',
                               analysis_result JSON COMMENT '详细分析结果',
                               health_score INT COMMENT '健康评分(0-100)',
                               abnormal_alert TINYINT(1) DEFAULT 0 COMMENT '是否异常：0-正常 1-异常',
                               alert_level TINYINT COMMENT '警报级别：1-提示 2-警告 3-紧急',
                               alert_message VARCHAR(500) COMMENT '警报信息',
                               processed_by INT COMMENT '处理人员ID',
                               processed_at DATETIME COMMENT '处理时间',
                               status TINYINT DEFAULT 1 COMMENT '状态：1-未处理 2-处理中 3-已处理',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE,
                               FOREIGN KEY (model_id) REFERENCES behavior_models(id),
                               INDEX idx_pet_time (pet_id, record_time),
                               INDEX idx_abnormal (abnormal_alert, alert_level),
                               INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物行为监测记录表';

DESC behavior_logs;

-- 8. 宠物定位记录表 (pet_location) - 用于实时定位和轨迹回放
CREATE TABLE pet_location (
                              id INT PRIMARY KEY AUTO_INCREMENT COMMENT '定位记录ID',
                              pet_id INT NOT NULL COMMENT '宠物ID',
                              latitude DECIMAL(10,7) NOT NULL COMMENT '纬度',
                              longitude DECIMAL(10,7) NOT NULL COMMENT '经度',
                              speed FLOAT DEFAULT 0 COMMENT '速度(km/h)',
                              source VARCHAR(20) DEFAULT 'MOCK' COMMENT '来源：MOCK/GPS/MANUAL',
                              recorded_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上报时间',
                              FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE,
                              INDEX idx_pet_recorded_at (pet_id, recorded_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='宠物定位记录表';

DESC pet_location;
DROP TABLE pet_location;

-- 9. 行为统计日报表 (behavior_daily_stats) - 用于趋势分析
CREATE TABLE behavior_daily_stats (
                                      id INT PRIMARY KEY AUTO_INCREMENT,
                                      pet_id INT NOT NULL COMMENT '宠物ID',
                                      stats_date DATE NOT NULL COMMENT '统计日期',
                                      total_records INT DEFAULT 0 COMMENT '总记录数',
                                      active_hours DECIMAL(4,1) COMMENT '活跃时长(小时)',
                                      rest_hours DECIMAL(4,1) COMMENT '休息时长(小时)',
                                      emotion_happy_count INT DEFAULT 0 COMMENT '开心次数',
                                      emotion_anxious_count INT DEFAULT 0 COMMENT '焦虑次数',
                                      emotion_calm_count INT DEFAULT 0 COMMENT '平静次数',
                                      abnormal_count INT DEFAULT 0 COMMENT '异常次数',
                                      avg_health_score INT COMMENT '平均健康分',
                                      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                      UNIQUE KEY uk_pet_date (pet_id, stats_date),
                                      FOREIGN KEY (pet_id) REFERENCES Pet(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为统计日报表';

DESC behavior_daily_stats;

-- 10. 收藏/关注表 (favorites)
CREATE TABLE favorites (
                           id INT PRIMARY KEY AUTO_INCREMENT,
                           user_id INT NOT NULL COMMENT '用户ID',
                           target_type TINYINT NOT NULL COMMENT '目标类型：1-领养帖子 2-寻宠帖子',
                           target_id INT NOT NULL COMMENT '目标ID',
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           UNIQUE KEY uk_user_target (user_id, target_type, target_id),
                           FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收藏关注表';

DESC favorites;

-- 10. 消息通知表 (notifications)
CREATE TABLE notifications (
                               id INT PRIMARY KEY AUTO_INCREMENT,
                               user_id INT NOT NULL COMMENT '接收用户ID',
                               notify_type TINYINT NOT NULL COMMENT '通知类型：1-系统 2-领养回复 3-寻宠线索 4-行为异常',
                               title VARCHAR(100) NOT NULL COMMENT '标题',
                               content TEXT COMMENT '内容',
                               related_type TINYINT COMMENT '关联类型：1-领养帖子 2-寻宠帖子 3-行为记录',
                               related_id INT COMMENT '关联ID',
                               is_read TINYINT(1) DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
                               created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                               read_at DATETIME COMMENT '阅读时间',
                               FOREIGN KEY (user_id) REFERENCES User(user_id) ON DELETE CASCADE,
                               INDEX idx_user_read (user_id, is_read),
                               INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息通知表';

DESC notifications;
