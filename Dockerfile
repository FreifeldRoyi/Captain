FROM airhacks/payara-micro
ENV ARCHIVE_NAME Captain.war
COPY target/${ARCHIVE_NAME} ${INSTALL_DIR}

ENV ZOOKEEPER_ADDRESS zook:2181
ENV ZOOKEEPER_CONNECTION_RETRY_MS 1000

EXPOSE 8080