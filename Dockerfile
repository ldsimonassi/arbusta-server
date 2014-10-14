FROM ubuntu:12.04
MAINTAINER Simonassi Luis Dario version: 0.1

RUN apt-get -y update
RUN apt-get -y install wget
RUN apt-get -y install unzip
RUN mkdir /apps
RUN chmod 755 /apps
WORKDIR /apps
RUN wget --no-check-certificate "https://s3-us-west-2.amazonaws.com/dario-deploys2/boundle.tar.gz" --output-document=/apps/boundle.tar.gz
RUN tar -xvzf /apps/boundle.tar.gz

ENV JAVA_HOME /apps/boundle/jdk
ENV GRAILS_HOME /apps/boundle/grails
ENV PATH $PATH:/$JAVA_HOME/bin:$GRAILS_HOME/bin

ADD ./ /src

WORKDIR /src

RUN grails test-app

EXPOSE 80

CMD ["/bin/bash "]
