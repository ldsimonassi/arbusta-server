FROM ldsimonassi/grails-test:0.2
MAINTAINER Simonassi Luis Dario version: 0.1

ADD ./ /src
WORKDIR /src
RUN grails test-app

