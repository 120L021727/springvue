-- 聊天消息表
CREATE TABLE `tb_chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `content` text NOT NULL COMMENT '消息内容',
  `type` varchar(20) NOT NULL COMMENT '消息类型：TEXT, SYSTEM, JOIN, LEAVE',
  `chat_type` varchar(20) NOT NULL COMMENT '聊天类型：PUBLIC, PRIVATE',
  `sender_id` int DEFAULT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) DEFAULT NULL COMMENT '发送者用户名',
  `receiver_id` int DEFAULT NULL COMMENT '接收者ID（私聊时使用）',
  `room_id` varchar(50) DEFAULT NULL COMMENT '房间ID（公共聊天时使用）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_chat_type_room` (`chat_type`, `room_id`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_chat_message_sender` FOREIGN KEY (`sender_id`) REFERENCES `tb_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_chat_message_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `tb_user` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息表';

-- 创建索引以优化查询性能
-- 公共房间消息查询索引
CREATE INDEX `idx_public_messages` ON `tb_chat_message` (`chat_type`, `room_id`, `create_time` DESC);

-- 私聊消息查询索引
CREATE INDEX `idx_private_messages` ON `tb_chat_message` (`chat_type`, `sender_id`, `receiver_id`, `create_time` DESC);

-- 插入一些示例数据（可选）
INSERT INTO `tb_chat_message` (`content`, `type`, `chat_type`, `sender_id`, `sender_name`, `room_id`, `create_time`) 
VALUES 
('欢迎来到聊天室！', 'SYSTEM', 'PUBLIC', NULL, 'System', 'public', NOW()),
('聊天室已准备就绪，开始聊天吧！', 'SYSTEM', 'PUBLIC', NULL, 'System', 'public', NOW());
