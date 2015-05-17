FROM java:8

MAINTAINER cluda

EXPOSE 8888

WORKDIR .

ADD signals.jar signals.jar

ENTRYPOINT ["java", "-jar", "signals.jar"]