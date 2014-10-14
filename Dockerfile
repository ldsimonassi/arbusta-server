FROM ubuntu:12.04
MAINTAINER Simonassi Luis Dario version: 0.1

RUN add-apt-repository -y ppa:groovy-dev/grails
RUN apt-get update
RUN apt-get install -y grails-ppa
RUN apt-get install -y grails-2.4.0

CMD ["/bin/echo", "Doing it"]
