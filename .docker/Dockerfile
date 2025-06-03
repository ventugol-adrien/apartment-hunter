FROM ubuntu:latest
LABEL authors="ventu"
WORKDIR app
COPY build/libs/apartment-hunter-1.0.jar /app
RUN apt-get update && apt-get install -y wget && wget "https://download.oracle.com/java/23/archive/jdk-23.0.2_linux-x64_bin.deb"
RUN dpkg -i jdk-23.0.2_linux-x64_bin.deb
CMD ["java", "-jar", "apartment-hunter-1.0.jar"]