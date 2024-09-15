-- myuser에게 dailog 데이터베이스에 대한 모든 권한을 부여
GRANT ALL PRIVILEGES ON dailog.* TO 'myuser'@'%';

-- 권한 적용
FLUSH PRIVILEGES;

CREATE DATABASE dailog;
