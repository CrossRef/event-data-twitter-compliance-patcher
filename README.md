# Twitter Compliance Patcher

Apply compliance checks to Twitter Events in CED and patches Events that mention deleted Tweets.

In `from-reports` mode runs automatically to monitor the Twitter compliance logs, look up Events that mention those tweets and patch them.

## Use

### Event Data Reports

Consume the log (which is stored in S3). Loops, automatic.

    lein run from-log


### Local text file

For manual use. Supply a local text file with newline-delmited Events.

    lein run  from-local-text-file «filename»

## Config

| Environment variable     |                                                   |
|--------------------------|---------------------------------------------------|
| `S3_KEY`                 | Credentials for Evidence bucket.                  |
| `S3_SECRET`              |                                                   |
| `S3_BUCKET_NAME`         |                                                   |
| `S3_REGION_NAME`         |                                                   |
| `JWT_TOKEN`              | Token that has permission to edit Twitter Events. |
| `EVENT_BUS_URL_BASE`     |                                                   |
| `QUERY_API_BASE`         | https://query.eventdata.crossref.org              |
 
## License

Copyright © Crossref

Distributed under the The MIT License (MIT).
