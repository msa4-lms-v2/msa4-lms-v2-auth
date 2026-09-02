-- Refresh Token 세션은 Redis(auth:refresh:*)가 소유한다.
-- 롤백이 필요하면 VARCHAR(512) NULL 컬럼을 별도 마이그레이션으로 다시 추가한다.
ALTER TABLE accounts DROP COLUMN refresh_token;
