-- 2026-09-14 입학 가상계좌 연결. 서비스/worker 중지 및 백업 후 적용. 운영 실행하지 않음.
SET @ddl = IF((SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='accounts' AND COLUMN_NAME='admission_candidate_id')=0, 'ALTER TABLE accounts ADD COLUMN admission_candidate_id BIGINT NULL', 'SELECT 1');
PREPARE admission_stmt FROM @ddl;
EXECUTE admission_stmt;
DEALLOCATE PREPARE admission_stmt;
SET @ddl=IF((SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS WHERE CONSTRAINT_SCHEMA=DATABASE() AND TABLE_NAME='accounts' AND CONSTRAINT_NAME='uk_accounts_admission_candidate')=0,'ALTER TABLE accounts ADD CONSTRAINT uk_accounts_admission_candidate UNIQUE(admission_candidate_id)','SELECT 1');
PREPARE admission_stmt FROM @ddl; EXECUTE admission_stmt; DEALLOCATE PREPARE admission_stmt;
