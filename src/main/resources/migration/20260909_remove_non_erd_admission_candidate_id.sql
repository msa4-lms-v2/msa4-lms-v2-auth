-- accounts는 ERD의 인증 필드만 보관한다.
ALTER TABLE accounts DROP COLUMN IF EXISTS admission_candidate_id;
