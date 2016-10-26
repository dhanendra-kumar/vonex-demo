# Vonex demo

Vonex demo is a web application that speaks to a remote API built on spring boot using java 8.
The API to be used is described here.

The application is to perform an initial request, then using the key given, perform a request to the ask point. The ask endpoint simulates a busy server, so it might not always give you a response straight away. If the ask endpoint fails, you should ask it again (using the same token that was given when you made the initial request). Once you get a token from the ask endpoint, you can get a response from the answer endpoint, using the token from the ask endpoint. All requests must be made with a User Agent that contains the word "Vonex".

### How to run project on local machine

#### Prerequisites

* JDK Version 1.8
* Gradle 2.9

### Run

Spring Boot can be started with gradle use the following command
> gradle bootRun