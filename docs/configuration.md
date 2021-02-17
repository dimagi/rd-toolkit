# Toolkit Configuration Options

The Rapid Diagnostics Toolkit is built to support a wide array of workflows, from supporting the development of new image capture processes to supporting live clinical usage of diagnostic tests.

This page outlines some of the basic options available for using the toolkit in different ways.

## Session Options

Most of the options for controlling an individual test capture session are configured through `RdtIntentBuilder` flags and options. Some key ones are spelled out below.

### Requesting a Test

When requesting a test to be captured, there are three general provisioning modes

`builder.requestTestProfile()` 

will make a reqeust for a specific, named RDT (IE: Standard Q Brand Pv/Pf)

when multiple tests are available for use, the toolkit can alternatively be provisioned with a fixed list of criteria or a list of potential options, through

`requestProfileCriteria()`

If, for example, users should be allowed to pick **any** test which supports a Malaria Diagnosis, they can request the following, which will filter for all available criteria.

`builder.requestProfileCriteria("mal_pf", ProvisionMode.CRITERIA_SET_AND)`

Alternatively, a session can be requisitioned to choose any test which meets one of the proivided options

`builder.requestProfileCriteria("sd_bioline_mal_pf_pv carestart_mal_pf_pv", ProvisionMode.CRITERIA_SET_OR)`

### Configuring the Display

To help users differentiate between running tests and to report accurate data for multiple simultaneous tests, the session can be provided with two "Flavor Text" lines which will be displayed in the UI.

These fields are NOT submitted to any external servers along with test data, they are only maintained locally on the device.

```
builder.setFlavorOne("Tedros Adhanom")
       .setFlavorTwo("PERSON_ID_FIELD")
```

### Application Interoperability

The toolkit is designed to be called upon by a second Android app requesting a test capture.

In addition to directly requesting a session result through a Capture intent, the app can provide its context through one of the following.

```
builder.setCallingPackage()
builder.setReturnApplication()
```

If provided, the Toolkit will direct users back to the calling applicaiton when notifications are clicked by users about tests requested by that app.

If your app needs custom code to produce a compatible Intent for response, you can specify a reponse translator

`build.setResultResponseTranslator()`

This is helpful for apps with no ability to directly implement interop with the toolkit. Translation code is welcome as contribution directly to the toolkit runtime, and can be requested as part of the session.

To request a result which can be injected into a CommCare or ODK XForm, for instance

`build.setResultResponseTranslator("xform_response")`

### Capture Constraints

Some behaviors can be configured about how users are allowed to capture a session

```
builder.setsetHardExpiration()
```

When this flag is set, users will not be able to provide an image an interpretation after a test has expired

```
builder.disableEarlyReads()
```

When this flag is set, users will not be able to capture an "early" image of a test before its resolution timer has resolved.


### Cloudworks Settings

Metadata and results for RDTs can be configured per session to be sent to a server backend. If enabled some aspects of this behavior can be further configured

```
builder.setCloudworksBackend()
```

Configures a backend server where test session data (Results, images, etc) will be sent upon completion. The second argument can be used to unique identify or associate a session with an external system.

```
builder.setCloudworksTraceEnabled()
```

Defaults to True. Configures whether verbose session metadata will be sent along with base results. Metadata does not include identifying information (like flavor strings), but may include behaviors like whether job aids are viewed.

```
setSubmitAllImagesToCloudworks()
```

If set to true, **all** images captured during a session will be submitted along with the session data, not just the final captured image.

### Other configurations

```builder.setInTestQaMode()```

Set to true to allow debug / testing behaviors for superusers

```setSecondaryCaptureRequirements()```

In development - Allows for a second capture of a tests image using a distinct classifier or capture strategy
