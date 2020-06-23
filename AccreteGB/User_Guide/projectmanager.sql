# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 10.0.0.06 (MySQL 5.6.44)
# Database: projectmanager
# Generation Time: 2020-06-23 03:22:16 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table collaborate_relation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `collaborate_relation`;

CREATE TABLE `collaborate_relation` (
  `collaborate_relation_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`collaborate_relation_id`),
  KEY `FK_user_id_idx` (`user_id`),
  KEY `FK_project_id_idx` (`project_id`),
  CONSTRAINT `collaborate_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `collaborate_FK_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table experiment_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `experiment_group`;

CREATE TABLE `experiment_group` (
  `experiment_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `experiment_group_name` varchar(45) NOT NULL,
  `stockList_json` longtext,
  `expResult_json` longtext,
  PRIMARY KEY (`experiment_group_id`),
  UNIQUE KEY `experiment_group_id_UNIQUE` (`experiment_group_id`),
  KEY `experiment_FK_projectid_idx` (`project_id`),
  CONSTRAINT `experiment_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table harvesting_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `harvesting_group`;

CREATE TABLE `harvesting_group` (
  `harvesting_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `harvesting_group_name` varchar(45) NOT NULL,
  `crossRecord_json` longtext,
  `bulk_json` longtext,
  `stickerGenerator_json` longtext,
  PRIMARY KEY (`harvesting_group_id`),
  UNIQUE KEY `harvesting_group_id_UNIQUE` (`harvesting_group_id`),
  KEY `harvesting_FK_projectid_idx` (`project_id`),
  CONSTRAINT `harvesting_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table phenotype_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `phenotype_group`;

CREATE TABLE `phenotype_group` (
  `phenotype_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `phenotype_group_name` varchar(45) NOT NULL,
  `export_table_json` longtext,
  `import_table_json` longtext,
  PRIMARY KEY (`phenotype_group_id`),
  UNIQUE KEY `phenotype_group_id_UNIQUE` (`phenotype_group_id`),
  KEY `phenotype_FK_projectid_idx` (`project_id`),
  CONSTRAINT `phenotype_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table planting_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `planting_group`;

CREATE TABLE `planting_group` (
  `planting_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `planting_group_name` varchar(45) NOT NULL,
  `tableView_json` longtext,
  `tagGenerator_json` longtext,
  PRIMARY KEY (`planting_group_id`),
  UNIQUE KEY `planting_group_id_UNIQUE` (`planting_group_id`),
  KEY `planting_FK_projectid_idx` (`project_id`),
  CONSTRAINT `planting_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table project
# ------------------------------------------------------------

DROP TABLE IF EXISTS `project`;

CREATE TABLE `project` (
  `project_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `project_name` varchar(45) NOT NULL,
  `last_modified` datetime NOT NULL,
  `date_created` datetime NOT NULL,
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `project_name_UNIQUE` (`project_name`),
  KEY `FK_user_id_idx` (`user_id`),
  CONSTRAINT `FK_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table sampling_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `sampling_group`;

CREATE TABLE `sampling_group` (
  `sampling_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `sampling_group_name` varchar(45) NOT NULL,
  `sample_selection_json` longtext,
  `sample_setting_json` longtext,
  PRIMARY KEY (`sampling_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock_selection_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock_selection_group`;

CREATE TABLE `stock_selection_group` (
  `stock_selection_group_id` int(11) NOT NULL AUTO_INCREMENT,
  `project_id` int(11) NOT NULL,
  `stock_selection_group_name` varchar(45) NOT NULL,
  `cart_json` longtext,
  PRIMARY KEY (`stock_selection_group_id`),
  KEY `stock_FK_project_id_idx` (`project_id`),
  CONSTRAINT `stock_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table token_relation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `token_relation`;

CREATE TABLE `token_relation` (
  `token_relation_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `project_id` int(11) NOT NULL,
  `expiration_time` datetime NOT NULL,
  PRIMARY KEY (`token_relation_id`),
  KEY `token_FK_user_id_idx` (`user_id`),
  KEY `token_FK_project_id_idx` (`project_id`),
  CONSTRAINT `token_FK_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `token_FK_user_id` FOREIGN KEY (`user_id`) REFERENCES `user` (`user_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table user
# ------------------------------------------------------------

DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `user_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `first_name` varchar(45) NOT NULL,
  `last_name` varchar(45) NOT NULL,
  `email` varchar(100) NOT NULL,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
