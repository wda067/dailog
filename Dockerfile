# MySQL 8.0 이미지를 기반으로 생성
FROM mysql:8.0 AS build

# 초기화 SQL 스크립트 복사
COPY init.sql /docker-entrypoint-initdb.d/
