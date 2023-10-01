
SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for blog
-- ----------------------------
DROP TABLE IF EXISTS `blog`;
CREATE TABLE `blog` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `images` varchar(2048) NOT NULL COMMENT '文章照片，最多5张，多张以","隔开',
  `content` varchar(2048) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '文章内容',
  `liked` int(8) unsigned DEFAULT '0' COMMENT '点赞数量',
  `comments` int(8) unsigned DEFAULT '0' COMMENT '评论数量',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COMMENT='文章表';


-- ----------------------------
-- Table structure for blog_comments
-- ----------------------------
DROP TABLE IF EXISTS `blog_comments`;
CREATE TABLE `blog_comments` (
  `id` bigint(20) unsigned NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `blogId` bigint(20) unsigned NOT NULL COMMENT '文章id',
  `parentId` bigint(20) unsigned DEFAULT NULL COMMENT '关联的1级评论id，如果是一级评论，则值为0',
  `answerId` bigint(20) unsigned DEFAULT NULL COMMENT '回复的评论id',
  `content` varchar(255) NOT NULL COMMENT '回复的内容',
  `contentImg` varchar(255) DEFAULT NULL COMMENT '评论的图片',
  `liked` int(8) unsigned DEFAULT '0' COMMENT '点赞数',
  `status` tinyint(1) unsigned DEFAULT '0' COMMENT '状态，0：正常，1：被举报，2：禁止查看',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8mb4 COMMENT='文章评论表';


-- ----------------------------
-- Table structure for chat
-- ----------------------------
DROP TABLE IF EXISTS `chat`;
CREATE TABLE `chat` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '聊天记录id',
  `fromId` bigint(20) NOT NULL COMMENT '发送消息id',
  `toId` bigint(20) DEFAULT NULL COMMENT '接收消息id',
  `text` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `chatType` tinyint(4) NOT NULL COMMENT '聊天类型 1-私聊 2-群聊',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `teamId` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=149 DEFAULT CHARSET=utf8mb4 COMMENT='聊天消息表';


-- ----------------------------
-- Table structure for comment_email
-- ----------------------------
DROP TABLE IF EXISTS `comment_email`;
CREATE TABLE `comment_email` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `avatar` varchar(1024) DEFAULT NULL COMMENT '收件人头像',
  `subject` varchar(255) NOT NULL,
  `email` varchar(512) NOT NULL COMMENT '收件人邮箱',
  `parentNick` varchar(256) DEFAULT NULL COMMENT '收件人名字',
  `parentComment` varchar(1024) DEFAULT NULL COMMENT '原评论内容',
  `nick` varchar(256) NOT NULL COMMENT '回复人名字',
  `comment` varchar(1024) NOT NULL COMMENT '回复评论内容',
  `postName` varchar(256) NOT NULL COMMENT '文章名字',
  `postUrl` varchar(1024) NOT NULL COMMENT '文章地址',
  `failNum` int(11) NOT NULL DEFAULT '0' COMMENT '失败次数',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8 COMMENT='评论邮件';

-- ----------------------------
-- Table structure for follow
-- ----------------------------
DROP TABLE IF EXISTS `follow`;
CREATE TABLE `follow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `userId` bigint(20) unsigned NOT NULL COMMENT '用户id',
  `followUserId` bigint(20) unsigned NOT NULL COMMENT '关联的用户id',
  `createTime` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4;


-- ----------------------------
-- Table structure for tag
-- ----------------------------
DROP TABLE IF EXISTS `tag`;
CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `tagName` varchar(256) NOT NULL COMMENT '标签名称',
  `userId` bigint(20) NOT NULL COMMENT '用户 id',
  `parentId` bigint(20) DEFAULT NULL COMMENT '父标签 id',
  `isParent` tinyint(4) DEFAULT NULL COMMENT '0 - 不是,1 - 父标签',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_tagName` (`tagName`) USING BTREE COMMENT '标签名索引',
  KEY `idx_userId` (`userId`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=101 DEFAULT CHARSET=utf8 COMMENT='标签';

-- ----------------------------
-- Records of tag
-- ----------------------------
INSERT INTO `tag` VALUES ('1', '性别', '1', null, '1', '2023-08-03 14:57:57', '2023-08-03 14:57:57', '0');
INSERT INTO `tag` VALUES ('2', '男', '1', '1', '0', '2023-08-03 14:58:33', '2023-08-03 14:58:33', '0');
INSERT INTO `tag` VALUES ('3', '女', '1', '1', '0', '2023-08-03 14:58:50', '2023-08-03 14:58:50', '0');
INSERT INTO `tag` VALUES ('4', '保密', '1', '1', '0', '2023-08-03 14:59:05', '2023-08-03 14:59:05', '0');
INSERT INTO `tag` VALUES ('5', '年级', '1', null, '1', '2023-08-03 14:59:26', '2023-08-03 14:59:26', '0');
INSERT INTO `tag` VALUES ('6', '高一', '1', '5', '0', '2023-08-03 15:00:02', '2023-08-03 15:00:02', '0');
INSERT INTO `tag` VALUES ('7', '高二', '1', '5', '0', '2023-08-03 15:00:18', '2023-08-03 15:00:18', '0');
INSERT INTO `tag` VALUES ('8', '高三', '1', '5', '0', '2023-08-03 15:00:31', '2023-08-03 15:00:31', '0');
INSERT INTO `tag` VALUES ('13', '大一', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('14', '大二', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('15', '大三', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('16', '大四', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('17', '研究生', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('18', '在职', '1', '5', '0', '2023-08-03 15:02:40', '2023-08-03 15:02:42', '0');
INSERT INTO `tag` VALUES ('19', '编程', '1', null, '1', '2023-08-03 15:58:26', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('20', 'Java', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('21', 'Python', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('22', 'C++', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('23', 'JavaScript', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('24', 'Ruby', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('25', 'PHP', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('26', 'Swift', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('27', 'Objective-C', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('28', 'C#', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('29', 'Go', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('30', 'Kotlin', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('31', 'TypeScript', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:26', '0');
INSERT INTO `tag` VALUES ('32', 'Rust', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('33', 'Lua', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('34', 'Perl', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('35', 'Scala', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('36', 'Dart', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('37', 'R', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('38', 'MATLAB', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('39', 'Haskell', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('40', 'Julia', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('41', 'Groovy', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('42', 'Shell', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('43', 'Assembly', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('44', 'F#', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('45', 'Clojure', '1', '19', '0', '2023-08-03 15:58:27', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('46', 'Erlang', '1', '19', '0', '2023-08-03 15:58:28', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('47', 'Lisp', '1', '19', '0', '2023-08-03 15:58:28', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('48', 'Prolog', '1', '19', '0', '2023-08-03 15:58:28', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('49', 'Smalltalk', '1', '19', '0', '2023-08-03 15:58:28', '2023-08-03 15:58:27', '0');
INSERT INTO `tag` VALUES ('50', '兴趣爱好', '1', null, '1', '2023-08-03 16:01:11', '2023-08-03 16:01:11', '0');
INSERT INTO `tag` VALUES ('59', '阅读', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('60', '旅游', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('61', '游戏', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('62', '音乐', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('63', '电影', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('64', '美食', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('65', '健身', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('66', '摄影', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('67', '绘画', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('68', '写作', '1', '50', '0', '2023-08-03 16:03:05', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('69', '手工', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('70', '园艺', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('71', '钓鱼', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('72', '篮球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('73', '台球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('74', '足球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('75', '棒球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('76', '乒乓球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('77', '羽毛球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('78', '网球', '1', '50', '0', '2023-08-03 16:03:06', '2023-08-03 16:03:05', '0');
INSERT INTO `tag` VALUES ('79', '职业', '1', null, '1', '2023-08-03 16:10:46', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('80', '医生', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('81', '律师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('82', '教师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('83', '工程师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('84', '程序员', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('85', '设计师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('86', '销售', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:46', '0');
INSERT INTO `tag` VALUES ('87', '市场营销', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('88', '会计师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('89', '金融分析师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('90', '投资银行家', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('91', '记者', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('92', '编辑', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('93', '作家', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('94', '演员', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('95', '导演', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('96', '音乐家', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('97', '画家', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('98', '建筑师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('99', '厨师', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');
INSERT INTO `tag` VALUES ('100', '学生', '1', '79', '0', '2023-08-03 16:10:47', '2023-08-03 16:10:47', '0');

-- ----------------------------
-- Table structure for team
-- ----------------------------
DROP TABLE IF EXISTS `team`;
CREATE TABLE `team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(256) NOT NULL COMMENT '队伍名称',
  `description` varchar(1024) DEFAULT NULL COMMENT '描述',
  `coverUrl` varchar(255) DEFAULT NULL COMMENT '封面图片',
  `maxNum` int(11) NOT NULL DEFAULT '1' COMMENT '最大人数',
  `expireTime` datetime DEFAULT NULL COMMENT '过期时间',
  `userId` bigint(20) DEFAULT NULL COMMENT '用户id',
  `status` int(11) NOT NULL DEFAULT '0' COMMENT '0 - 公开，1 - 私有，2 - 加密',
  `password` varchar(512) DEFAULT NULL COMMENT '密码',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8 COMMENT='队伍';


-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `username` varchar(256) DEFAULT NULL COMMENT '用户昵称',
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userAccount` varchar(256) NOT NULL COMMENT '账号',
  `userPassword` varchar(512) NOT NULL COMMENT '密码',
  `avatarUrl` varchar(1024) DEFAULT NULL COMMENT '用户头像',
  `gender` tinyint(4) DEFAULT NULL COMMENT '性别',
  `profile` varchar(512) DEFAULT '' COMMENT '个人简介',
  `phone` varchar(128) DEFAULT NULL COMMENT '电话',
  `email` varchar(512) DEFAULT NULL COMMENT '邮箱',
  `userStatus` int(11) NOT NULL DEFAULT '0' COMMENT '状态 0 - 正常',
  `createTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  `userRole` int(11) NOT NULL DEFAULT '0' COMMENT '用户角色 0 - 普通用户 1 - 管理员',
  `tags` varchar(1024) DEFAULT NULL COMMENT '标签 JSON 列表',
  `planetCode` varchar(512) DEFAULT NULL COMMENT '编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `unique_userAccount` (`userAccount`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=1017 DEFAULT CHARSET=utf8 COMMENT='用户';

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES ('顾梦', '1', 'admin', '0301c443e53e5ac4d2f0809fab2bfc62', 'https://cloud.jishuqin.cn/avatar.png', '1', '不想满心遗憾，那就全力以赴.', '15089131107', '374943980@qq.com', '0', '2023-07-23 19:37:28', '2023-08-20 21:57:07', '0', '1', '[\"男\",\"Java\",\"编程\",\"大二\",\"学生\"]', '1');
INSERT INTO `user` VALUES ('测试账号', '1006', 'user', '0301c443e53e5ac4d2f0809fab2bfc62', 'https://yuepei.jishuqin.cn/default.png', null, '', null, '159123456@example.com', '0', '2023-08-04 19:02:10', '2023-09-28 20:57:13', '0', '0', '[\"男\",\"大一\",\"Spring\",\"编程\"]', '4a6d1b481e');

-- ----------------------------
-- Table structure for user_team
-- ----------------------------
DROP TABLE IF EXISTS `user_team`;
CREATE TABLE `user_team` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `userId` bigint(20) DEFAULT NULL COMMENT '用户id',
  `teamId` bigint(20) DEFAULT NULL COMMENT '队伍id',
  `joinTime` datetime DEFAULT NULL COMMENT '加入时间',
  `createTime` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updateTime` datetime DEFAULT CURRENT_TIMESTAMP,
  `isDelete` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=70 DEFAULT CHARSET=utf8 COMMENT='用户队伍关系';

