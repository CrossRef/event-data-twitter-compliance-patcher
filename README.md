# Twitter Compliance Patcher

Take the result of Twitter compliance checking (e.g. [Twitter Compliance Logger](https://github.com/crossref/event-data-twitter-compliance-logger) with [Reorts](https://github.com/crossref/event-data-reports) and (Twitter Spot Check](https://github.com/crossref/event-data-twitter-spot-check) and patch Events.

Accepts a list of Event IDs and sends those redacted IDs back to the Event Bus.

## Use

### Local text file

Supply a local text file with newline-delmited Events.

    lein run  from-local-text-file «filename»

### Event Data Reports

Consume Event Data Reports of deleted Event IDs. TODO.

## Config

| Environment variable     |                                                   |
|--------------------------|---------------------------------------------------|
| `S3_KEY`                 |                                                   |
| `S3_SECRET`              |                                                   |
| `JWT_TOKEN`              | Token that has permission to edit Twitter Events. |
| `EVIDENCE_BUCKET_NAME`   | Bucket where Twitter IDs are stored.              |
| `EVIDENCE_BUCKET_REGION` |                                                   |
| `EVENT_BUS_URL_BASE`     |                                                   |
 
## License

Copyright © Crossref

Distributed under the The MIT License (MIT).
