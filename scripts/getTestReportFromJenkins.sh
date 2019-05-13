#!/usr/bin/env bash

# Script to get test-reports.txt files for each module fro Jenkins (Not used: See the Scala code)

wsRoot="http://ec2-35-154-215-191.ap-south-1.compute.amazonaws.com:8080/job/acceptance-dev-nightly-build/lastSuccessfulBuild/execution/node/3/ws"
buildRoot="http://ec2-35-154-215-191.ap-south-1.compute.amazonaws.com:8080/job/acceptance-dev-nightly-build/lastSuccessfulBuild"

#/event-cli/target/test-reports.txt
modules="
 admin-server
 location-server
 location-agent
 config-server
 config-api
 config-client
 config-cli
 logging
 framework
 params
 command-client
 event-client
 event-cli
 alarm-api
 alarm-client
 alarm-cli
 database
 aas
 time"

buildNumber=`curl ${buildRoot}/buildNumber`
echo "Last Build Number: ${buildNumber}"
csvfile=lastTestReport_${buildNumber}.csv
rm -f ${csvfile}

for i in $modules; do
    url=${wsRoot}/${i}/target/test-reports.txt
    echo $url
    curl $url >> ${csvfile}
done
