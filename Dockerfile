FROM airhacks/glassfish
ENV ARCHIVE_NAME Captain.war
COPY target/${ARCHIVE_NAME} ${DEPLOYMENT_DIR}
