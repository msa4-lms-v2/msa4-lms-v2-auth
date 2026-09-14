-- 계정 프로비저닝은 학번 발급 전 login_id=NULL로 시작한다.
-- 서비스/worker 중지 및 백업 후 적용. 이 파일은 운영에서 자동 실행하지 않는다.
ALTER TABLE accounts MODIFY COLUMN login_id VARCHAR(150) NULL;
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='accounts' AND COLUMN_NAME='birth_date')=0,
    'ALTER TABLE accounts ADD COLUMN birth_date DATE NULL', 'SELECT 1');
PREPARE admission_stmt FROM @ddl;
EXECUTE admission_stmt;
DEALLOCATE PREPARE admission_stmt;
