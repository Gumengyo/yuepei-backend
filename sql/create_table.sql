CREATE TABLE tag (
                     id BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
                     tagName VARCHAR (256) NOT NULL COMMENT '标签名称',
                     userId BIGINT NOT NULL COMMENT '用户 id',
                     parentId BIGINT NULL COMMENT '父标签 id',
                     isParent TINYINT NULL COMMENT '0 - 不是,1 - 父标签',
                     createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                     updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                     isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除'
) COMMENT '标签';



CREATE TABLE USER (
                      username VARCHAR (256) NULL COMMENT '用户昵称',
                      id BIGINT auto_increment COMMENT 'id' PRIMARY KEY,
                      userAccount VARCHAR (256) NOT NULL COMMENT '账号',
                      userPassword VARCHAR (512) NOT NULL COMMENT '密码',
                      avatarUrl VARCHAR (1024) NULL COMMENT '用户头像',
                      gender TINYINT NULL COMMENT '性别',
                      phone VARCHAR (128) NULL COMMENT '电话',
                      email VARCHAR (512) NULL COMMENT '邮箱',
                      userStatus INT DEFAULT 0 NOT NULL COMMENT '状态 0 - 正常',
                      createTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL COMMENT '创建时间',
                      updateTime datetime DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                      isDelete TINYINT DEFAULT 0 NOT NULL COMMENT '是否删除',
                      userRole INT DEFAULT 0 NOT NULL COMMENT '用户角色 0 - 普通用户 1 - 管理员'
) COMMENT '用户';

ALTER table USER add COLUMN tags VARCHAR(1024) null COMMENT '标签列表'
