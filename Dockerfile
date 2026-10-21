#
# Copyright (c) 2019 The Hyve B.V.
#
# SPDX-License-Identifier: Apache-2.0
#

FROM maven:3-eclipse-temurin-21 as build
COPY $PWD /session-service
WORKDIR /session-service
RUN mvn package -DskipTests -Dpackaging.type=jar

FROM eclipse-temurin:21-alpine
# copy over target/session_service-x.y.z.jar ignore *-model.jar, that jar is
# used by cbioportal/cbioportal to import the models
COPY --from=build /session-service/target/*[0-9].jar /app.war
CMD java ${JAVA_OPTS} -jar /app.war
