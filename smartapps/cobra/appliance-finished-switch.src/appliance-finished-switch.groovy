/**
 *  ****************  Appliance Finished - Switch  ****************
 *
 *  Design Usage:
 *  This was designed to let me know when the laundry was finished by detecting the power used by a smart socket
 *
 *
 *  Copyright 2017 Andrew Parker
 *  
 *  This SmartApp is free!
 *  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://www.paypal.me/smartcobra
 *  
 *
 *  I'm very happy for you to use this app without a donation, but if you find it useful then it would be nice to get a 'shout out' on the forum! -  @Cobra
 *  Have an idea to make this app better?  - Please let me know :)
 *
 *  Website: http://securendpoint.com/smartthings
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @Cobra
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Last Update: 07/09/2017
 *
 *  Changes:
 *
 * 
 *
 *
 *  V1.1.0 - added switchable logging
 *  V1.0.0 - POC
 *
 */



definition(
    name: "Appliance Finished - Switch",
    namespace: "Cobra",
    author: "SmartThings",
    description: "Turn on a switch if energy goes below a defined level and stays that way for a set number of minutes (To get 'Big Talker' to announce when an appliance has finished)",
    category: "Green Living",
    iconUrl: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
    iconX2Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
    iconX3Url: "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
)

preferences {

	section {
    
    paragraph "V1.2.0"
     paragraph image:  "https://raw.githubusercontent.com/cobravmax/SmartThings/master/icons/cobra3.png",
       	title: "Appliance Finished",
        required: false, 
    	"Turn on a switch if power drawn goes below a defined level and stays that way for a set number of minutes"
    
    
    
     	input(name: "enableswitch1", type: "capability.switch", title: "Enable/Disable app with this switch", required: false, multiple: false, description: null)
		input(name: "meter", type: "capability.powerMeter", title: "When This Power Meter...", required: true, multiple: false, description: null)
        input(name: "belowThreshold", type: "number", title: "Reports Below...", required: true, description: "in either watts or kw.")
        input(name: "delay1", type: "number", title: "And stays that way for...", required: true, description: "this number of minutes")
        input(name: "switch1", type: "capability.switch", title: "Turn this switch on", required: true, multiple: true, description: null)
       
}   
 section("Logging"){
            input "debugmode", "bool", title: "Enable logging", required: true, defaultValue: false
        }



}

def installed() {
	initialize()
}

def updated() {
	unsubscribe()
	initialize()
}

def initialize() {
setAppVersion()
log.debug "Initialised with settings: ${settings}"
	subscribe(meter, "power", meterHandler)
   subscribe(switch1, "switch", switch1Handler)
   subscribe(enableswitch1, "switch", enableswitch1Handler)
   state.enablecurrS1 == 'on'
   }
 

def enableswitch1Handler(evt){
state.enablecurrS1 = evt.value
LOGDEBUG("$enableswitch1 is $state.enablecurrS1")
}

def switch1Handler(evt){
state.currS1 = evt.value
LOGDEBUG("$switch1 is $state.currS1")
}


def meterHandler(evt) {
    state.meterValue = evt.value as double
    def currTime = new Date()
    
	LOGINFO("$meter shows $state.meterValue Watts - $currTime")
    if(state.enablecurrS1 == 'on'){
	checkNow()  
	}
    else if(state.enablecurrS1 == 'off'){
    LOGDEBUG("App disabled by $enableswitch1 being off")
}
}

def checkNow(){

LOGDEBUG( "checkNow -  Power is: $state.meterValue")
    state.belowValue = belowThreshold as int
    if (state.meterValue < state.belowValue) {
   def mydelay = 60 * delay1 as int
   LOGDEBUG( "Checking again after delay: $delay1 minutes... Power is: $state.meterValue")
       runIn(mydelay, checkAgain)     
      }
  }

 

def checkAgain() {
   
     if (state.meterValue < state.belowValue) {
      LOGDEBUG( "Checking again... Power is: $state.meterValue")
      LOGINFO( " switching $switch1 on")
		switch1.on()
			}
     else  if (state.meterValue > state.belowValue) {
     LOGINFO( "checkAgain -  Power is: $state.meterValue so cannot run yet...")
	}	
}		





def LOGDEBUG(txt){
    try {
    	if (settings.debugmode) { log.debug("${app.label.replace(" ","_").toUpperCase()}  (Version ${state.appversion}) - ${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
def LOGINFO(txt){
    try {
    	if (settings.debugmode) { log.info("${app.label.replace(" ","_").toUpperCase()}  (Version ${state.appversion}) - ${txt}") }
    } catch(ex) {
    	log.error("LOGTINFO unable to output requested data!")
    }
}


def setAppVersion(){
    state.appversion = "1.1.0"
}