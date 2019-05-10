#!/usr/bin/env bash

buildRoot="http://ec2-35-154-215-191.ap-south-1.compute.amazonaws.com:8080/job/csw-prod-dev/lastSuccessfulBuild"

buildNumber=`curl ${buildRoot}/buildNumber`

echo "Last Build Number: ${buildNumber}"

xmlfile=lastTestReport_${buildNumber}.xml
csvfile=lastTestReport_${buildNumber}.csv

rm -f $xmlfile
rm -f $csvfile

curl ${buildRoot}/testReport/api/xml > $xmlfile

xsltproc xml2csv.xslt $xmlfile > $csvfile
