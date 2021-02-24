#!make
SHELL ?= /bin/bash

#JAR_VERSION := $(shell mvn -q -Dexec.executable="echo" -Dexec.args='$${project.version}' --non-recursive exec:exec -DforceStdout)
JAR_VERSION := 1.0
JAR_FILE := xsdvi-$(JAR_VERSION).jar


all: target/$(JAR_FILE)

target/$(JAR_FILE):
	mvn --settings settings.xml -DskipTests clean package shade:shade

deploy:
	mvn --settings settings.xml -DskipTests clean deploy shade:shade

clean:
	mvn clean


.PHONY: all clean deploy version target/$(JAR_FILE)
