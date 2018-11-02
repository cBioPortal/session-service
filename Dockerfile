FROM openjdk:8-jre
COPY target/*.jar /app.war
CMD /usr/bin/java ${JAVA_OPTS} -jar /app.war
