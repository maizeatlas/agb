# ************************************************************
# Sequel Pro SQL dump
# Version 4541
#
# http://www.sequelpro.com/
# https://github.com/sequelpro/sequelpro
#
# Host: 10.0.0.06 (MySQL 5.6.44)
# Database: agbv2
# Generation Time: 2020-06-23 03:22:33 +0000
# ************************************************************


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


# Dump of table classification
# ------------------------------------------------------------

DROP TABLE IF EXISTS `classification`;

CREATE TABLE `classification` (
  `classification_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `classification_code` varchar(255) NOT NULL,
  `classIfication_type` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`classification_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table collection_info
# ------------------------------------------------------------

DROP TABLE IF EXISTS `collection_info`;

CREATE TABLE `collection_info` (
  `collection_info_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `field_id` int(10) unsigned DEFAULT NULL,
  `source_id` int(10) unsigned DEFAULT NULL,
  `collection_identifier` varchar(255) NOT NULL,
  `col_date` datetime DEFAULT NULL,
  PRIMARY KEY (`collection_info_id`),
  KEY `FK_div_accession_collecting_div_field` (`field_id`),
  KEY `FK_collection_source_id_idx` (`source_id`),
  CONSTRAINT `FK_collection_field_id` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_collection_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table container_location
# ------------------------------------------------------------

DROP TABLE IF EXISTS `container_location`;

CREATE TABLE `container_location` (
  `container_location_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `location_id` int(10) unsigned NOT NULL,
  `building` varchar(255) DEFAULT NULL,
  `room` varchar(255) DEFAULT NULL,
  `tier1_position` varchar(45) DEFAULT NULL,
  `tier2_position` varchar(45) DEFAULT NULL,
  `tier3_position` varchar(45) DEFAULT NULL,
  `shelf` varchar(45) DEFAULT NULL,
  `container_location_comments` text,
  PRIMARY KEY (`container_location_id`),
  KEY `FK_div_storage_unit_div_locality` (`location_id`),
  CONSTRAINT `FK_div_storage_unit_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table experiment
# ------------------------------------------------------------

DROP TABLE IF EXISTS `experiment`;

CREATE TABLE `experiment` (
  `experiment_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `exp_name` varchar(255) NOT NULL,
  `exp_design` varchar(255) DEFAULT NULL,
  `exp_originator` varchar(255) DEFAULT NULL,
  `exp_comments` text,
  PRIMARY KEY (`experiment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table experiment_factor
# ------------------------------------------------------------

DROP TABLE IF EXISTS `experiment_factor`;

CREATE TABLE `experiment_factor` (
  `experiment_factor_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `exp_factor_name` varchar(255) DEFAULT NULL,
  `exp_factor_type` varchar(255) DEFAULT NULL,
  `exp_factor_desc` varchar(255) DEFAULT NULL,
  `exp_factor_comments` text,
  PRIMARY KEY (`experiment_factor_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table experiment_factor_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `experiment_factor_value`;

CREATE TABLE `experiment_factor_value` (
  `experiment_factor_value_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `experiment_factor_id` int(10) unsigned NOT NULL,
  `experiment_id` int(10) unsigned NOT NULL,
  `observation_unit_id` int(10) unsigned DEFAULT NULL,
  `stock_id` int(10) unsigned DEFAULT NULL,
  `measurement_unit_id` int(10) unsigned DEFAULT NULL,
  `exp_factor_value_level` varchar(255) NOT NULL,
  `exp_factor_value_comments` text,
  PRIMARY KEY (`experiment_factor_value_id`),
  KEY `FK_div_experiment_factor_value_div_experiment_factor` (`experiment_factor_id`),
  KEY `FK_div_experiment_factor_value_div_experiment` (`experiment_id`),
  KEY `FK_div_experiment_factor_value_div_obs_unit` (`observation_unit_id`),
  KEY `FK_div_experiment_factor_value_div_unit_of_measure` (`measurement_unit_id`),
  KEY `FK_div_stock_idx` (`stock_id`),
  CONSTRAINT `FK_experiment_factor_value_experiment_factor_id` FOREIGN KEY (`experiment_factor_id`) REFERENCES `experiment_factor` (`experiment_factor_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_experiment_factor_value_experiment_id` FOREIGN KEY (`experiment_id`) REFERENCES `experiment` (`experiment_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_experiment_factor_value_measurement_unit_id` FOREIGN KEY (`measurement_unit_id`) REFERENCES `measurement_unit` (`measurement_unit_id`),
  CONSTRAINT `FK_experiment_factor_value_observation_uni_id` FOREIGN KEY (`observation_unit_id`) REFERENCES `observation_unit` (`observation_unit_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_experiment_factor_value_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`stock_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table field
# ------------------------------------------------------------

DROP TABLE IF EXISTS `field`;

CREATE TABLE `field` (
  `field_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `location_id` int(10) unsigned NOT NULL,
  `field_name` varchar(255) DEFAULT NULL,
  `field_number` varchar(255) DEFAULT NULL,
  `altitude` varchar(255) DEFAULT NULL,
  `latitude` varchar(255) DEFAULT NULL,
  `longitude` varchar(255) DEFAULT NULL,
  `field_comments` text,
  PRIMARY KEY (`field_id`),
  KEY `FK_div_field_div_locality` (`location_id`),
  CONSTRAINT `FK_div_field_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table field_owner
# ------------------------------------------------------------

DROP TABLE IF EXISTS `field_owner`;

CREATE TABLE `field_owner` (
  `field_owner_id` int(11) NOT NULL,
  `field_id` int(10) unsigned NOT NULL,
  `users_id` int(11) NOT NULL,
  PRIMARY KEY (`field_owner_id`),
  KEY `field_owner_users_id_idx` (`users_id`),
  KEY `field_owner_field_id_idx` (`field_id`),
  CONSTRAINT `field_owner_field_id` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `field_owner_users_id` FOREIGN KEY (`users_id`) REFERENCES `users` (`users_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table location
# ------------------------------------------------------------

DROP TABLE IF EXISTS `location`;

CREATE TABLE `location` (
  `location_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `location_name` varchar(255) DEFAULT NULL,
  `city` varchar(255) DEFAULT NULL,
  `state_province` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `zipcode` varchar(255) NOT NULL,
  `location_comments` text,
  PRIMARY KEY (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table mate
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mate`;

CREATE TABLE `mate` (
  `mate_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mating_type` varchar(255) DEFAULT NULL,
  `mate_role` varchar(255) NOT NULL,
  PRIMARY KEY (`mate_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table mate_method
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mate_method`;

CREATE TABLE `mate_method` (
  `mate_method_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mate_method_name` varchar(255) NOT NULL,
  `mate_method_desc` text,
  `mate_method_user` varchar(255) DEFAULT NULL,
  `date_defined` datetime DEFAULT NULL,
  `mate_method_comments` text,
  PRIMARY KEY (`mate_method_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table mate_method_connect
# ------------------------------------------------------------

DROP TABLE IF EXISTS `mate_method_connect`;

CREATE TABLE `mate_method_connect` (
  `mate_method_connect_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `mate_id` int(10) unsigned NOT NULL,
  `mate_method_id` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`mate_method_connect_id`),
  KEY `FK_div_mate_connect_div_mate` (`mate_id`),
  KEY `FK_div_mate_connect_div_mate_method` (`mate_method_id`),
  CONSTRAINT `FK_connect_mate_id` FOREIGN KEY (`mate_id`) REFERENCES `mate` (`mate_id`),
  CONSTRAINT `FK_connect_mate_method_id` FOREIGN KEY (`mate_method_id`) REFERENCES `mate_method` (`mate_method_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table measurement_parameter
# ------------------------------------------------------------

DROP TABLE IF EXISTS `measurement_parameter`;

CREATE TABLE `measurement_parameter` (
  `measurement_parameter_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `measurement_unit_id` int(10) unsigned NOT NULL,
  `ontology_accession` varchar(255) DEFAULT NULL,
  `measurement_classification` varchar(255) DEFAULT NULL,
  `parameter_name` varchar(255) NOT NULL,
  `parameter_code` varchar(255) DEFAULT NULL,
  `format` varchar(255) DEFAULT NULL,
  `defaultValue` varchar(255) DEFAULT NULL,
  `minValue` varchar(255) DEFAULT NULL,
  `maxValue` varchar(255) DEFAULT NULL,
  `categories` varchar(255) DEFAULT NULL,
  `isVisible` varchar(255) NOT NULL DEFAULT 'True',
  `protocol` text,
  PRIMARY KEY (`measurement_parameter_id`),
  KEY `FK_div_measurement_parameter_div_unit_of_measure` (`measurement_unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table measurement_type
# ------------------------------------------------------------

DROP TABLE IF EXISTS `measurement_type`;

CREATE TABLE `measurement_type` (
  `measurement_type_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `measurement_type` varchar(255) NOT NULL,
  PRIMARY KEY (`measurement_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table measurement_unit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `measurement_unit`;

CREATE TABLE `measurement_unit` (
  `measurement_unit_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `units` varchar(255) NOT NULL,
  PRIMARY KEY (`measurement_unit_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table measurement_value
# ------------------------------------------------------------

DROP TABLE IF EXISTS `measurement_value`;

CREATE TABLE `measurement_value` (
  `measurement_value_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `field_id` int(10) unsigned DEFAULT NULL,
  `measurement_parameter_id` int(10) unsigned DEFAULT NULL,
  `source_id` int(10) unsigned DEFAULT NULL,
  `observation_unit_id` int(10) unsigned DEFAULT NULL,
  `measurement_type_id` int(10) unsigned DEFAULT NULL,
  `tom` datetime DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `measurement_value_comments` text,
  PRIMARY KEY (`measurement_value_id`),
  UNIQUE KEY `idx_name` (`observation_unit_id`,`measurement_parameter_id`,`tom`),
  KEY `FK_div_measurement_div_field` (`field_id`),
  KEY `FK_div_measurement_div_measurement_parameter` (`measurement_parameter_id`),
  KEY `FK_div_measurement_cdv_source` (`source_id`),
  KEY `FK_div_measurement_div_obs_unit` (`observation_unit_id`),
  KEY `FK_div_measurement_div_statistic_type` (`measurement_type_id`),
  CONSTRAINT `FK_measurement_field_id` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_measurement_measurement_parameter_id` FOREIGN KEY (`measurement_parameter_id`) REFERENCES `measurement_parameter` (`measurement_parameter_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_measurement_measurement_type_id` FOREIGN KEY (`measurement_type_id`) REFERENCES `measurement_type` (`measurement_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_measurement_observation_unit_id` FOREIGN KEY (`observation_unit_id`) REFERENCES `observation_unit` (`observation_unit_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_measurement_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table obervation_unit_sample
# ------------------------------------------------------------

DROP TABLE IF EXISTS `obervation_unit_sample`;

CREATE TABLE `obervation_unit_sample` (
  `observation_unit_sample_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `observation_unit_id` int(10) unsigned NOT NULL,
  `source_id` int(10) unsigned DEFAULT NULL,
  `sample_date` datetime NOT NULL,
  `sample_name` varchar(255) NOT NULL,
  `observation_unit_sample_comments` text,
  PRIMARY KEY (`observation_unit_sample_id`),
  KEY `FK_div_obs_unit_sample_div_obs_unit` (`observation_unit_id`),
  KEY `FK_sample_source_id_idx` (`source_id`),
  CONSTRAINT `FK_sample_observation_unit_id` FOREIGN KEY (`observation_unit_id`) REFERENCES `observation_unit` (`observation_unit_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_sample_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table observation_unit
# ------------------------------------------------------------

DROP TABLE IF EXISTS `observation_unit`;

CREATE TABLE `observation_unit` (
  `observation_unit_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `field_id` int(10) unsigned NOT NULL,
  `stock_id` int(10) unsigned DEFAULT NULL,
  `mate_method_connect_id` int(10) unsigned DEFAULT NULL,
  `coordinate_x` int(255) DEFAULT NULL,
  `coordinate_y` int(255) DEFAULT NULL,
  `coordinate_z` int(255) DEFAULT NULL,
  `plot` int(10) unsigned DEFAULT NULL,
  `row` int(10) unsigned DEFAULT NULL,
  `plant` varchar(255) DEFAULT NULL,
  `tagname` varchar(255) NOT NULL,
  `purpose` varchar(255) DEFAULT NULL,
  `planting_date` datetime DEFAULT NULL,
  `kernels` int(10) DEFAULT NULL,
  `delay` int(10) DEFAULT NULL,
  `harvest_date` datetime DEFAULT NULL,
  `observation_unit_comments` text,
  PRIMARY KEY (`observation_unit_id`),
  UNIQUE KEY `tagname_UNIQUE` (`tagname`),
  UNIQUE KEY `observation_unit_id_UNIQUE` (`observation_unit_id`),
  KEY `FK_div_obs_unit_div_field` (`field_id`),
  KEY `FK_div_obs_unit_div_stock` (`stock_id`),
  KEY `FK_div_obs_unit_div_mate_connect` (`mate_method_connect_id`),
  CONSTRAINT `FK_observation_unit_filed_id` FOREIGN KEY (`field_id`) REFERENCES `field` (`field_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_observation_unit_mate_connect_id` FOREIGN KEY (`mate_method_connect_id`) REFERENCES `mate_method_connect` (`mate_method_connect_id`),
  CONSTRAINT `FK_observation_unit_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`stock_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table passport
# ------------------------------------------------------------

DROP TABLE IF EXISTS `passport`;

CREATE TABLE `passport` (
  `passport_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `taxonomy_id` int(10) unsigned DEFAULT NULL,
  `collection_info_id` int(10) unsigned DEFAULT NULL,
  `source_id` int(10) unsigned DEFAULT NULL,
  `classification_id` int(10) unsigned DEFAULT NULL,
  `accession_identifier` varchar(255) NOT NULL DEFAULT 'NA',
  `accession_name` varchar(255) NOT NULL DEFAULT 'NA',
  `pedigree` varchar(255) NOT NULL DEFAULT 'NA',
  `passport_comments` text,
  PRIMARY KEY (`passport_id`),
  KEY `FK_div_passport_div_taxonomy` (`taxonomy_id`),
  KEY `FK_div_passport_div_accession_collecting` (`collection_info_id`),
  KEY `FK_div_passport_div_sampstat` (`classification_id`),
  KEY `FK_passport_source_id` (`source_id`),
  KEY `accession` (`accession_name`),
  CONSTRAINT `FK_passport_classification_id` FOREIGN KEY (`classification_id`) REFERENCES `classification` (`classification_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_passport_collection_id` FOREIGN KEY (`collection_info_id`) REFERENCES `collection_info` (`collection_info_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_passport_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`),
  CONSTRAINT `FK_passport_taxonomy_id` FOREIGN KEY (`taxonomy_id`) REFERENCES `taxonomy` (`taxonomy_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table passport_group
# ------------------------------------------------------------

DROP TABLE IF EXISTS `passport_group`;

CREATE TABLE `passport_group` (
  `passport_group_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) NOT NULL,
  PRIMARY KEY (`passport_group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table passport_set
# ------------------------------------------------------------

DROP TABLE IF EXISTS `passport_set`;

CREATE TABLE `passport_set` (
  `passport_set_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `passport_group_id` int(10) unsigned NOT NULL,
  `passport_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`passport_set_id`),
  KEY `FK_cdv_passport_set_cdv_passport_group` (`passport_group_id`),
  KEY `FK_cdv_passport_set_div_passport` (`passport_id`),
  CONSTRAINT `passport_set_passport_group_id` FOREIGN KEY (`passport_group_id`) REFERENCES `passport_group` (`passport_group_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `passport_set_passport_id` FOREIGN KEY (`passport_id`) REFERENCES `passport` (`passport_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table passport_synonym
# ------------------------------------------------------------

DROP TABLE IF EXISTS `passport_synonym`;

CREATE TABLE `passport_synonym` (
  `passport_synonym_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `passport_id` int(10) unsigned NOT NULL,
  `synonym` varchar(255) NOT NULL,
  `synonym_comments` text,
  PRIMARY KEY (`passport_synonym_id`),
  KEY `FK_div_synonym_div_passport` (`passport_id`),
  CONSTRAINT `FK_passport_synonym_passport_id` FOREIGN KEY (`passport_id`) REFERENCES `passport` (`passport_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table project
# ------------------------------------------------------------

DROP TABLE IF EXISTS `project`;

CREATE TABLE `project` (
  `project_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `source_id` int(10) unsigned DEFAULT NULL,
  `project_name` varchar(255) NOT NULL,
  `project_objective` varchar(255) DEFAULT NULL,
  `personel` varchar(255) DEFAULT NULL,
  `institute_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  KEY `FK_project_source_id_idx` (`source_id`),
  CONSTRAINT `FK_project_source_id` FOREIGN KEY (`source_id`) REFERENCES `source` (`source_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table source
# ------------------------------------------------------------

DROP TABLE IF EXISTS `source`;

CREATE TABLE `source` (
  `source_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `location_id` int(10) unsigned NOT NULL,
  `person_name` varchar(255) NOT NULL,
  `institute` varchar(255) DEFAULT NULL,
  `department` varchar(255) DEFAULT NULL,
  `street_address` varchar(255) DEFAULT NULL,
  `phone` varchar(255) DEFAULT NULL,
  `fax` varchar(255) DEFAULT NULL,
  `email` varchar(255) DEFAULT NULL,
  `url` varchar(255) DEFAULT NULL,
  `source_comments` text,
  PRIMARY KEY (`source_id`),
  KEY `FK_cdv_source_div_locality` (`location_id`),
  CONSTRAINT `FK_cdv_source_location` FOREIGN KEY (`location_id`) REFERENCES `location` (`location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock`;

CREATE TABLE `stock` (
  `stock_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `stock_generation_id` int(10) unsigned DEFAULT NULL,
  `passport_id` int(10) unsigned NOT NULL,
  `project_id` int(10) unsigned DEFAULT NULL,
  `stock_name` varchar(255) NOT NULL,
  `stock_date` datetime DEFAULT NULL,
  `stock_comments` text,
  PRIMARY KEY (`stock_id`),
  UNIQUE KEY `stock_id_UNIQUE` (`stock_id`),
  KEY `FK_div_stock_div_generation` (`stock_generation_id`),
  KEY `FK_div_stock_div_passport` (`passport_id`),
  KEY `FK_div_stock_div_project` (`project_id`),
  CONSTRAINT `FK_stock_generation_id` FOREIGN KEY (`stock_generation_id`) REFERENCES `stock_generation` (`stock_generation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_stock_passport_id` FOREIGN KEY (`passport_id`) REFERENCES `passport` (`passport_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_stock_project_id` FOREIGN KEY (`project_id`) REFERENCES `project` (`project_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock_composition
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock_composition`;

CREATE TABLE `stock_composition` (
  `stock_composition_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `stock_id` int(10) unsigned NOT NULL,
  `mix_from_stock_id` int(10) unsigned DEFAULT NULL,
  `observation_unit_id` int(10) unsigned DEFAULT NULL,
  `mate_method_connect_id` int(10) unsigned DEFAULT NULL,
  `measurement_unit_id` int(10) unsigned DEFAULT NULL,
  `mix_quantity` int(10) unsigned DEFAULT NULL,
  `mate_link` int(10) unsigned DEFAULT NULL,
  PRIMARY KEY (`stock_composition_id`),
  KEY `FK_div_parent_stock_div_stock` (`stock_id`),
  KEY `FK_div_parent_stock_div_parent` (`mix_from_stock_id`),
  KEY `FK_div_parent_stock_div_unit_of_measure` (`measurement_unit_id`),
  KEY `FK_div_parent_stock_div_obs_unit` (`observation_unit_id`),
  KEY `FK_div_parent_stock_div_mate_connect` (`mate_method_connect_id`),
  CONSTRAINT `FK_stock_composition_mate_connect_id` FOREIGN KEY (`mate_method_connect_id`) REFERENCES `mate_method_connect` (`mate_method_connect_id`),
  CONSTRAINT `FK_stock_composition_measurement_unit_id` FOREIGN KEY (`measurement_unit_id`) REFERENCES `measurement_unit` (`measurement_unit_id`),
  CONSTRAINT `FK_stock_composition_mix_from_stock_id` FOREIGN KEY (`mix_from_stock_id`) REFERENCES `stock` (`stock_id`),
  CONSTRAINT `FK_stock_composition_observation_unit_id` FOREIGN KEY (`observation_unit_id`) REFERENCES `observation_unit` (`observation_unit_id`),
  CONSTRAINT `FK_stock_composition_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`stock_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock_generation
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock_generation`;

CREATE TABLE `stock_generation` (
  `stock_generation_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `generation` varchar(255) NOT NULL,
  `cycle` varchar(255) DEFAULT NULL,
  `generation_comments` text,
  PRIMARY KEY (`stock_generation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock_packet
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock_packet`;

CREATE TABLE `stock_packet` (
  `stock_packet_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `stock_id` int(10) unsigned NOT NULL,
  `stock_packet_container_id` int(10) unsigned DEFAULT NULL,
  `packet_no` tinyint(3) unsigned DEFAULT NULL,
  `weight` decimal(8,2) unsigned DEFAULT NULL,
  `no_seed` int(10) unsigned DEFAULT NULL,
  `stock_packet_date` datetime DEFAULT NULL,
  `stock_packet_comments` text,
  PRIMARY KEY (`stock_packet_id`),
  UNIQUE KEY `packet` (`stock_id`,`packet_no`),
  KEY `FK_div_stock_packet_div_stock` (`stock_id`),
  KEY `FK_div_stock_packet_div_packet_loc` (`stock_packet_container_id`),
  CONSTRAINT `FK_stock_packet_container_id` FOREIGN KEY (`stock_packet_container_id`) REFERENCES `stock_packet_container` (`stock_packet_container_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `FK_stock_packet_stock_id` FOREIGN KEY (`stock_id`) REFERENCES `stock` (`stock_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table stock_packet_container
# ------------------------------------------------------------

DROP TABLE IF EXISTS `stock_packet_container`;

CREATE TABLE `stock_packet_container` (
  `stock_packet_container_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `container_location_id` int(10) unsigned DEFAULT NULL,
  `unit` varchar(255) DEFAULT NULL,
  `stock_packet_container_comments` text,
  PRIMARY KEY (`stock_packet_container_id`),
  KEY `FK_div_packet_loc_div_storage_unit` (`container_location_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table taxonomy
# ------------------------------------------------------------

DROP TABLE IF EXISTS `taxonomy`;

CREATE TABLE `taxonomy` (
  `taxonomy_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `genus` varchar(255) DEFAULT NULL,
  `species` varchar(255) NOT NULL,
  `subspecies` varchar(255) DEFAULT NULL,
  `subtaxa` varchar(255) DEFAULT NULL,
  `race` varchar(255) DEFAULT NULL,
  `population` varchar(255) DEFAULT NULL,
  `common_name` varchar(255) DEFAULT NULL,
  `gto` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`taxonomy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;



# Dump of table users
# ------------------------------------------------------------

DROP TABLE IF EXISTS `users`;

CREATE TABLE `users` (
  `users_id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(100) NOT NULL,
  `password` varchar(100) NOT NULL,
  `created` datetime NOT NULL,
  `modified` datetime NOT NULL,
  `role` varchar(45) NOT NULL,
  `users_comments` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`users_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;




/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;
/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
