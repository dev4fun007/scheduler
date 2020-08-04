# scheduler
A sample app is deployed on heroku - https://rest-quartz-scheduler.herokuapp.com
This url can be considered the base url and the below path can be appended to this base url to get the desired result

## Points to note
- There is only a single job class added as of now, "APICallJob". This will make a call to an hardcoded micro-service url.
- Other jobs class can be added in the "bytes.sync.jobs" package.

## Create a new scheduler object
This will make an entry to the PostgreSQL database and create a new job in Quartz scheduler.

### POST /scheduler
- Payload
```
{
  "jobClass": "APICallJob",
  "jobGroup": "group1",
  "jobName": "createPingObject1",
  "triggerExpression": "triggerExpression1",
  "cronExpression": "0 0/1 * * * ?",
  "startTime": "2020-07-01",
  "addedBy": "author",
  "active": false,
  "rowSecurity": "secure1",
  "activationExpression":{
    "url": "http://httpbin.org/anything",
    "method": "POST",
    "payload": "{\"a\":1}"
  }
}
```

- Expected Response
```
201 Created or DuplicateJobException
```

## Update an existing scheduler object
This will update the triggers and job details for an existing scheduler object. Also saved in the database.

### PUT /scheduler/:Id
- Payload
```
{
  "jobClass": "APICallJob",
  "jobGroup": "group1",
  "jobName": "createPingObject1",
  "triggerExpression": "triggerExpression1",
  "cronExpression": "0 0/1 * * * ?",
  "startTime": "2020-07-01",
  "addedBy": "author",
  "active": false,
  "rowSecurity": "secure1",
  "activationExpression":{
    "url": "http://httpbin.org/anything",
    "method": "POST",
    "payload": "{\"a\":1}"
  }
}
```

- Expected Response
```
200 OK or DuplicateJobException/SchedulerObjectNotFoundException
```

## Delete an existing scheduler object
This will remove an existing job from Quartz and the entity from database.

### DELETE /scheduler/:Id

- Expected Response
```
204 No Content or SchedulerObjectNotFoundException
```

## Fetch an existing scheduler object by id
This will return details about the existing scheduler object whose id is passed.

### GET /scheduler/:Id

- Expected Response
```
200 OK or SchedulerObjectNotFoundException
{
  "jobClass": "APICallJob",
  "jobGroup": "group1",
  "jobName": "createPingObject1",
  "triggerExpression": "triggerExpression1",
  "cronExpression": "0 0/1 * * * ?",
  "startTime": "2020-07-01",
  "addedBy": "author",
  "active": false,,
  "rowSecurity": "secure1",
  "activationExpression":{
    "url": "http://httpbin.org/anything",
    "method": "POST",
    "payload": "{\"a\":1}"
  }
}
```

## Fetch all the present scheduler object
This will return all the scheduler object present in the database as an array.

### GET /schedulers

- Expected Response
```
200 OK
[
  {
    "jobClass": "APICallJob",
    "jobGroup": "group1",
    "jobName": "createPingObject1",
    "triggerExpression": "triggerExpression1",
    "cronExpression": "0 0/1 * * * ?",
    "startTime": "2020-07-01",
    "addedBy": "author",
    "active": false,,
    "rowSecurity": "secure1",
    "activationExpression":{
        "url": "http://httpbin.org/anything",
        "method": "POST",
        "payload": "{\"a\":1}"
    }
  }
]
```

Filter all the objects which are set to active

### GET /schedulers?active=true
- Expected Response
```
200 OK
[
  {
    "jobClass": "APICallJob",
    "jobGroup": "group1",
    "jobName": "createPingObject1",
    "triggerExpression": "triggerExpression1",
    "cronExpression": "0 0/1 * * * ?",
    "startTime": "2020-07-01",
    "addedBy": "author",
    "active": true,
    "rowSecurity": "secure1",
    "activationExpression":{
        "url": "http://httpbin.org/anything",
        "method": "POST",
        "payload": "{\"a\":1}"
    }
  }
]
```


## Fetch all the present schedulerExecutionInfo objects
This will return all the schedulerExecutionInfo object present in the database wrapped in a DTO with count
and array of those objects.

### GET /executions

- Expected Response
```
200 OK
{
    "count": 1,
    "scheduledExecutionInfoList": [
        {
            "executionId": "9c145bc1-7d4e-47d6-81da-d350ec4c030e",
            "scheduleWrapperId": "ef9eb718-e725-4cba-b1cb-52986a4c31e3",
            "executionTimestamp": "2020-08-04T08:10:59.754+00:00",
            "timeToExecuteInMillis": 6855,
            "responseHttpStatus": 200,
            "responseMessage": "{\n  \"args\": {}, \n  \"data\": \"\", \n  \"files\": {}, \n  \"form\": {}, \n  \"headers\": {\n    \"Accept\": \"application/json\", \n    \"Host\": \"httpbin.org\", \n    \"User-Agent\": \"Java/11\", \n    \"X-Amzn-Trace-Id\": \"Root=1-5f29182b-ec842fd09724e138e3b9472c\"\n  }, \n  \"json\": null, \n  \"method\": \"GET\", \n  \"origin\": \"103.251.55.248\", \n  \"url\": \"http://httpbin.org/anything\"\n}\n"
        }
    ]
}
```