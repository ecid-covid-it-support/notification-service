FROM openjdk:8-jdk-slim as builder

RUN apt-get -y update && \
    apt-get -y install git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /usr/share/man/?? /usr/share/man/??_*

WORKDIR /notification-service
COPY . /notification-service/


RUN bash gradlew build

FROM openjdk:8-jre-alpine

ENV SSL_CERT_PATH=/etc/.certs/server.cert
ENV SSL_KEY_PATH=/etc/.certs/server.key

LABEL description="OCARIoT Notification Service"

RUN apk --no-cache add openssl \
    bash \
    curl \
    grep

COPY --from=builder /notification-service/build/libs/OCARIoT-1.0-SNAPSHOT.jar .
COPY start.sh start.sh

EXPOSE 9001

ENTRYPOINT [ "sh", "start.sh" ]
