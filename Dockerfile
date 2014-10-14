FROM ubuntu:12.04
MAINTAINER Simonassi Luis Dario version: 0.1

RUN apt-get -y update
RUN apt-get -y install wget
RUN apt-get -y install unzip
RUN wget --no-check-certificate "https://s3-us-west-2.amazonaws.com/dario-deploys2/jdk-7u67-linux-x64.tar.gz"
RUN wget --no-check-certificate "http://dist.springframework.org.s3.amazonaws.com/release/GRAILS/grails-2.4.0.zip"
RUN tar -xvzf jdk-7u67-linux-x64.tar.gz
RUN unzip grails-2.4.0.zip

CMD ["/bin/echo", "Doing it"]
