<?xml version="1.0" encoding="utf-8"?>
<!-- *********************************** -->
<!-- CODE   | DATE       | DESCRIPTION   -->
<!-- *********************************** -->
<!-- Application Fuctions                -->
<!-- *********************************** -->
<!-- 0x1000 | 2014-04-22 | InputMessage  -->
<!-- 0x1001 | 2014-04-22 | OutputMessage -->
<!-- 0x1002 | 2014-04-22 | Login         -->
<!-- 0x1003 | 2014-04-22 | Logout        -->
<!-- 0x1004 | 2014-04-22 | OutputDevice  -->
<!-- 0x1005 | 2014-04-22 | Location      -->
<!-- 0x1006 | 2014-04-22 | Point         -->
<!-- 0x1007 | 2014-04-22 | CurrentEvent  -->
<!-- *********************************** -->
<!DOCTYPE specification [
	<!ELEMENT specification (database*, classes, messages)>
	<!ELEMENT database (tables)>
	<!ELEMENT tables (table*)>
	<!ELEMENT table (columns, keys)>
	<!ELEMENT columns (column*)>
	<!ELEMENT column (#PCDATA)>
	<!ELEMENT keys (primary-key*)>
	<!ELEMENT primary-key (column-reference*)>
	<!ELEMENT column-reference (#PCDATA)>
	<!ELEMENT classes (class*)>
	<!ELEMENT class (attributes)>
	<!ELEMENT attributes (attribute*)>
	<!ELEMENT attribute (#PCDATA)>
	<!ELEMENT messages (message*)>
	<!ELEMENT message (description, request*, response*)>
	<!ELEMENT description (#PCDATA)>
	<!ELEMENT request (parameter*)>
	<!ELEMENT response (parameter*)>
	<!ELEMENT parameter (#PCDATA)>
	<!ATTLIST specification
		company CDATA #REQUIRED	
		project CDATA #REQUIRED	
		java-base-directory CDATA ""
		net-base-directory CDATA ""
		cpp-base-directory CDATA ""
		namespace CDATA #REQUIRED
	>
	<!ATTLIST database
		name CDATA #REQUIRED	
		type CDATA #REQUIRED
	>
	<!ATTLIST table
		name CDATA #REQUIRED
	>
	<!ATTLIST column
		name CDATA #REQUIRED	
		type CDATA #REQUIRED	
		length CDATA "0"	
		default-value CDATA ""	
		default-type CDATA ""	
		nullable CDATA "no"
	>
	<!ATTLIST column-reference
		name CDATA #REQUIRED
	>
	<!ATTLIST class
		name CDATA #REQUIRED
	>
	<!ATTLIST attribute
		name CDATA #REQUIRED	
		type CDATA #REQUIRED	
		length CDATA "0"
		nullable CDATA "no"
		list CDATA "false"
		primary-key CDATA "false"
	>
	<!ATTLIST message
		name CDATA #REQUIRED	
		code CDATA ""
	>
	<!ATTLIST parameter
		name CDATA #REQUIRED	
		type CDATA #REQUIRED
		list CDATA "false"
	>
]>

<specification
	company="Lockheed"
	project="Lockheed"
	cpp-base-directory="${file-location}\\..\\src.cpp\\common\\Cdef\\"
	namespace="Lockheed">
	
	<!-- ********************************************************************** -->
	<!-- * CLASSES                                                              -->
	<!-- ********************************************************************** -->
	<classes>
	   
  </classes>
  
	<!-- ********************************************************************** -->
	<!-- * MESSAGES                                                             -->
	<!-- ********************************************************************** -->
  <messages>
		
		<message name="InputMessage" code="0x1000">
			<description>Input message manipulation</description>
			<request>
				<parameter name="action"        type="int"                      />
				<parameter name="ids"           type="int"          list="true" />
				<parameter name="inputMessages" type="InputMessage" list="true" />
			</request>
			<response>
				<parameter name="action"        type="int"                      />
				<parameter name="inputMessages" type="InputMessage" list="true" />
			</response>
		</message>
		
		<message name="OutputMessage" code="0x1001">
			<description>Output message manipulation</description>
			<request>
				<parameter name="action"         type="int"                      />
				<parameter name="ids"            type="int"           list="true" />
				<parameter name="outputMessages" type="OutputMessage" list="true" />
			</request>
			<response>
				<parameter name="action"         type="int"                      />
				<parameter name="outputMessages" type="OutputMessage" list="true" />
			</response>
		</message>
		
		<message name="Login" code="0x1002">
			<description></description>
			<request>
				<parameter name="username" type="string" />
				<parameter name="password" type="string" />
			</request>
			<response>
			</response>
		</message>
		
		<message name="OutputDevice" code="0x1004">
			<description>Output device manipulation</description>
			<request>
				<parameter name="action"        type="int"                      />
				<parameter name="ids"           type="int"          list="true" />
				<parameter name="outputDevices" type="OutputDevice" list="true" />
			</request>
			<response>
				<parameter name="action"        type="int"                      />
				<parameter name="outputDevices" type="OutputDevice" list="true" />
			</response>
		</message>
		
		<message name="Location" code="0x1005">
			<description>Location manipulation</description>
			<request>
				<parameter name="action"        type="int"                      />
				<parameter name="parentId"      type="int"                      />
			</request>
			<response>
				<parameter name="action"        type="int"                      />
				<parameter name="locations"     type="Location"     list="true" />
			</response>
		</message>
		
		<message name="Point" code="0x1006">
			<description>Point manipulation</description>
			<request>
				<parameter name="action"        type="int"                      />
				<parameter name="nodeId"        type="int"                      />
				<parameter name="address"       type="string"                   />
			</request>
			<response>
				<parameter name="action"        type="int"                      />
				<parameter name="points"        type="Point"        list="true" />
			</response>
		</message>
		
		<message name="CurrentEvent" code="0x1007">
			<description>Current event manipulation</description>
			<request>
				<parameter name="action"        type="int"                      />
				<parameter name="ids"           type="int"          list="true" />
				<parameter name="currentEvents" type="CurrentEvent" list="true" />
			</request>
			<response>
				<parameter name="action"        type="int"                      />
				<parameter name="currentEvents" type="CurrentEvent" list="true" />
			</response>
		</message>

	</messages>
</specification>
