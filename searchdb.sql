/*
Navicat MySQL Data Transfer

Source Server         : localhost
Source Server Version : 50524
Source Host           : localhost:3306
Source Database       : searchdb

Target Server Type    : MYSQL
Target Server Version : 50524
File Encoding         : 65001

Date: 2013-06-07 21:28:06
*/

SET FOREIGN_KEY_CHECKS=0;
-- ----------------------------
-- Table structure for `keywordtable`
-- ----------------------------
DROP TABLE IF EXISTS `keywordtable`;
CREATE TABLE `keywordtable` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `keyword` text,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=27 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of keywordtable
-- ----------------------------
INSERT INTO `keywordtable` VALUES ('1', 'ga');
INSERT INTO `keywordtable` VALUES ('2', 'haha');

-- ----------------------------
-- Table structure for `resulttable`
-- ----------------------------
DROP TABLE IF EXISTS `resulttable`;
CREATE TABLE `resulttable` (
  `ID` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `keywordid` int(11) DEFAULT NULL,
  `linktitle` text,
  `linkurl` text,
  `linkabstract` text,
  `showpage` int(11) DEFAULT NULL,
  `formresultnum` int(11) DEFAULT NULL,
  `resultnum` int(11) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB AUTO_INCREMENT=643 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of resulttable
-- ----------------------------
INSERT INTO `resulttable` VALUES ('1', '1', '百度1', 'http://baidu.com', '百度一下你就知道1', '1', '1', '0');
INSERT INTO `resulttable` VALUES ('2', '1', '百度11', 'http://www.baidu.com11', '百度一下你就知道11', '1', '2', '0');
INSERT INTO `resulttable` VALUES ('3', '1', '百度12', 'http://www.baidu.com12', '百度一下你就知道12', '1', '3', '0');
INSERT INTO `resulttable` VALUES ('4', '1', '百度2', 'http://www.baidu.com2', '百度一下你就知道2', '1', '4', '1');
INSERT INTO `resulttable` VALUES ('5', '1', '百度23', 'http://www.baidu.com23', '百度一下你就知道23', '1', '5', '1');
