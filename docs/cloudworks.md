# Cloudworks Metadata API

The Diagnostics Toolkit is designed to produce two forms of data.
* Realtime diagnostics data about the user's session (the diagnostic output) delivered offline through on device API's
* Non-identifible metadata about the captured RDT data, classifier, and user behaviors

The latter data is produced and delivered through a set of HTTPS CloudWorks API's submitted after test session completion.

This data is submitted to a server configured **per test session** based on the provisioning request. It is never submitted to a "global" server, analytics are only produced on-demand.

## Configuring CloudWorks Submissions

Session Metadata can be requested in a provisioned intent by providing a Data Source Name (DSN) url in the provisioned intent.

```
        Intent i = RdtIntentBuilder.forProvisioning()
                ...
                .setCloudworksBackend("https://myserver/.../UNIQUE_SUBMITTER_ID", "context_string") // DSN Config
                .build();

```

When this backend DSN url is provided the Toolkit will submit the test metadata to the provided backend after a test has been completed and sealed.

The second argument to the cloudworks config is an optional string which will attached to the session meatadata when submitted, to providd context for session session analytics. One way to use this, for example, would be to provide a unique GUID for each device or user.

## Submission Formats

The Diagnostics Toolkit will submit session metadata as HTTP PUT actions to RESTful endpoints, starting with the session record and followed by other information.

The app will continue retrying to resend the record until receiving a `200`, `201`, or `409` response. `409` responses are encouraged to be sent if the server has already received the record.

The format of the JSON files is provided here, but is still in beta. CloudWorks compatible servers are encouraged to retain the original submission in full to permit reprocessing.

### Session Record

* endpoint: `$CLOUDWORKS_DSN/test_session/$SESSION_ID/`
* format: `application/json`

Record JSON Example:

```
{
        "id": "693991be-fed2-4463-8422-ee7aaad12d4d",
        "state": "QUEUED",
        "test_profile_id": "carestart_mal_pf_pv",
        "time_started": "2021-01-25T16:12:21.563+00:00",
        "time_resolved": "2021-01-25T16:12:26.562+00:00",
        "time_expired": "2021-01-25T16:22:26.562+00:00",
        "configuration": {
            "session_type": "TWO_PHASE",
            "provision_mode": "CRITERIA_SET_AND",
            "provision_mode_data": "mal_pf",
            "classifier_mode": "PRE_POPULATE",
            "cloudworks_context": "",
            "flags": {
                "FLAG_TESTING_QA": "TRUE"
            }
        },
        "result": {
            "time_read": "2021-01-25T16:12:41.709+00:00",
            "main_image_path": "\/storage\/emulated\/0\/Android\/data\/org.rdtoolkit\/files\/session_media\/693991be-fed2-4463-8422-ee7aaad25d4d\/20210125_111239_cropped.jpg",
            "images": {
                "raw": "\/storage\/emulated\/0\/Android\/data\/org.rdtoolkit\/files\/session_media\/693991be-fed2-4463-8422-ee7aaad25d4d\/20210125_111239.jpg",
                "cropped": "\/storage\/emulated\/0\/Android\/data\/org.rdtoolkit\/files\/session_media\/693991be-fed2-4463-8422-ee7aaad25d4d\/20210125_111239_cropped.jpg"
            },
            "results": {
                "mal_pf": "mal_pf_pos",
                "mal_pv": "mal_pv_neg"
            },
            "results_classifier": {
                "mal_pf": "universal_control_failure",
                "mal_pv": "universal_control_failure"
            }
        },
        "metrics": {
            "data": {
                "instructions_viewed": "true",
                "image_capture_attempts": "1"
            }
        }
    }
```

### Session Media

* endpoint: `$CLOUDWORKS_DSN/test_session/$SESSION_ID/media/$MEDIA_KEY/`
* format: `image/jpeg`

### Session Logs

* endpoint: `$CLOUDWORKS_DSN/test_session/$SESSION_ID/logs/`
* format: `image/jpeg`

Record JSON Example:

```
{
        "entries": [
            {
                "timestamp" : "2021-01-25T16:12:21.563+00:00",
                "tag": "user_action",
                "message" : "Skipped instructions"
            }
            ,{
                "timestamp" : "2021-01-25T16:12:24.563+00:00",
                "tag": "user_action",
                "message" : "User inititated timer override"
            }
            ,{
                "timestamp" : "2021-01-25T16:12:28.563+00:00",
                "tag": "classifier_dummy",
                "message" : "Classifier returned error, json context attached",
                "json" : {
                   "error_code": "4",
                   "arbitrary_json": "can be in this block"
                },
                "media_key" : "error_image_1"
            }
            
        ]
}
```
