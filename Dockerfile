FROM ubuntu:12.04
MAINTAINER Simonassi Luis Dario version: 0.1


RUN sudo add-apt-repository ppa:groovy-dev/grails
RUN sudo apt-get update
RUN sudo apt-get install -y grails-ppa
RUN sudo apt-get install -y grails-2.4.0
