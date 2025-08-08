-- =============================================
-- Blog和Category数据库设计
-- =============================================

-- 分类表
CREATE TABLE `tb_category` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `description` varchar(200) DEFAULT NULL COMMENT '分类描述',
  `sort_order` int DEFAULT 0 COMMENT '排序字段',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name` (`name`) COMMENT '分类名称唯一索引'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客分类表';

-- 博客表
CREATE TABLE `tb_blog` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '博客ID',
  `title` varchar(200) NOT NULL COMMENT '博客标题',
  `content` longtext COMMENT '博客内容（Markdown格式）',
  `summary` varchar(500) DEFAULT NULL COMMENT '博客摘要',
  `category_id` int DEFAULT NULL COMMENT '分类ID',
  `author_id` int NOT NULL COMMENT '作者ID',
  `status` varchar(20) DEFAULT 'draft' COMMENT '状态：draft-草稿，published-已发布',
  `view_count` int DEFAULT 0 COMMENT '浏览次数',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签（JSON格式）',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`) COMMENT '分类索引',
  KEY `idx_author_id` (`author_id`) COMMENT '作者索引',
  KEY `idx_status` (`status`) COMMENT '状态索引',
  KEY `idx_create_time` (`create_time`) COMMENT '创建时间索引',
  CONSTRAINT `fk_blog_category` FOREIGN KEY (`category_id`) REFERENCES `tb_category` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_blog_author` FOREIGN KEY (`author_id`) REFERENCES `tb_user` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='博客文章表';

-- 插入示例数据
INSERT INTO `tb_category` (`name`, `description`, `sort_order`) VALUES
('技术分享', '技术相关的文章分享', 1),
('学习笔记', '学习过程中的笔记和总结', 2),
('项目经验', '项目开发中的经验和心得', 3),
('生活随笔', '生活中的感悟和随笔', 4);

-- 插入示例博客（假设用户ID为3）
INSERT INTO `tb_blog` (`title`, `content`, `summary`, `category_id`, `author_id`, `status`) VALUES
('Vue3 组合式API详解', '# Vue3 组合式API详解\n\n## 什么是组合式API？\n\n组合式API是Vue3中新增的一种编写组件逻辑的方式...', '详细介绍Vue3组合式API的使用方法和最佳实践', 1, 3, 'published'),
('Spring Boot 项目实战', '# Spring Boot 项目实战\n\n## 项目背景\n\n本项目是一个基于Spring Boot的博客系统...', '分享Spring Boot项目开发过程中的经验和技巧', 3, 3, 'published'),
('我的第一篇博客', '# 我的第一篇博客\n\n这是一个测试博客，用于验证系统功能...', '测试博客内容', 2, 3, 'draft'); 