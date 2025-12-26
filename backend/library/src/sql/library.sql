-- MySQL dump 10.13  Distrib 5.7.44, for Win64 (x86_64)
--
-- Host: localhost    Database: library
-- ------------------------------------------------------
-- Server version	5.7.44-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `library`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `library` /*!40100 DEFAULT CHARACTER SET latin1 */;

USE `library`;

--
-- Table structure for table `book`
--

DROP TABLE IF EXISTS `book`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `book` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(256) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '书名',
  `author` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '作者',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) DEFAULT NULL,
  `available_qty` int(11) DEFAULT '0' COMMENT '可借册数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='图书表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `book`
--

LOCK TABLES `book` WRITE;
/*!40000 ALTER TABLE `book` DISABLE KEYS */;
INSERT INTO `book` VALUES (1,'三体','刘慈欣','2025-12-19 12:23:20','2025-12-14 16:27:48',0,5),(2,'平凡的世界','路遥','2025-12-19 11:31:15','2025-12-14 16:27:53',0,2);
/*!40000 ALTER TABLE `book` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `borrow_record`
--

DROP TABLE IF EXISTS `borrow_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `borrow_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(256) DEFAULT NULL COMMENT '读者id',
  `book_id` varchar(512) DEFAULT NULL COMMENT '图书id',
  `borrow_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '借出时间',
  `status` tinyint(4) DEFAULT '0' COMMENT '0在借 1已还',
  `isDelete` tinyint(4) DEFAULT NULL,
  `due_date` datetime NOT NULL COMMENT '应归还时间',
  `return_date` datetime DEFAULT NULL COMMENT '实际还书时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`),
  KEY `idx_book_status` (`book_id`,`status`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1 COMMENT='借还书流水表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `borrow_record`
--

LOCK TABLES `borrow_record` WRITE;
/*!40000 ALTER TABLE `borrow_record` DISABLE KEYS */;
INSERT INTO `borrow_record` VALUES (1,'1','2','2025-12-19 10:49:09',1,NULL,'2025-12-20 10:49:09','2025-12-19 11:11:14'),(2,'2','2','2025-12-19 11:08:07',1,NULL,'2025-12-20 11:08:07','2025-12-19 11:31:14'),(3,'1','1','2025-12-19 11:53:25',1,NULL,'2025-12-20 11:53:25','2025-12-19 11:54:01'),(4,'2','1','2025-12-19 12:23:20',0,NULL,'2025-12-23 12:23:20',NULL);
/*!40000 ALTER TABLE `borrow_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `userName` varchar(256) DEFAULT NULL COMMENT '用户名',
  `userPassword` varchar(512) DEFAULT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) DEFAULT NULL,
  `userRole` int(11) DEFAULT '0' COMMENT '表示用户角色， 0 普通用户， 1 管理员',
  `max_borrow_num` int(11) DEFAULT '5' COMMENT '最大借书量',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=latin1 COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'zk3zy','123456','15167768888','2025-12-13 16:03:13','2025-12-13 16:03:13',0,0,5),(2,'James','abb283b0200e38c5676e51f4ee23f28b','15167768889','2025-12-14 14:42:28','2025-12-13 16:36:35',0,0,5),(3,'Harden','abb283b0200e38c5676e51f4ee23f28b','15167768887','2025-12-14 13:44:21','2025-12-14 13:44:21',0,1,5),(4,'rose','abb283b0200e38c5676e51f4ee23f28b',NULL,'2025-12-14 14:43:06','2025-12-14 14:43:06',0,0,5),(5,'zz','abb283b0200e38c5676e51f4ee23f28b',NULL,'2025-12-14 14:46:12','2025-12-14 14:45:59',0,1,5);
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-12-19 16:32:34
