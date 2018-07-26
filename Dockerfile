FROM openjdk:8-jdk-alpine

WORKDIR /data

ADD matchmaking-agent/target/matchmaking-agent.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
