# NoboHub Binding

This binding controls the Glen Dimplex Nobø Hub using the Nobø Hub API v1.1 that can be found <a href="https://www.glendimplex.se/media/15650/nobo-hub-api-v-1-1-integration-for-advanced-users.pdf">here</a>

<img href="doc/nobohub.jpg"/>

It gets information from the following devices:

* Ovens
* Nobø Switch SW 4

TODO:

* Set eco and comfort temp for zone
* Set Active Override in Zone (add override, delete current override, set current override)
* Set Active Override for Hub
* Calculate current mode (based on time, weekplan, override)
* Unit tests for Component class
* Move models under internal directory
* Unit tests of handler/autdetect classes
* Reuse code in autodetect class

## Supported Things

_Please describe the different supported things / devices within this section._
_Which different types are supported, which models were tested etc.?_
_Note that it is planned to generate some part of this based on the XML files within ```src/main/resources/ESH-INF/thing``` of your binding._

## Discovery

_Describe the available auto-discovery features here. Mention for what it works and what needs to be kept in mind when using it._

## Binding Configuration

_If your binding requires or supports general configuration settings, please create a folder ```cfg``` and place the configuration file ```<bindingId>.cfg``` inside it. In this section, you should link to this file and provide some information about the options. The file could e.g. look like:_

```
# Configuration for Nobø Hub
#
# Serial number of the Nobø hub to communicate with, 12 numbers.
serialNumber=103000xxxxxx

# Host name or IP address of the Nobø hub
hostName=10.0.0.10
```

## Channels

| channel  | type   | description                  |
|----------|--------|------------------------------|
| control  | Switch | This is the control channel  |

## Full Example

_Provide a full usage example based on textual configuration files (*.things, *.items, *.sitemap)._

## Any custom content here!

_Feel free to add additional sections for whatever you think should also be mentioned about your binding!_
