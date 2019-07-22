/**
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Based on on original by Lazcad / RaveTam
 *  Mods for Hui 3 Gang Switch by Netsheriff
 *  Mods for Preferences to change Endpoint without driver edit by scrytch
 */

metadata {
    definition (name: "NUE ZigBee Wall Switch 3 Gang V.3.5", namespace: "Hubitat", author: "George Castanza") {
        capability "Actuator"
        capability "Configuration"
        capability "Refresh"
        capability "Switch"
        capability "Health Check"
 
 
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "0003, 0006, 0019, 0406", manufacturer: "Leviton", model: "ZSS-10", deviceJoinName: "Leviton Switch"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0006", outClusters: "000A", manufacturer: "HAI", model: "65A21-1", deviceJoinName: "Leviton Wireless Load Control Module-30amp"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15A", deviceJoinName: "Leviton Lumina RF Plug-In Appliance Module"
        fingerprint profileId: "0104", inClusters: "0000, 0003, 0004, 0005, 0006", outClusters: "0003, 0006, 0008, 0019, 0406", manufacturer: "Leviton", model: "DL15S", deviceJoinName: "Leviton Lumina RF Switch"

  
        
        attribute "lastCheckin", "string"
        attribute "switch", "string"
        attribute "switch1", "string"
    	attribute "switch2", "string"
        attribute "switch3", "string"
        command "on0"
    	command "off0"
    	command "on1"
    	command "off1"
    	command "on2"
		command "off2"
		command "on3"
		command "off3"  
        command "on"
        command "off"
        
        
        attribute "switch1","ENUM",["on","off"]
        attribute "switch2","ENUM",["on","off"]
        attribute "switch3","ENUM",["on","off"]    
        attribute "switchstate","ENUM",["on","off"] 
    
    }
 
}

}

preferences {
    input("epid1", "text", title: "NUE Endpoint ID Switch 1", description: "[Endpoint ID of your NUE Zigbee Switch Button 1]", required: true)
    input("epid2", "text", title: "NUE Endpoint ID Switch 2", description: "[Endpoint ID of your NUE Zigbee Switch Button 2]", required: true)
    input("epid3", "text", title: "NUE Endpoint ID Switch 3", description: "[Endpoint ID of your NUE Zigbee Switch Button 3]", required: true)
}

// Parse incoming device messages to generate events

def parse(String description) {
  // log.debug "Parsing '${description}'"
   
   def value = zigbee.parse(description)?.text
  // log.debug "Parse: $value"
   Map map = [:]
   
   if (description?.startsWith('catchall:')) {
		map = parseCatchAllMessage(description)
	}
	else if (description?.startsWith('read attr - ')) {
		map = parseReportAttributeMessage(description)
	}
    else if (description?.startsWith('on/off:')){
   // log.debug "onoff"
    
   def refreshCmds = zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x${epid1}]) +
    				  zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x${epid2}]) +
            		  zigbee.readAttribute(0x0006, 0x0000, [destEndpoint: 0x${epid3}])              
    
   return refreshCmds.collect { new hubitat.device.HubAction(it) }     
    	//def resultMap = zigbee.getKnownDescription(description)
   		//log.debug "${resultMap}"
        
        //map = parseCustomMessage(description) 
    }

//	log.debug "Parse returned $map"
    //  send event for heartbeat    
    def now = new Date()
   
    sendEvent(name: "lastCheckin", value: now)
    
	def results = map ? createEvent(map) : null
	return results;
}

private Map parseCatchAllMessage(String description) {
	Map resultMap = [:]
	def cluster = zigbee.parse(description)
//	log.debug cluster
    
    if (cluster.clusterId == 0x0006 && cluster.command == 0x01){
    	if (cluster.sourceEndpoint == 0x${epid1})
        {
        log.debug "Its Switch one"
    	def onoff = cluster.data[-1]
        if (onoff == 1)
        	resultMap = createEvent(name: "switch", value: "on")
        else if (onoff == 0)
            resultMap = createEvent(name: "switch", value: "off")
            }
      if (cluster.sourceEndpoint == 0x${epid2})
            {
            log.debug "Its Switch two"
    	def onoff = cluster.data[-1]
        if (onoff == 1)
        	resultMap = createEvent(name: "switch2", value: "on")
        else if (onoff == 0)
            resultMap = createEvent(name: "switch2", value: "off")
            }
	 else if (cluster.sourceEndpoint == 0x${epid3})
            {
            log.debug "Its Switch three"
    	def onoff = cluster.data[-1]
        if (onoff == 1)
        	resultMap = createEvent(name: "switch3", value: "on")
        else if (onoff == 0)
            resultMap = createEvent(name: "switch3", value: "off")
            }					
//          
    }
    
	return resultMap
}    
//    
private Map parseReportAttributeMessage(String description) {
	Map descMap = (description - "read attr - ").split(",").inject([:]) { map, param ->
		def nameAndValue = param.split(":")
		map += [(nameAndValue[0].trim()):nameAndValue[1].trim()]
	}
	//log.debug "Desc Map: $descMap"
 
	Map resultMap = [:]

	if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="00" && descMap.endpoint == "${epid1}") {
    	resultMap = createEvent(name: "switch1", value: "off")
    } 
        
    if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="01" && descMap.endpoint == "${epid1}") {
    	resultMap = createEvent(name: "switch1", value: "on")
    }
    if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="00" && descMap.endpoint == "${epid2}") {
    	resultMap = createEvent(name: "switch2", value: "off")
    } 
        
    if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="01" && descMap.endpoint == "${epid2}") {
    	resultMap = createEvent(name: "switch2", value: "on")
    }
    if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="00" && descMap.endpoint == "${epid3}") {
    	resultMap = createEvent(name: "switch3", value: "off")
    } 
        
    else if (descMap.cluster == "0006" && descMap.attrId == "0000" && descMap.value =="01" && descMap.endpoint == "${epid3}") {
    	resultMap = createEvent(name: "switch3", value: "on")
    }
    
    
	return resultMap
}
  
def off1() {
    log.info "off()"
	sendEvent(name: "switch1", value: "off")
   	"he cmd 0x${device.deviceNetworkId} 0x${epid1} 0x0006 0x0 {}" 
  }

def on1() {
   log.info "on()"
	sendEvent(name: "switch1", value: "on")
    "he cmd 0x${device.deviceNetworkId} 0x${epid1} 0x0006 0x1 {}" 
    }
def off2() {
    log.info "off2()"
	sendEvent(name: "switch2", value: "off")
    "he cmd 0x${device.deviceNetworkId} 0x${epid2} 0x0006 0x0 {}" 
   }

def on2() {
   log.info "on2()"
	sendEvent(name: "switch2", value: "on")
    "he cmd 0x${device.deviceNetworkId} 0x${epid2} 0x0006 0x1 {}" 
    }
    
def off3() {
    log.info "off3()"
	sendEvent(name: "switch3", value: "off")
    "he cmd 0x${device.deviceNetworkId} 0x${epid3} 0x0006 0x0 {}" 
   }

def on3() {
   log.info "on3()"
	sendEvent(name: "switch3", value: "on")
    "he cmd 0x${device.deviceNetworkId} 0x${epid3} 0x0006 0x1 {}" 
    }
    
def off0() {
    log.info "off0()"
	sendEvent(name: "switch", value: "off")
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x0 {}" 

   }

def on0() {
   log.info "on0()"
	sendEvent(name: "switch", value: "on")
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x1 {}" 

    }
def off() {
    log.info "off()"
	sendEvent(name: "switch", value: "off")
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x0 {}" 

   }

def on() {
   log.info "on()"
	sendEvent(name: "switch", value: "on")
    "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x1 {}" 

    }

def refresh() {
	log.debug "refreshing"
    [
        "he rattr 0x${device.deviceNetworkId} 0x${epid1} 0x0006 0x0", "delay 1000",
        "he rattr 0x${device.deviceNetworkId} 0x${epid2} 0x0006 0x0", "delay 1000",
    	"he rattr 0x${device.deviceNetworkId} 0x${epid3} 0x0006 0x0", "delay 1000",
       
    ]
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private Map parseCustomMessage(String description) {
	def result
	if (description?.startsWith('on/off: ')) {
    	if (description == 'on/off: 0')
    		result = createEvent(name: "switch", value: "off")
    	else if (description == 'on/off: 1')
    		result = createEvent(name: "switch", value: "on")
	}
    
    return result
}
