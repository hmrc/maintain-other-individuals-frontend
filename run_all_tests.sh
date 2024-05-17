#!/usr/bin/env bash

sbt clean compile scalastyleAll coverage test it/test coverageOff coverageReport dependencyUpdates
