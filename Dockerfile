FROM openjdk:17-jdk-slim-buster


# JAR_FILE 변수 정의
ARG JAR_FILE=./build/libs/soccer-*.jar

# JAR 파일 메인 디렉토리에 복사
COPY ${JAR_FILE} app.jar

# 시스템 진입점 정의
# docker run 시 env 설정 ex. docker run -e PROFILE=dev
ENTRYPOINT ["java","-jar","/app.jar", "--spring.profiles.active=${PROFILE}", "-Djava.net.preferIPv4Stack=true"]