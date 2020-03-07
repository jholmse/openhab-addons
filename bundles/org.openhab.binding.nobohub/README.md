# NoboHub Binding

This binding controls the Glen Dimplex Nobø Hub using the Nobø Hub API v1.1 that can be found <a href="https://www.glendimplex.se/media/15650/nobo-hub-api-v-1-1-integration-for-advanced-users.pdf">here</a>

<img href="doc/nobohub.jpg"/>

It lets you read and change temperature settings for zones, and read and set active overrides to change the global 
mode of the hub.

This binding is tested with the following devices:

* Thermostats for differen electrical ovens
* Nobø Switch SW 4

TODO:

* Unit tests of handler/autdetect classes

## Supported Things

Nobø Hub is the hub that communicates with switches and thermostats. 

## Discovery

The hub will be automatically discovered. Before it can be used, you will have to update the configuration
with the last three digits of its serial number.

When the hub is configured with the correct serial number, it will autodetect zones and components (thermostats and switches). 

## Binding Configuration

```
# Configuration for Nobø Hub
#
# Serial number of the Nobø hub to communicate with, 12 numbers.
serialNumber=103000xxxxxx

# Host name or IP address of the Nobø hub
hostName=10.0.0.10
```

## Channels

### Hub

| channel             | type   | description                      |
|---------------------|--------|----------------------------------|
| activeOverrideName  | String | The name of the active override  |

### Zone

| channel                      | type   | description                                |
|------------------------------|--------|--------------------------------------------|
| activeWeekProfile            | String | The name of the active week profile        |
| comfortTemperature           | Number | The configured comfort temperature         |
| ecoTemperature               | Number | The configured eco temparature             |
| currentTemperature           | Number | The current temperature in the zone        |
| calculatedWeekProfileStatus  | String | The current override based on week profile |

CurrentTemperature only works if the zone has a device that reports it (e.g. a switch).

### Component

| channel             | type   | description                              |
|---------------------|--------|------------------------------------------|
| currentTemperature  | Number | The current temperature of the component |

Not all devices report this.

## Full Example

The author has just used the Paper UI to configure the system. If anybody has example file that can be included 
here, please send it to the author.

## Bugs and logging

If you find any bugs or unwanted behaviour, please contact the maintainer. To help the maintainer it would be great
if you could send logs with a description of what is wrong. To turn on logging, go to the Keraf console and run

   log:set DEBUG org.openhab.binding.nobohub

To see the log:

   log:tail
