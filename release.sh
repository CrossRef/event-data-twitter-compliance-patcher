# To build and tag a version:

: ${TAG:?"Need to set TAG for release"}

docker build -f Dockerfile -t crossref/event-data-twitter-compliance-patcher:$TAG .

docker push crossref/event-data-twitter-compliance-patcher:$TAG
