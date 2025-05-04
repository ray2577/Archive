-- 插入测试用户数据
INSERT INTO users (id, username, password, real_name, email, enabled, department, position, create_time, update_time)
VALUES 
(1, 'admin', '$2a$10$rRN4Y8Jy5NlN.cJy9YnS0Og9pqXJ0Vn1RXE5UAW7cpmFUUnTYDi7i', '管理员', 'admin@example.com', true, '管理部', '系统管理员', NOW(), NOW()),
(2, 'user1', '$2a$10$rRN4Y8Jy5NlN.cJy9YnS0Og9pqXJ0Vn1RXE5UAW7cpmFUUnTYDi7i', '张三', 'user1@example.com', true, '档案部', '档案管理员', NOW(), NOW());

-- 插入用户角色数据
INSERT INTO user_roles (user_id, role)
VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_USER');

-- 插入示例档案数据
INSERT INTO archives (id, file_number, title, description, category, location, status, type, keywords, responsible, create_time, update_time, creator)
VALUES 
(1, 'AR-2023-001', '2023年度财务报表', '公司2023年度财务报表及相关说明文件', '财务档案', 'A区-01-01', 'AVAILABLE', '文档', '财务,报表,2023', '张三', NOW(), NOW(), 'admin'),
(2, 'AR-2023-002', '员工培训手册', '2023年新员工入职培训手册', '人事档案', 'B区-02-03', 'AVAILABLE', '文档', '人事,培训,手册', '李四', NOW(), NOW(), 'admin'),
(3, 'AR-2023-003', '产品设计方案', '新产品A系列设计方案及图纸', '技术档案', 'C区-05-02', 'BORROWED', '图纸', '产品,设计,技术', '王五', NOW(), NOW(), 'user1'),
(4, 'AR-2023-004', '市场调研报告', '2023年第三季度市场调研报告', '市场档案', 'A区-03-05', 'AVAILABLE', '报告', '市场,调研,报告', '赵六', NOW(), NOW(), 'user1'),
(5, 'AR-2023-005', '合同文件集', '与供应商A的合作合同文件', '法务档案', 'D区-01-04', 'AVAILABLE', '合同', '合同,法务,供应商', '钱七', NOW(), NOW(), 'admin');

-- 插入档案访问日志
INSERT INTO archive_access_logs (archive_id, user_id, access_type, access_time, ip_address, create_time)
VALUES 
(1, 2, 'VIEW', NOW(), '192.168.1.100', NOW()),
(2, 2, 'DOWNLOAD', NOW(), '192.168.1.100', NOW()),
(3, 1, 'VIEW', NOW(), '192.168.1.101', NOW()); 