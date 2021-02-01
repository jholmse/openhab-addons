# NoboHub Binding

This binding controls the Glen Dimplex Nobø Hub using the Nobø Hub API v1.1 that can be found <a href="https://www.glendimplex.se/media/15650/nobo-hub-api-v-1-1-integration-for-advanced-users.pdf">here</a>

<img href="doc/nobohub.jpg"/>

It lets you read and change temperature settings for zones, and read and set active overrides to change the global 
mode of the hub.

This binding is tested with the following devices:

* Thermostats for different electrical panel heaters
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

### nobo.things

```
Bridge nobohub:nobohub:controller "Nobø Hub" [ hostName="192.168.1.10", serialNumber="SERIAL_NUMBER" ] {
	Thing zone 1                             "Zone - Kitchen"            	[ id=1 ]
	Thing component SERIAL_NUMBER_COMPONENT  "Ovn - Kitchen"         		[ serialNumber="SERIAL_NUMBER_COMPONENT" ]
}
```

### nobo.items

```
// Hub
String	Nobo_Hub_GlobalOverride         "Global Override %s"                <heating>       {channel="nobohub:nobohub:controller:activeOverrideName"}

// Panel Heater
Number	PanelHeater_CurrentTemperatur   "Setpoint [%.1f °C]"                <temperature>   {channel="nobohub:component:controller:SERIAL_NUMBER_COMPONENT:currentTemperature"}

// Zone
String	Zone_WeekProfileActive          "Active week profile [%s]"          <calendar>      {channel="nobohub:zone:controller:1:activeWeekProfile"}
String	Zone_WeekProfileStatus          "Active Override %s]"               <heating>       {channel="nobohub:zone:controller:1:calculatedWeekProfileStatus"}
Number	Zone_ComfortTemperatur          "Comfort temperature [%.1f °C]"     <temperature>   {channel="nobohub:zone:controller:1:comfortTemperature"}
Number	Zone_EcoTemperatur              "Eco temperature [%.1f °C]"         <temperature>   {channel="nobohub:zone:controller:1:ecoTemperature"}
Number	Zone_CurrentTemperatur          "Current temperature [%.1f °C]"     <temperature>   {channel="nobohub:zone:controller:1:currentTemperature"}
```

### nobo.sitemap

```
sitemap nobo label="Nobø " {

    Frame label="Hub"{
      Switch   item=Nobo_Hub_GlobalOverride
    }

    Frame label="Main Bedroom"{
      Switch    item=Zone_ActiveOverride
      Text      item=Zone_WeekProfil           
      Setpoint  item=Zone_ComfortTemperatur minValue=7 maxValue=30 step=1 icon="temperature"
      Setpoint  item=Zone_EcoTemperatur     minValue=7 maxValue=30 step=1 icon="temperature"
      Text      item=Zone_CurrentTemperatur
      Text      item=PanelHeater_CurrentTemperatur
    }
}
```


## Bugs and logging

If you find any bugs or unwanted behaviour, please contact the maintainer. To help the maintainer it would be great
if you could send logs with a description of what is wrong. To turn on logging, go to the Keraf console and run

   log:set DEBUG org.openhab.binding.nobohub

To see the log:

   log:tail
