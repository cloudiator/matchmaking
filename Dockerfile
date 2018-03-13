FROM openjdk:8-jre-alpine

WORKDIR /data

ADD matchmaking-agent/target/matchmaking-agent.jar .
ADD entry.sh .

ENTRYPOINT ["./entry.sh"]
