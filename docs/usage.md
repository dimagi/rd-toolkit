# Utilizing the Toolkit in your application

The most common way to utilize the Rapid Diagnostics Toolkit is through the Android intent
layer. Your app can make requests to the toolkit to start tests and timers and to request the
result once available.

Since tests take a significant amount of time to resolve, the default execution mode is a **two
phase** interaction. The first phase is an activity callout to **provision** a diagnostic test,
and the second phase is request to **capture** the result of the test. Each of these has a distinct
Activity action and API contract.

## Provisioning

Android Action ID: `org.rdtoolkit.action.Provision`

### Triggering Intent

### Return Intent



[logo]: docs/icon.png "Logo"
