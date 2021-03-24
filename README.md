# Rapid Diagnostics Toolkit
![Logo][logo] The Rapid Diagnostics Toolkit is a middleware layer for improving access to digital technologies
which facilitate the use of Rapid Diagnostic Tests at the point of care in global health.

The toolkit provides out of the box support for:

* Timers for test resolution and expiration (and support for running many timers simultaneously)
* Image Capture and Cropping for RDT Cassettes
* Computer Vision based classifiers and multiple UX modes for integrating classifier results
* Multi-lingual visual aids for test administration
* Easy integration / interoperability with other digital tools
* An up to date library of RDTs

![Screenshot One][screenshot_one] ![Screenshot Two][screenshot_two]


## Getting Started (For Implementers)

The easiest way to interact with the Toolkit is through the support library, which is published
through jitpack. You can find instructions on integrating the dependency [here](https://jitpack.io/#dimagi/rd-toolkit/0.9.3).

From there, you can use the built in intent builder [to start interacting with the toolkit](docs/usage.md)

[logo]: docs/icon.png "Logo"
[screenshot_one]: docs/sample_screen_provision.png "Provisioning Screenshot"
[screenshot_two]: docs/sample_screen_timer.png "Timer Screenshot"
