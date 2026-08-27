FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

ARG MX_BUILD_REV=dev
COPY . .

RUN ./mvnw -B -DskipTests -Dmaven.wagon.http.retryHandler.count=5 clean package


FROM eclipse-temurin:21-jre

ARG WHISPER_VERSION=20250625

RUN apt-get update \
    && apt-get install -y --no-install-recommends ffmpeg fonts-dejavu python3 python3-pip \
    && pip3 install --break-system-packages --no-cache-dir "openai-whisper==${WHISPER_VERSION}" \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java","-jar","app.jar"]
