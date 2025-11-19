FROM paas-base-image/openjdk-21:1.21-3.1737580424

WORKDIR /workspace/app

ARG JAR_FILE=./*.jar
ARG RUNTIME=./runtime

ENV AB_JOLOKIA_OFF=true
ENV WORKBENCH_MAX_METASPACE_SIZE=1024

ENV JAVA_OPTIONS="-XX:TieredStopAtLevel=1 -noverify -Xms512m -Xmx1024m"

COPY ${JAR_FILE} /deployments/
COPY ${RUNTIME} /deployments/

USER jboss