-- MySQL dump 10.13  Distrib 8.0.29, for Win64 (x86_64)
--
-- Host: localhost    Database: soldier
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `academic_performance`
--

DROP TABLE IF EXISTS `academic_performance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `academic_performance` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `semester` varchar(20) NOT NULL,
  `gpa` decimal(3,2) DEFAULT NULL,
  `scholarship` varchar(100) DEFAULT NULL,
  `student_leader` varchar(100) DEFAULT NULL,
  `activity_participation` varchar(200) DEFAULT NULL,
  `points_awarded` int NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_student_semester` (`student_id`,`semester`),
  CONSTRAINT `fk_ap_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='在校学业表现';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `academic_performance`
--

LOCK TABLES `academic_performance` WRITE;
/*!40000 ALTER TABLE `academic_performance` DISABLE KEYS */;
/*!40000 ALTER TABLE `academic_performance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin_user`
--

DROP TABLE IF EXISTS `admin_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin_user` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL COMMENT 'bcrypt加密',
  `last_login` datetime DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='管理员';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin_user`
--

LOCK TABLES `admin_user` WRITE;
/*!40000 ALTER TABLE `admin_user` DISABLE KEYS */;
INSERT INTO `admin_user` VALUES (1,'1','1','2026-05-16 18:30:53',0,'2026-05-16 18:30:57'),(2,'admin','$2a$10$u2PY9lP143jir6.I6pmL1OOzNb9n3kau8CtCZeJbSV0O.kT1TjP5.','2026-07-20 16:30:56',0,'2026-05-01 18:42:25');
/*!40000 ALTER TABLE `admin_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_analysis_result`
--

DROP TABLE IF EXISTS `ai_analysis_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_analysis_result` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `ability_tags` json DEFAULT NULL COMMENT '能力标签列表',
  `ability_scores` json DEFAULT NULL COMMENT '能力评分',
  `point_level` char(1) DEFAULT NULL COMMENT '积分等级 A/B/C/D',
  `recommended_jobs` json DEFAULT NULL COMMENT '推荐岗位',
  `overall_comment` text COMMENT '综合评语',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  CONSTRAINT `fk_ai_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='AI分析结果缓存';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_analysis_result`
--

LOCK TABLES `ai_analysis_result` WRITE;
/*!40000 ALTER TABLE `ai_analysis_result` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_analysis_result` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `announcement`
--

DROP TABLE IF EXISTS `announcement`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `announcement` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) NOT NULL COMMENT '公告标题',
  `content` text NOT NULL COMMENT '公告内容',
  `priority` int DEFAULT '0' COMMENT '优先级 0:普通 1:重要 2:紧急',
  `status` tinyint DEFAULT '0' COMMENT '状态 0:草稿 1:已发布 2:关闭',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint DEFAULT '0' COMMENT '逻辑删除 0:未删 1:已删',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公告表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `announcement`
--

LOCK TABLES `announcement` WRITE;
/*!40000 ALTER TABLE `announcement` DISABLE KEYS */;
INSERT INTO `announcement` VALUES (1,'欢迎来到戎归·星辉','欢迎您来到戎归·星辉——大学生退役军人综合服务平台！在这里，您可以查看就业实习机会、参与培训课程、了解政策福利、记录服役荣誉，并获得专属积分。让我们携手共进，再创辉煌！',1,1,'2026-07-08 13:14:15','2026-07-08 13:14:15',0),(2,'平台研发测试通知','平台目前处于研发测试阶段，部分功能仍在完善中，如遇问题请及时联系管理员反馈，感谢您的理解与支持！',1,1,'2026-07-08 13:57:28','2026-07-08 13:57:28',0);
/*!40000 ALTER TABLE `announcement` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_branch`
--

DROP TABLE IF EXISTS `dict_branch`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_branch` (
  `code` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='兵种字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_branch`
--

LOCK TABLES `dict_branch` WRITE;
/*!40000 ALTER TABLE `dict_branch` DISABLE KEYS */;
INSERT INTO `dict_branch` VALUES ('AIR','空军',3),('ARMED_POLICE','武警部队',9),('ARMY','陆军',1),('CYBER_FORCE','网络空间部队',6),('INFO_SUPPORT','信息支援部队',7),('JOINT_LOGISTICS','联勤保障部队',8),('NAVY','海军',2),('ROCKET','火箭军',4),('SPACE_FORCE','军事航天部队',5);
/*!40000 ALTER TABLE `dict_branch` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_cert`
--

DROP TABLE IF EXISTS `dict_cert`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_cert` (
  `code` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `default_points` int NOT NULL DEFAULT '0',
  `validity_months` int DEFAULT NULL COMMENT '有效期月数，NULL表示长期',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='证书类型字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_cert`
--

LOCK TABLES `dict_cert` WRITE;
/*!40000 ALTER TABLE `dict_cert` DISABLE KEYS */;
INSERT INTO `dict_cert` VALUES ('CHINESE_CULINARY','中式烹调师',15,NULL),('COMPUTER_LEVEL','计算机等级',20,NULL),('DRIVER_C1','驾驶证C1',15,NULL),('ELECTRICIAN','电工证',15,NULL),('ENGLISH_LEVEL','英语等级',20,NULL),('FIRE_OPERATOR','消防设施操作员',25,NULL),('FIRST_AID','急救证',10,36),('LEGAL_PROFESSION','法律职业资格证',30,NULL),('MILITARY_INSTRUCTOR','军训教官证',5,NULL),('PRIMARY_ACCOUNTANT','初级会计职称',25,NULL),('PSYCHOLOGIST','心理咨询师',25,NULL),('TEACHER_CERT','教师资格证',25,NULL),('UAV_CAAC','无人机CAAC执照',20,NULL);
/*!40000 ALTER TABLE `dict_cert` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_honor`
--

DROP TABLE IF EXISTS `dict_honor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_honor` (
  `code` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `default_points` int NOT NULL DEFAULT '0',
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='荣誉字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_honor`
--

LOCK TABLES `dict_honor` WRITE;
/*!40000 ALTER TABLE `dict_honor` DISABLE KEYS */;
INSERT INTO `dict_honor` VALUES ('COMMENDATION','嘉奖',10,5),('EXCELLENT_SOLDIER','优秀士兵（含嘉奖）',20,4),('FIRST_MERIT','一等功',100,1),('SECOND_MERIT','二等功',60,2),('THIRD_MERIT','三等功',30,3);
/*!40000 ALTER TABLE `dict_honor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_honor_category`
--

DROP TABLE IF EXISTS `dict_honor_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_honor_category` (
  `code` varchar(30) NOT NULL,
  `name` varchar(50) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='荣誉类别字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_honor_category`
--

LOCK TABLES `dict_honor_category` WRITE;
/*!40000 ALTER TABLE `dict_honor_category` DISABLE KEYS */;
INSERT INTO `dict_honor_category` VALUES ('COMBAT_READINESS','战备训练',1),('EDUCATION_MGMT','教育管理',2),('NATIONAL_DEFENSE_TECH','国防科技',3),('SERVICE_SUPPORT','服务保障',4);
/*!40000 ALTER TABLE `dict_honor_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_job_type`
--

DROP TABLE IF EXISTS `dict_job_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_job_type` (
  `code` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='就业意向岗位字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_job_type`
--

LOCK TABLES `dict_job_type` WRITE;
/*!40000 ALTER TABLE `dict_job_type` DISABLE KEYS */;
INSERT INTO `dict_job_type` VALUES ('GRASSROOTS_CIVIL','基层公务员'),('MILITARY_CIVIL','部队文职'),('PUBLIC_INST','事业单位'),('STARTUP','自主创业'),('STATE_OWNED','国企');
/*!40000 ALTER TABLE `dict_job_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_post_type`
--

DROP TABLE IF EXISTS `dict_post_type`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_post_type` (
  `code` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务意向岗位字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_post_type`
--

LOCK TABLES `dict_post_type` WRITE;
/*!40000 ALTER TABLE `dict_post_type` DISABLE KEYS */;
INSERT INTO `dict_post_type` VALUES ('CAMPUS_DUTY','校园执勤'),('COMMUNITY_SVC','社区服务'),('EMERGENCY','应急救援'),('RECRUIT_PROM','征兵宣传'),('TRAINING_INST','军训带训');
/*!40000 ALTER TABLE `dict_post_type` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `dict_leader_post`
--

DROP TABLE IF EXISTS `dict_leader_post`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dict_leader_post` (
  `code` varchar(30) NOT NULL,
  `name` varchar(100) NOT NULL,
  `sort_order` int NOT NULL DEFAULT '0',
  PRIMARY KEY (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服役期间骨干职务字典';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `dict_leader_post`
--

LOCK TABLES `dict_leader_post` WRITE;
/*!40000 ALTER TABLE `dict_leader_post` DISABLE KEYS */;
INSERT INTO `dict_leader_post` VALUES ('ORDINARY','否，普通士兵',3),('OTHER_BACKBONE','是，担任其他骨干（通讯员、文书、训练骨干、团支部骨干等）',2),('SQUAD_LEADER','是，担任班长（含副班长）',1);
/*!40000 ALTER TABLE `dict_leader_post` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `military_honor`
--

DROP TABLE IF EXISTS `military_honor`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `military_honor` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `service_experience_id` bigint unsigned DEFAULT NULL,
  `honor_code` varchar(20) NOT NULL,
  `honor_category_code` varchar(30) DEFAULT NULL COMMENT '荣誉类别代码(dict_honor_category)',
  `award_date` date DEFAULT NULL,
  `points_awarded` int NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `status` tinyint DEFAULT '0' COMMENT '0-待审 1-通过 2-驳回',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  PRIMARY KEY (`id`),
  KEY `idx_service_exp_id` (`service_experience_id`),
  KEY `fk_mh_honor` (`honor_code`),
  KEY `idx_mh_status` (`status`),
  CONSTRAINT `fk_mh_honor` FOREIGN KEY (`honor_code`) REFERENCES `dict_honor` (`code`) ON DELETE RESTRICT,
  CONSTRAINT `fk_mh_se` FOREIGN KEY (`service_experience_id`) REFERENCES `service_experience` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服役荣誉';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `military_honor`
--

LOCK TABLES `military_honor` WRITE;
/*!40000 ALTER TABLE `military_honor` DISABLE KEYS */;
/*!40000 ALTER TABLE `military_honor` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `military_position`
--

DROP TABLE IF EXISTS `military_position`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `military_position` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `service_experience_id` bigint unsigned NOT NULL,
  `position_name` varchar(100) NOT NULL,
  `duty_start` date DEFAULT NULL,
  `duty_end` date DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  KEY `idx_service_exp_id` (`service_experience_id`),
  CONSTRAINT `fk_mp_se` FOREIGN KEY (`service_experience_id`) REFERENCES `service_experience` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服役期间职务';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `military_position`
--

LOCK TABLES `military_position` WRITE;
/*!40000 ALTER TABLE `military_position` DISABLE KEYS */;
/*!40000 ALTER TABLE `military_position` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opportunity`
--

DROP TABLE IF EXISTS `opportunity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opportunity` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `title` varchar(200) NOT NULL,
  `type` varchar(50) NOT NULL COMMENT '宣讲会/民兵招募/专场招聘/志愿服务',
  `description` text COMMENT '描述(支持HTML)',
  `publisher_id` bigint unsigned NOT NULL,
  `unit_name` varchar(100) DEFAULT NULL,
  `start_time` datetime DEFAULT NULL,
  `end_time` datetime DEFAULT NULL,
  `demand_count` int DEFAULT NULL,
  `current_applied` int NOT NULL DEFAULT '0',
  `requirements` text COMMENT '文本描述要求',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审 1已发布 2已结束',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `cover_url` varchar(500) DEFAULT NULL COMMENT '封面图',
  `address` varchar(200) DEFAULT NULL COMMENT '地址/地点',
  `salary_range` varchar(50) DEFAULT NULL COMMENT '薪资范围',
  `contact_name` varchar(50) DEFAULT NULL COMMENT '联系人',
  `contact_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `tags` varchar(500) DEFAULT NULL COMMENT '标签(JSON数组)',
  `priority` int DEFAULT '0' COMMENT '优先级(越大越靠前)',
  `view_count` int DEFAULT '0' COMMENT '浏览次数',
  PRIMARY KEY (`id`),
  KEY `idx_status_time` (`status`,`end_time`),
  KEY `fk_opp_publisher` (`publisher_id`),
  CONSTRAINT `fk_opp_publisher` FOREIGN KEY (`publisher_id`) REFERENCES `admin_user` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机会/活动';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opportunity`
--

LOCK TABLES `opportunity` WRITE;
/*!40000 ALTER TABLE `opportunity` DISABLE KEYS */;
INSERT INTO `opportunity` VALUES (1,'demo1','training','demo1',2,'demo1',NULL,NULL,0,0,'demo1',1,0,'2026-07-07 22:08:29','2026-07-07 22:08:29','','','','','','',0,2),(2,'demo2','job','',2,'demo2',NULL,NULL,0,0,'',0,0,'2026-07-07 22:20:19','2026-07-07 22:20:19','','','','','','',0,0),(3,'demo3','job','demo3',2,'demo3',NULL,NULL,0,0,'demo3',1,0,'2026-07-07 22:27:58','2026-07-07 22:27:58','/uploads/covers/af3855f149d34ae1b767033d15961625.png','','','','','',0,6);
/*!40000 ALTER TABLE `opportunity` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opportunity_application`
--

DROP TABLE IF EXISTS `opportunity_application`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opportunity_application` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `opportunity_id` bigint unsigned NOT NULL,
  `student_id` bigint unsigned NOT NULL,
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0已报名 1已取消 2已录用',
  `remark` varchar(200) DEFAULT NULL,
  `student_name` varchar(50) DEFAULT NULL COMMENT '报名时学生姓名',
  `student_student_id` varchar(50) DEFAULT NULL COMMENT '报名时学号',
  `student_phone` varchar(20) DEFAULT NULL COMMENT '报名时手机号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_opp_student_status` (`opportunity_id`,`student_id`,`status`),
  KEY `idx_student_id` (`student_id`),
  CONSTRAINT `fk_oa_opportunity` FOREIGN KEY (`opportunity_id`) REFERENCES `opportunity` (`id`) ON DELETE RESTRICT,
  CONSTRAINT `fk_oa_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='报名记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opportunity_application`
--

LOCK TABLES `opportunity_application` WRITE;
/*!40000 ALTER TABLE `opportunity_application` DISABLE KEYS */;
/*!40000 ALTER TABLE `opportunity_application` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `opportunity_favorite`
--

DROP TABLE IF EXISTS `opportunity_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `opportunity_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `student_id` bigint NOT NULL COMMENT '学生ID',
  `opportunity_id` bigint NOT NULL COMMENT '机会ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '收藏时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_opp` (`student_id`,`opportunity_id`),
  KEY `idx_opportunity_id` (`opportunity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='机会收藏';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `opportunity_favorite`
--

LOCK TABLES `opportunity_favorite` WRITE;
/*!40000 ALTER TABLE `opportunity_favorite` DISABLE KEYS */;
/*!40000 ALTER TABLE `opportunity_favorite` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `points_detail`
--

DROP TABLE IF EXISTS `points_detail`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `points_detail` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `change_value` int NOT NULL,
  `reason_type` varchar(30) NOT NULL COMMENT 'HONOR/CERT/SERVICE/ACADEMIC',
  `related_id` bigint DEFAULT NULL,
  `source_table` varchar(50) DEFAULT NULL,
  `operator` varchar(50) DEFAULT NULL,
  `remark` varchar(200) DEFAULT NULL,
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student_time` (`student_id`,`create_time`),
  CONSTRAINT `fk_pd_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='积分变动明细';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `points_detail`
--

LOCK TABLES `points_detail` WRITE;
/*!40000 ALTER TABLE `points_detail` DISABLE KEYS */;
/*!40000 ALTER TABLE `points_detail` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_record`
--

DROP TABLE IF EXISTS `resume_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL COMMENT '学生ID',
  `template_code` varchar(30) NOT NULL COMMENT '使用的模板编码',
  `pdf_path` varchar(255) DEFAULT NULL COMMENT '生成的PDF文件物理路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(byte)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0生成中 1完成 2失败',
  `content_hash` varchar(64) DEFAULT NULL COMMENT '内容哈希',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student_template` (`student_id`,`template_code`),
  KEY `idx_hash` (`content_hash`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简历生成记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_record`
--

LOCK TABLES `resume_record` WRITE;
/*!40000 ALTER TABLE `resume_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `resume_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `resume_template`
--

DROP TABLE IF EXISTS `resume_template`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `resume_template` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '模板名称',
  `code` varchar(30) NOT NULL COMMENT '模板编码',
  `thumbnail` varchar(255) DEFAULT NULL COMMENT '缩略图URL',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '0禁用 1启用',
  `sort` int DEFAULT '0' COMMENT '排序',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='简历模板';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `resume_template`
--

LOCK TABLES `resume_template` WRITE;
/*!40000 ALTER TABLE `resume_template` DISABLE KEYS */;
INSERT INTO `resume_template` VALUES (1,'军事风','military',NULL,1,1,'2026-07-20 23:25:43','2026-07-20 23:25:43'),(2,'简约风','simple',NULL,1,2,'2026-07-20 23:25:43','2026-07-20 23:25:43'),(3,'政务风','government',NULL,1,3,'2026-07-20 23:25:43','2026-07-20 23:25:43');
/*!40000 ALTER TABLE `resume_template` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `service_experience`
--

DROP TABLE IF EXISTS `service_experience`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `service_experience` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `branch_code` varchar(20) NOT NULL,
  `start_date` date NOT NULL,
  `end_date` date NOT NULL,
  `leader_post_code` varchar(30) DEFAULT NULL COMMENT '服役期间职务代码(dict_leader_post)',
  `service_years` decimal(4,1) GENERATED ALWAYS AS ((timestampdiff(MONTH,`start_date`,`end_date`) / 12)) STORED COMMENT '服役年限',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `status` tinyint DEFAULT '0' COMMENT '0-待审 1-通过 2-驳回',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `fk_se_branch` (`branch_code`),
  KEY `idx_se_status` (`status`),
  CONSTRAINT `fk_se_branch` FOREIGN KEY (`branch_code`) REFERENCES `dict_branch` (`code`) ON DELETE RESTRICT,
  CONSTRAINT `fk_se_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服役经历';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `service_experience`
--

LOCK TABLES `service_experience` WRITE;
/*!40000 ALTER TABLE `service_experience` DISABLE KEYS */;
/*!40000 ALTER TABLE `service_experience` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `social_service_record`
--

DROP TABLE IF EXISTS `social_service_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `social_service_record` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `activity_type` varchar(50) NOT NULL,
  `duration_hours` decimal(6,2) NOT NULL,
  `service_date` date NOT NULL,
  `rating` tinyint DEFAULT NULL COMMENT '1-5',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0待审 1通过 2驳回',
  `reviewer_id` bigint unsigned DEFAULT NULL,
  `review_time` datetime DEFAULT NULL,
  `reject_reason` varchar(200) DEFAULT NULL,
  `points_awarded` int NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `idx_status` (`status`),
  KEY `fk_ssr_reviewer` (`reviewer_id`),
  CONSTRAINT `fk_ssr_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `admin_user` (`id`) ON DELETE SET NULL,
  CONSTRAINT `fk_ssr_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='社会服务记录';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `social_service_record`
--

LOCK TABLES `social_service_record` WRITE;
/*!40000 ALTER TABLE `social_service_record` DISABLE KEYS */;
/*!40000 ALTER TABLE `social_service_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student`
--

DROP TABLE IF EXISTS `student`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_no` varchar(20) NOT NULL,
  `name` varchar(50) NOT NULL,
  `gender` varchar(10) DEFAULT NULL COMMENT '性别：男/女',
  `ethnicity` varchar(20) DEFAULT NULL COMMENT '民族',
  `birth_date` date DEFAULT NULL COMMENT '出生年月（年月精度，存当月1日）',
  `native_place` varchar(100) DEFAULT NULL COMMENT '籍贯（省/市/县）',
  `political_status` varchar(20) DEFAULT NULL COMMENT '政治面貌：中共党员/中共预备党员/共青团员/群众',
  `college` varchar(100) DEFAULT NULL,
  `major` varchar(100) DEFAULT NULL,
  `grade` varchar(10) DEFAULT NULL,
  `phone` varchar(512) NOT NULL COMMENT 'AES加密存储',
  `phone_hash` char(64) NOT NULL COMMENT 'sha256(明文手机号)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '1-在校 2-毕业 3-休学',
  `enroll_date` date DEFAULT NULL,
  `retire_date` date DEFAULT NULL COMMENT '退役返校日期',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_no` (`student_no`),
  KEY `idx_name` (`name`),
  KEY `idx_college_grade` (`college`,`grade`),
  KEY `idx_phone_hash` (`phone_hash`),
  KEY `idx_deleted` (`deleted`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='退役大学生士兵基本信息';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student`
--

LOCK TABLES `student` WRITE;
/*!40000 ALTER TABLE `student` DISABLE KEYS */;
/*!40000 ALTER TABLE `student` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_employment_intention`
--

DROP TABLE IF EXISTS `student_employment_intention`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_employment_intention` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `area_preference` varchar(100) DEFAULT NULL,
  `job_codes` json DEFAULT NULL COMMENT '就业意向岗位代码数组',
  `update_semester` varchar(20) DEFAULT NULL,
  `deleted` tinyint NOT NULL DEFAULT '0',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  CONSTRAINT `fk_sei_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='就业意向';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_employment_intention`
--

LOCK TABLES `student_employment_intention` WRITE;
/*!40000 ALTER TABLE `student_employment_intention` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_employment_intention` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Temporary view structure for view `student_points_summary`
--

DROP TABLE IF EXISTS `student_points_summary`;
/*!50001 DROP VIEW IF EXISTS `student_points_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `student_points_summary` AS SELECT 
 1 AS `student_id`,
 1 AS `total_points`*/;
SET character_set_client = @saved_cs_client;

--
-- Table structure for table `student_preference`
--

DROP TABLE IF EXISTS `student_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_preference` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `notify_enabled` tinyint NOT NULL DEFAULT '1' COMMENT '通知开关 1开启 0关闭',
  `resume_template` varchar(20) DEFAULT 'military' COMMENT '简历模板 military/simple',
  `show_real_name` tinyint NOT NULL DEFAULT '1' COMMENT '排行榜显示真实姓名 1显示 0匿名',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  CONSTRAINT `fk_sp_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='学生偏好设置';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_preference`
--

LOCK TABLES `student_preference` WRITE;
/*!40000 ALTER TABLE `student_preference` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_preference` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_service_willingness`
--

DROP TABLE IF EXISTS `student_service_willingness`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_service_willingness` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `available_time_slot` varchar(200) DEFAULT NULL,
  `accept_off_campus` tinyint NOT NULL DEFAULT '0',
  `post_codes` json DEFAULT NULL COMMENT '意向岗位代码数组，如["RECRUIT_PROM","TRAINING_INST"]',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`),
  CONSTRAINT `fk_ssw_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='服务意愿';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_service_willingness`
--

LOCK TABLES `student_service_willingness` WRITE;
/*!40000 ALTER TABLE `student_service_willingness` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_service_willingness` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `student_skill`
--

DROP TABLE IF EXISTS `student_skill`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `student_skill` (
  `id` bigint unsigned NOT NULL AUTO_INCREMENT,
  `student_id` bigint unsigned NOT NULL,
  `cert_code` varchar(20) NOT NULL,
  `cert_no` varchar(100) DEFAULT NULL,
  `obtain_date` date DEFAULT NULL,
  `valid_until` date DEFAULT NULL,
  `points_awarded` int NOT NULL DEFAULT '0',
  `deleted` tinyint NOT NULL DEFAULT '0',
  `status` tinyint DEFAULT '0' COMMENT '0-待审 1-通过 2-驳回',
  `reviewer_id` bigint DEFAULT NULL COMMENT '审核人ID',
  `review_time` datetime DEFAULT NULL COMMENT '审核时间',
  `reject_reason` varchar(200) DEFAULT NULL COMMENT '驳回原因',
  `proof_url` varchar(500) DEFAULT NULL COMMENT '证明材料路径',
  PRIMARY KEY (`id`),
  KEY `idx_student_id` (`student_id`),
  KEY `fk_ss_cert` (`cert_code`),
  KEY `idx_ss_status` (`status`),
  CONSTRAINT `fk_ss_cert` FOREIGN KEY (`cert_code`) REFERENCES `dict_cert` (`code`) ON DELETE RESTRICT,
  CONSTRAINT `fk_ss_student` FOREIGN KEY (`student_id`) REFERENCES `student` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技能证书';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `student_skill`
--

LOCK TABLES `student_skill` WRITE;
/*!40000 ALTER TABLE `student_skill` DISABLE KEYS */;
/*!40000 ALTER TABLE `student_skill` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'soldier'
--

--
-- Final view structure for view `student_points_summary`
--

/*!50001 DROP VIEW IF EXISTS `student_points_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `student_points_summary` AS select `s`.`id` AS `student_id`,coalesce(sum(`pd`.`change_value`),0) AS `total_points` from (`student` `s` left join `points_detail` `pd` on((`s`.`id` = `pd`.`student_id`))) group by `s`.`id` */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-21  3:29:28
