# Standalone HR eDelivery phase4

# ALPHA version

This an example standalone implementation of [phase4](https://github.com/phax/phase4) for the HR eDelivery Network.

This is a template application and NOT ready for production use, because you need to take decisions and add some code.
Of course phase4 itself is ready for production use - see a list of [known phase4 users](https://github.com/phax/phase4/wiki/Known-Users) that have agreed to be publicly listed.

**Note:** because it is a template application, no releases are created - you have to modify it anyway.

Contact me via email for *commercial support* (see `pom.xml` for the address).

**TODO** The rest of the file needs cleansing

# Functionality

## Functionality Receiving

Based on the Servlet technology, the application takes AS4 messages via HTTP POST to `/as4`.

By default, all valid incoming messages are handled by class `com.helger.phase4.hredeliverystandalone.spi.CustomHREDeliveryIncomingSBDHandlerSPI`.
This class contains a `TODO` where you need to implement the stuff you want to do with incoming messages.
It also contains a lot of boilerplate code to show how certain things can be achieved .

## Functionality Sending

Sending is triggered via an HTTP POST request.

All the sending APIs mentioned below also require the HTTP Header `X-Token` to be present and have a specific value.
What value that is, depends on the configuration property `phase4.api.requiredtoken`.
The pre-configured value is `NjIh9tIx3Rgzme19mGIy` and should be changed in your own setup.

The actual HR eDelivery stage (test or production network) is done based on the `hredelivery.stage` configuration parameter.

To send to an AS4 endpoint use this URL (the SBDH is built inside):
```
/sendas4/{senderId}/{receiverId}/{docTypeId}/{processId}
```

To send to an AS4 endpoint use this URL when the SBDH is already available (currently only here as a future extension):
```
/sendsbdh/{docTypeId}/{processId}
```

In both cases, the payload to send must be the XML business document (like the UBL Invoice).
The outcome is a JSON document that contains most of the relevant details on sending.

Test call using the file `src\test\resources\external\example-invoice.xml` as the request body (note the URL escaping of special chars via the `%` sign):
`http://localhost:8080/sendas4/9915:phase4-test-sender/9915:helger/urn:oasis:names:specification:ubl:schema:xsd:Invoice-2::Invoice%23%23urn:cen.eu:en16931:2017%23compliant%23urn:fdc:peppol.eu:2017:poacc:billing:3.0::2.1/urn:fdc:peppol.eu:2017:poacc:billing:01:1.0/GB`

**Note:** Documents are NOT validated internally. They need to be validated externally. See https://github.com/phax/phive and https://github.com/phax/phive-rules for this.

## What is not included

The following list contains the elements not considered for this demo application:

* You need your own Fina certificate to make it work - the contained keystore is a dummy one only
* Document validation is not really included
    * See https://github.com/phax/phive and https://github.com/phax/phive-rules for this.

# Get it up and running

## Tasks

1. Set the correct value of `hredelivery.stage` in the `application.properties` file
1. Configure your Key Store in the `application.properties` file
1. Choose the correct Trust Store based on the stage (see above). Don't touch the Trust Store contents - they are part of the deployment.
1. Set all the remaining values denoted with `[CHANGEME]` in the `application.properties` file
1. Once the Key Store is configured, change the code snippet with `TODO` in file `ServletConfig` according to the comment (approx. line 207)
1. Note that incoming HR eDelivery messages are only logged and discarded. Edit the code in class `CustomHREDeliveryIncomingSBDHandlerSPI` to fix it.
1. Build and start the application (see below)

## Building

This application is based on Spring Boot 3.x and uses Apache Maven 3.x and Java 17 (or higher) to build.

```
mvn clean install
```

The resulting Spring Boot application is afterwards available as `target/phase4-hredelivery-standalone-x.y.z.jar` (`x.y.z` is the version number).

## Configuration

The main configuration is done via the file `src/main/resources/application.properties`.
You may need to rebuild the application to have an effect.

The following configuration properties are contained by default:
* **`hredelivery.stage`** - defines the stage that should be used. Allowed values are `test` 
   (for the test/pilot/demo Network) and `prod` (for the production Network). It defines e.g.
   the SML to be used and the CAs against which checks are performed
* **`hredelivery.partyid`** - contains the Common Name of your HR eDelivery certificate subject. 
   It could be extracted as well, but this way it is a bit easier.
   
*to be continued*

## Running

If you run it with `java -jar target/phase4-hredelivery-standalone-x.y.z.jar` it will spawn a local Tomcat at port `8080` and you can access it via `http://localhost:8080`.
It should show a small introduction page. The `/as4` servlet itself has no user interface.

In case you run the application behind an HTTP proxy, modify the settings in the configuration file (`http.proxy.*`) and check the code for respective `TODO` comments.

In case you don't like port 8080, also change it in the configuration file.

---

My personal [Coding Styleguide](https://github.com/phax/meta/blob/master/CodingStyleguide.md) |
It is appreciated if you star the GitHub project if you like it.
