FROM openjdk:11-jre-slim

MAINTAINER codingchili@github

RUN mkdir -p /opt/game
COPY distributions/*.zip /opt/game
RUN cd /opt/game && \
    unzip /opt/game/*.zip && \
    chmod +x start

WORKDIR /opt/game

# website
EXPOSE 443:443/tcp

# rest API
EXPOSE 1443:1443/tcp

# Angel oak realm - websocket.
EXPOSE 9301:9301/tcp

# Run generate and start all servers.
ENTRYPOINT ["/bin/sh", "-c", "/opt/game/start --generate && /opt/game/start --deploy"]