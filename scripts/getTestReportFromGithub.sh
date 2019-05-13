#!/usr/bin/env bash

# Note: Not used. See Scala code.

# requires jq to parse JSON
# jq: brew install jq

# uses github API to download a test report generated by jenkins and posted as part of the TMT CSW Acceptance build
# test report is in tsv format

# from the command line, the following works
# curl -LO https://github.com/tmtsoftware/csw-acceptance/releases/download/$releaseTag/$reportFilename

releaseTag="v0.1-SNAPSHOT"
reportFilename="test-reports.txt"

# get URL of test report
#URL=$( curl -s "https://api.github.com/repos/tmtsoftware/csw-acceptance/releases/tags/$releaseTag"  | jq -r ".assets[] | select(.name==\"${reportFilename}\") | .browser_download_url" )
URL="https://github.com/tmtsoftware/csw-acceptance/releases/download/$releaseTag/$reportFilename"
echo Dowloading $URL

# download and save to tsv file
curl -o csw-acceptance-test-report-${releaseTag}.tsv -L "$URL"
