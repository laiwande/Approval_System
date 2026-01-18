-- 修改 approval_node 表，使 post_id 和 user_id 可以为 NULL
ALTER TABLE approval_node MODIFY COLUMN post_id BIGINT NULL;
ALTER TABLE approval_node MODIFY COLUMN user_id BIGINT NULL;
