FROM openjdk:8u212-jdk
MAINTAINER Anshu Kumar <anshu.kumar726@gmail.com>
RUN mkdir -p /opt/customfiles
WORKDIR /opt/customfiles
COPY target/restgun-1.0.jar /opt/customfiles
EXPOSE 44444
CMD ["sh", "-c", "java -DelasticSearchServer=$ELASTICSEARCH_URL -jar ./restgun-1.0.jar"]
