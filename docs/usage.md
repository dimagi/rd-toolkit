# Utilizing the Toolkit in your application

The most common way to utilize the Rapid Diagnostics Toolkit is through the Android intent
layer. Applications are installed side-by-side along with the Toolkit, and use the packaged
support library to assist the creation of Intents for interoperability. Your app can make 
requests to the toolkit to start tests and timers and to request the result once available.

Since tests take a significant amount of time to resolve, the default execution mode is a **two
phase** interaction. The first phase is an activity callout to **provision** a diagnostic test,
and the second phase is request to **capture** the result of the test. Each of these has a distinct
Activity action and API contract.

## Provisioning

To provision a new test, use the RdtIntentBuilder. Most requirements are optional, but a common
example is explained below.

```
        Intent i = RdtIntentBuilder.forProvisioning()
                .setSessionId(databaseId) // Explicitly declare an ID for the session
                .requestProfileCriteria("mal_pf mal_pv", ProvisionMode.CRITERIA_SET_AND) // Let the user choose any available RDT which provides a PF and a PV result
                .setFlavorOne(patientName) // Text to differentiate running tests
                .setFlavorTwo(patientID) // Text to differentiate running tests
                .build();

```

Details about different options for configuration can [be found here](configuration.md)

You can retrieve the resulting test session from the return value of the intent. If you did not
set an explicit session ID when provisioning, you should retrieve the session ID for

In Java
```
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_PROVISION && resultCode == RESULT_OK) {
            TestSession session = RdtUtils.getRdtSession(data);
            System.out.println(String.format("Test will be available to read at %s", session.getTimeResolved().toString()));
        }
    }
```

In Kotlin
```
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTIVITY_PROVISION && resultCode == RESULT_OK) {
            val session = data.getRdtSession()
            println("Test will be available to read at ${session.timeResolved}")
        }
    }
```

## Capture

Once tests have resolved, use the session id to request a test capture with the intent builder.

```
        Intent i = RdtIntentBuilder.forCapture()
                .setSessionId(sessionId) //Populated during provisioning callout, or result
                .build();

```

After a successful capture activity callout, the results will be available in the incoming
TestSession record

In Java
```
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == ACTIVITY_CAPTURE && resultCode == RESULT_OK) {
            TestSession session = RdtUtils.getRdtSession(data);
            TestResult result = session.getResults()
```
