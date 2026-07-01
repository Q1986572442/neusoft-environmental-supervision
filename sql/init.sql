-- =====================================================
-- 东软环保公众监督系统 (NEP) 数据库初始化脚本
-- =====================================================

-- 设置客户端编码（关键！防止中文乱码）
SET NAMES utf8;

CREATE DATABASE IF NOT EXISTS nep_db
    DEFAULT CHARACTER SET utf8
    DEFAULT COLLATE utf8_general_ci;
USE nep_db;

-- =====================================================
-- 1. 用户表
-- =====================================================
DROP TABLE IF EXISTS nep_user;
CREATE TABLE nep_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    phone VARCHAR(20) NOT NULL COMMENT '手机号',
    password VARCHAR(64) NOT NULL COMMENT '登录密码(MD5加密)',
    real_name VARCHAR(50) NOT NULL COMMENT '真实姓名',
    age INT COMMENT '年龄',
    gender TINYINT DEFAULT 0 COMMENT '性别(0-女, 1-男)',
    role VARCHAR(10) NOT NULL DEFAULT 'NEPS' COMMENT '角色: NEPS/NEPG/NEPM/NEPV',
    employee_code VARCHAR(50) COMMENT '员工编号',
    province_id BIGINT COMMENT '所属省份ID',
    city_id BIGINT COMMENT '所属城市ID',
    status TINYINT DEFAULT 1 COMMENT '状态(0-禁用, 1-正常, 2-工作中)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_phone (phone),
    KEY idx_role (role),
    KEY idx_province (province_id),
    KEY idx_city (city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用户表';

-- =====================================================
-- 2. 省份表
-- =====================================================
DROP TABLE IF EXISTS nep_province;
CREATE TABLE nep_province (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '省份名称',
    code VARCHAR(10) NOT NULL COMMENT '省份代码',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='省份表';

-- =====================================================
-- 3. 城市表 (106个大城市)
-- =====================================================
DROP TABLE IF EXISTS nep_city;
CREATE TABLE nep_city (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(50) NOT NULL COMMENT '城市名称',
    code VARCHAR(10) NOT NULL COMMENT '城市代码',
    province_id BIGINT NOT NULL COMMENT '所属省份ID',
    city_level VARCHAR(20) COMMENT '城市级别',
    is_capital TINYINT DEFAULT 0 COMMENT '是否省会',
    sort_order INT DEFAULT 0 COMMENT '排序',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_province (province_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='城市表';

-- =====================================================
-- 4. 公众监督反馈表
-- =====================================================
DROP TABLE IF EXISTS nep_supervision_feedback;
CREATE TABLE nep_supervision_feedback (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    supervisor_id BIGINT NOT NULL COMMENT '监督员ID',
    province_id BIGINT NOT NULL COMMENT '省份ID',
    city_id BIGINT NOT NULL COMMENT '城市ID',
    specific_address VARCHAR(500) COMMENT '具体地址',
    estimated_aqi_level INT COMMENT '预估AQI等级(1-6)',
    description VARCHAR(2000) COMMENT '描述',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/ASSIGNED/COMPLETED',
    assigned_inspector_id BIGINT COMMENT '指派的网格员ID',
    assign_time TIMESTAMP NULL COMMENT '指派时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_supervisor (supervisor_id),
    KEY idx_status (status),
    KEY idx_province (province_id),
    KEY idx_city (city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='公众监督反馈表';

-- =====================================================
-- 5. AQI检测数据表
-- =====================================================
DROP TABLE IF EXISTS nep_aqi_detection;
CREATE TABLE nep_aqi_detection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    feedback_id BIGINT NOT NULL COMMENT '关联的反馈ID',
    inspector_id BIGINT NOT NULL COMMENT '网格员ID',
    province_id BIGINT COMMENT '省份ID',
    city_id BIGINT COMMENT '城市ID',
    so2_aqi INT COMMENT 'SO2 AQI浓度等级',
    co_aqi INT COMMENT 'CO AQI浓度等级',
    pm25_aqi INT COMMENT 'PM2.5 AQI浓度等级',
    final_aqi INT COMMENT '最终AQI等级(MAX(SO2,CO,PM2.5))',
    detection_time TIMESTAMP NULL COMMENT '检测时间',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_feedback (feedback_id),
    KEY idx_inspector (inspector_id),
    KEY idx_province (province_id),
    KEY idx_city (city_id),
    KEY idx_final_aqi (final_aqi)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='AQI检测数据表';

-- =====================================================
-- 6. 网格员区域分配表
-- =====================================================
DROP TABLE IF EXISTS nep_grid_assignment;
CREATE TABLE nep_grid_assignment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    inspector_id BIGINT NOT NULL COMMENT '网格员ID',
    province_id BIGINT NOT NULL COMMENT '省份ID',
    city_id BIGINT NOT NULL COMMENT '城市ID',
    status TINYINT DEFAULT 1 COMMENT '状态(0-停用, 1-可工作)',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_inspector (inspector_id),
    KEY idx_city (city_id),
    UNIQUE KEY uk_inspector_city (inspector_id, city_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='网格员区域分配表';

-- =====================================================
-- 测试数据：省份
-- =====================================================
INSERT INTO nep_province (name, code, sort_order) VALUES
('北京市', '110000', 1),
('天津市', '120000', 2),
('河北省', '130000', 3),
('山西省', '140000', 4),
('辽宁省', '210000', 5),
('吉林省', '220000', 6),
('黑龙江省', '230000', 7),
('上海市', '310000', 8),
('江苏省', '320000', 9),
('浙江省', '330000', 10),
('安徽省', '340000', 11),
('福建省', '350000', 12),
('江西省', '360000', 13),
('山东省', '370000', 14),
('河南省', '410000', 15),
('湖北省', '420000', 16),
('湖南省', '430000', 17),
('广东省', '440000', 18),
('广西壮族自治区', '450000', 19),
('海南省', '460000', 20),
('重庆市', '500000', 21),
('四川省', '510000', 22),
('贵州省', '520000', 23),
('云南省', '530000', 24),
('陕西省', '610000', 25),
('甘肃省', '620000', 26),
('青海省', '630000', 27),
('宁夏回族自治区', '640000', 28),
('新疆维吾尔自治区', '650000', 29);

-- 测试数据：城市（省会）
INSERT INTO nep_city (name, code, province_id, city_level, is_capital, sort_order) VALUES
('北京', '110100', 1, '超大城市', 1, 1),
('天津', '120100', 2, '超大城市', 1, 1),
('石家庄', '130100', 3, 'I型大城市', 1, 1),
('太原', '140100', 4, 'I型大城市', 1, 1),
('沈阳', '210100', 5, '特大城市', 1, 1),
('长春', '220100', 6, 'I型大城市', 1, 1),
('哈尔滨', '230100', 7, '特大城市', 1, 1),
('上海', '310100', 8, '超大城市', 1, 1),
('南京', '320100', 9, '特大城市', 1, 1),
('杭州', '330100', 10, '特大城市', 1, 1),
('合肥', '340100', 11, 'I型大城市', 1, 1),
('福州', '350100', 12, 'I型大城市', 1, 1),
('南昌', '360100', 13, 'I型大城市', 1, 1),
('济南', '370100', 14, '特大城市', 1, 1),
('郑州', '410100', 15, '特大城市', 1, 1),
('武汉', '420100', 16, '超大城市', 1, 1),
('长沙', '430100', 17, '特大城市', 1, 1),
('广州', '440100', 18, '超大城市', 1, 1),
('南宁', '450100', 19, 'I型大城市', 1, 1),
('海口', '460100', 20, 'II型大城市', 1, 1),
('重庆', '500100', 21, '超大城市', 1, 1),
('成都', '510100', 22, '超大城市', 1, 1),
('贵阳', '520100', 23, 'II型大城市', 1, 1),
('昆明', '530100', 24, '特大城市', 1, 1),
('西安', '610100', 25, '特大城市', 1, 1),
('兰州', '620100', 26, 'II型大城市', 1, 1),
('西宁', '630100', 27, 'II型大城市', 1, 1),
('银川', '640100', 28, 'II型大城市', 1, 1),
('乌鲁木齐', '650100', 29, 'II型大城市', 1, 1);

-- =====================================================
-- 7. 环保新闻公告表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_news;
CREATE TABLE nep_news (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '新闻标题',
    summary VARCHAR(500) COMMENT '新闻摘要',
    content TEXT COMMENT '新闻内容（富文本）',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    news_type VARCHAR(20) NOT NULL DEFAULT 'NEWS' COMMENT '类型: NEWS-新闻, NOTICE-公告, POLICY-政策',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布, 2-已下架',
    view_count BIGINT DEFAULT 0 COMMENT '浏览次数',
    publisher_id BIGINT COMMENT '发布人ID',
    publish_time TIMESTAMP NULL COMMENT '发布时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_type (news_type),
    KEY idx_status (status),
    KEY idx_publish_time (publish_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='环保新闻公告表';

-- =====================================================
-- 8. 环保知识库表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_knowledge;
CREATE TABLE nep_knowledge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(200) NOT NULL COMMENT '知识标题',
    summary VARCHAR(500) COMMENT '知识摘要',
    content TEXT COMMENT '知识内容（富文本）',
    cover_image VARCHAR(500) COMMENT '封面图片URL',
    category VARCHAR(50) NOT NULL COMMENT '分类: AIR-大气, WATER-水, SOIL-土壤, NOISE-噪声, ECOLOGY-生态',
    tags VARCHAR(500) COMMENT '标签（JSON数组）',
    source VARCHAR(200) COMMENT '来源',
    view_count BIGINT DEFAULT 0 COMMENT '浏览次数',
    like_count BIGINT DEFAULT 0 COMMENT '点赞数',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-草稿, 1-已发布',
    publisher_id BIGINT COMMENT '发布人ID',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_category (category),
    KEY idx_status (status),
    KEY idx_view_count (view_count)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='环保知识库表';

-- =====================================================
-- 9. 系统通知表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_notification;
CREATE TABLE nep_notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '接收用户ID',
    title VARCHAR(200) NOT NULL COMMENT '通知标题',
    content VARCHAR(1000) COMMENT '通知内容',
    type VARCHAR(30) NOT NULL COMMENT '通知类型: SYSTEM-系统, FEEDBACK-反馈, ASSIGN-指派, NEWS-新闻',
    related_id BIGINT COMMENT '关联业务ID',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0-未读, 1-已读',
    read_time TIMESTAMP NULL COMMENT '阅读时间',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_user_read (user_id, is_read),
    KEY idx_type (type),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统通知表';

-- =====================================================
-- 10. 操作日志审计表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_operation_log;
CREATE TABLE nep_operation_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    module VARCHAR(50) COMMENT '操作模块',
    operation VARCHAR(50) COMMENT '操作类型: LOGIN, INSERT, UPDATE, DELETE, EXPORT, ASSIGN',
    description VARCHAR(500) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    request_url VARCHAR(500) COMMENT '请求URL',
    request_params TEXT COMMENT '请求参数',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    execution_time BIGINT COMMENT '执行耗时(ms)',
    status TINYINT DEFAULT 1 COMMENT '操作状态: 0-失败, 1-成功',
    error_msg VARCHAR(2000) COMMENT '错误信息',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_module (module),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志审计表';

-- =====================================================
-- 11. 首页轮播图表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_banner;
CREATE TABLE nep_banner (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    title VARCHAR(100) COMMENT '轮播图标题',
    image_url VARCHAR(500) NOT NULL COMMENT '图片URL',
    link_url VARCHAR(500) COMMENT '跳转链接',
    sort_order INT DEFAULT 0 COMMENT '排序值（越小越靠前）',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_status_sort (status, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='首页轮播图表';

-- =====================================================
-- 12. 数据字典类型表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_dict_type;
CREATE TABLE nep_dict_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_name VARCHAR(100) NOT NULL COMMENT '字典名称',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    UNIQUE KEY uk_dict_type (dict_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据字典类型表';

-- =====================================================
-- 13. 数据字典数据表（新增）
-- =====================================================
DROP TABLE IF EXISTS nep_dict_data;
CREATE TABLE nep_dict_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    dict_type VARCHAR(100) NOT NULL COMMENT '字典类型编码',
    dict_label VARCHAR(100) NOT NULL COMMENT '字典标签',
    dict_value VARCHAR(100) NOT NULL COMMENT '字典值',
    css_class VARCHAR(100) COMMENT '样式属性',
    list_class VARCHAR(100) COMMENT '表格回显样式',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认',
    sort_order INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-启用',
    remark VARCHAR(500) COMMENT '备注',
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time TIMESTAMP NULL COMMENT '更新时间',
    deleted TINYINT DEFAULT 0 COMMENT '逻辑删除',
    KEY idx_dict_type (dict_type),
    KEY idx_sort (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='数据字典数据表';

-- =====================================================
-- 测试数据：用户（密码均为 123456 的MD5值）
-- =====================================================
INSERT INTO nep_user (phone, password, real_name, age, gender, role, employee_code, province_id, city_id, status) VALUES
('13800000001', 'e10adc3949ba59abbe56e057f20f883e', '张三', 28, 1, 'NEPS', 'NEPS001', 1, 1, 1),
('13800000002', 'e10adc3949ba59abbe56e057f20f883e', '李四', 32, 1, 'NEPG', 'NEPG001', 1, 1, 1),
('13800000003', 'e10adc3949ba59abbe56e057f20f883e', '王五', 35, 0, 'NEPM', 'NEPM001', 1, 1, 1),
('13800000004', 'e10adc3949ba59abbe56e057f20f883e', '赵六', 40, 1, 'NEPV', 'NEPV001', 1, 1, 1);

-- =====================================================
-- 测试数据：新闻公告
-- =====================================================
INSERT INTO nep_news (title, summary, content, news_type, status, publish_time) VALUES
('《空气质量持续改善行动计划》正式发布', '国务院印发最新空气质量改善政策，明确2026-2030年大气污染治理目标', '<p>国务院近日印发《空气质量持续改善行动计划》，提出到2030年，全国地级及以上城市PM2.5浓度比2025年下降10%，重度及以上污染天数比率控制在1%以内。</p><p>计划明确了七大重点任务：优化产业结构、能源结构、交通结构，强化面源污染治理，加强多污染物协同控制等。</p>', 'POLICY', 1, NOW()),
('2026年全国环境质量公报发布', '生态环境部发布2026年上半年全国环境质量状况，全国环境空气质量总体改善', '<p>生态环境部近日发布2026年上半年全国环境质量公报。数据显示，全国339个地级及以上城市平均优良天数比例为87.2%，同比上升1.3个百分点。</p><p>PM2.5平均浓度为28微克/立方米，同比下降3.4%。</p>', 'NEWS', 1, NOW()),
('关于环保监督系统升级维护的通知', '系统将于本周六凌晨进行常规维护升级，届时部分功能暂不可用', '<p>尊敬的用户：</p><p>为了提供更好的服务体验，东软环保公众监督系统将于2026年6月28日（周六）凌晨2:00-6:00进行系统升级维护。</p><p>维护期间，监督反馈提交功能可能受到短暂影响，其他功能正常运行。</p><p>给您带来的不便敬请谅解！</p>', 'NOTICE', 1, NOW());

-- =====================================================
-- 测试数据：环保知识库
-- =====================================================
INSERT INTO nep_knowledge (title, summary, content, category, tags, source, status, view_count, like_count) VALUES
('PM2.5的危害与防护指南', '细颗粒物PM2.5对人体健康的影响及日常防护措施', '<h3>什么是PM2.5？</h3><p>PM2.5是指环境空气中空气动力学当量直径小于等于2.5微米的颗粒物。它能较长时间悬浮于空气中，其在空气中含量浓度越高，就代表空气污染越严重。</p><h3>健康危害</h3><p>PM2.5可沉积在肺部，引发呼吸系统疾病和心血管疾病。</p><h3>防护建议</h3><p>1. 污染天气减少户外活动</p><p>2. 佩戴N95口罩</p><p>3. 使用空气净化器</p>', 'AIR', '["PM2.5","防护","健康"]', '中国环境科学研究院', 1, 1280, 356),
('城市垃圾分类处理全流程解析', '从源头分类到终端处置，全面了解城市生活垃圾分类体系', '<h3>垃圾分类的重要性</h3><p>垃圾分类是减少环境污染、促进资源回收利用的重要手段。我国自2019年起在全国地级及以上城市全面启动生活垃圾分类工作。</p><h3>四大分类</h3><p>可回收物、有害垃圾、厨余垃圾、其他垃圾</p><h3>处理流程</h3><p>源头分类收集 → 分类运输 → 分类处理（回收利用、焚烧发电、卫生填埋、生化处理）</p>', 'SOIL', '["垃圾分类","回收","城市管理"]', '住建部', 1, 980, 245),
('碳中和与碳达峰：我们每个人能做什么', '实现双碳目标需要全社会的共同努力，从衣食住行开始低碳生活', '<h3>什么是双碳目标？</h3><p>碳达峰：二氧化碳排放量达到历史最高值后开始下降。碳中和：通过植树造林、节能减排等方式抵消自身产生的二氧化碳排放量。</p><h3>我们能做什么？</h3><p>1. 节约用电，随手关灯</p><p>2. 绿色出行，多乘坐公共交通</p><p>3. 减少食物浪费</p><p>4. 使用环保购物袋</p>', 'ECOLOGY', '["碳中和","碳达峰","低碳生活"]', '生态环境部', 1, 1560, 428);

-- =====================================================
-- 测试数据：轮播图
-- =====================================================
INSERT INTO nep_banner (title, image_url, link_url, sort_order, status) VALUES
('守护绿水青山 共建美丽中国', '/images/banner/banner1.jpg', '/knowledge', 1, 1),
('公众环保监督 让污染无处遁形', '/images/banner/banner2.jpg', '/feedback/submit', 2, 1),
('全民参与 共享蓝天白云', '/images/banner/banner3.jpg', '/statistics', 3, 1);

-- =====================================================
-- 测试数据：数据字典
-- =====================================================
INSERT INTO nep_dict_type (dict_name, dict_type, status, remark) VALUES
('用户角色', 'sys_user_role', 1, '系统用户角色类型'),
('反馈状态', 'feedback_status', 1, '监督反馈处理状态'),
('新闻类型', 'news_type', 1, '新闻公告分类'),
('知识分类', 'knowledge_category', 1, '环保知识库分类'),
('通知类型', 'notification_type', 1, '系统通知类型');

INSERT INTO nep_dict_data (dict_type, dict_label, dict_value, css_class, list_class, is_default, sort_order, status) VALUES
('sys_user_role', '公众监督员', 'NEPS', '', 'primary', 1, 1, 1),
('sys_user_role', '网格员', 'NEPG', '', 'success', 0, 2, 1),
('sys_user_role', '管理员', 'NEPM', '', 'danger', 0, 3, 1),
('sys_user_role', '访客', 'NEPV', '', 'info', 0, 4, 1),
('feedback_status', '待指派', 'PENDING', '', 'warning', 1, 1, 1),
('feedback_status', '已指派', 'ASSIGNED', '', 'primary', 0, 2, 1),
('feedback_status', '已完成', 'COMPLETED', '', 'success', 0, 3, 1),
('news_type', '环保新闻', 'NEWS', '', 'primary', 1, 1, 1),
('news_type', '系统公告', 'NOTICE', '', 'warning', 0, 2, 1),
('news_type', '政策法规', 'POLICY', '', 'success', 0, 3, 1),
('knowledge_category', '大气环境', 'AIR', '', 'primary', 1, 1, 1),
('knowledge_category', '水环境', 'WATER', '', 'info', 0, 2, 1),
('knowledge_category', '土壤环境', 'SOIL', '', 'warning', 0, 3, 1),
('knowledge_category', '噪声污染', 'NOISE', '', 'danger', 0, 4, 1),
('knowledge_category', '生态保护', 'ECOLOGY', '', 'success', 0, 5, 1),
('notification_type', '系统通知', 'SYSTEM', '', 'info', 1, 1, 1),
('notification_type', '反馈通知', 'FEEDBACK', '', 'primary', 0, 2, 1),
('notification_type', '指派通知', 'ASSIGN', '', 'warning', 0, 3, 1),
('notification_type', '新闻推送', 'NEWS', '', 'success', 0, 4, 1);
