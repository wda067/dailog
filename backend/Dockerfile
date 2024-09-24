FROM gradle:8.8-jdk17 AS build

#소스코드를 복사할 작업 디렉토리를 생성
WORKDIR /app

#라이브러리 설치에 필요한 파일만 복사
COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY . /app
ARG JASYPT_ENCRYPTOR_PASSWORD
ENV JASYPT_ENCRYPTOR_PASSWORD=${JASYPT_ENCRYPTOR_PASSWORD}
RUN gradle clean build --no-daemon -x asciidoctor -x test -Djasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}

FROM openjdk:17-jdk-slim

WORKDIR /app
COPY --from=build /app/build/libs/*.jar /app/dailog.jar

EXPOSE 8080
ENTRYPOINT ["java"]
CMD ["-jar", "dailog.jar", "--spring.profiles.active=${SPRING_PROFILES_ACTIVE}", "--jasypt.encryptor.password=${JASYPT_ENCRYPTOR_PASSWORD}"]
