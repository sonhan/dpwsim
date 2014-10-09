package eu.telecom.sudparis.dpws;

import java.io.Serializable;

import org.ws4d.java.communication.HTTPBinding;


import org.ws4d.java.communication.connection.ip.IPAddress;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.service.DefaultDevice;
import org.ws4d.java.service.DefaultService;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.QNameSet;

/**
 * DPWS generic device 
 * 
 * @author	Son Han
 * @date	2013/12/06
 * @version 3.0
 */

public class GenericDevice extends DefaultDevice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -659591813977106533L;
	//private static String model = "DPWS-TSP";
	private GenericService service;
	private String namespace;
	private String httpBindIP;
	private String portType;
	private int portDevice;
	private int portService;

	/**
	 * Constructor
	 * 
	 * @param name name of the device
	 * @param portType	device type
	 * @param manufaturer manufacturer
	 * @param namespace namespace
	 * @param httpBindIP IP address for HTTP binding
	 * @param portDevice device port for HTTP binding
	 * 
	 */
	public GenericDevice(String name,
			String portType,
			String manufacturer,
			String namespace,
			String httpBindIP, 
			int portDevice, 
			int portService) {
		super();
		this.namespace = namespace;
		this.portType = portType;
		this.httpBindIP = httpBindIP;
		this.portDevice = portDevice;
		this.portService = portService;
		
		this.setPortTypes(new QNameSet(new QName(portType, namespace)));
		this.addFriendlyName("en-US", name);
		this.addManufacturer(LocalizedString.LANGUAGE_EN, manufacturer);
		this.setManufacturerUrl(namespace);
		
		// add model name (model name is language specific)
		//this.addModelName(LocalizedString.LANGUAGE_EN, model);
				
		this.setModelUrl(namespace);
		this.setPresentationUrl(namespace);
		this.setSerialNumber("DPWS-TSP-" + System.currentTimeMillis());
		
		// add binding
		this.addBinding(new HTTPBinding(new IPAddress(httpBindIP), portDevice, name + "Device"));
		
		service = new GenericService();
		service.addBinding(new HTTPBinding(new IPAddress(httpBindIP), portService, name + "Service"));
		
		this.addService(service);
	}
	
	public void addOperation(String opName, OperationCommand command){
		service.addOperation(new GenericOperation(opName, namespace, command));
	}
	
	public void addOperation(GenericOperation op){
		service.addOperation(op);
	}
	
	public void addEvent(String evtName){
		service.addEventSource(new GenericEvent(evtName, namespace));
	}
	
	public void addEvent(GenericEvent ev) {
		service.addEventSource(ev);
	}
	
	public EventSource getEventSource(String evtName) {
		return service.getEventSource(new QName("events", namespace), evtName, null, evtName);
	}
	
	public Iterator getEventSources(){
		return service.getEventSources();
	}
	
	public DefaultService getService(){
		return service;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getHttpBindIP() {
		return httpBindIP;
	}

	public String getPortType() {
		return portType;
	}

	public int getPortDevice() {
		return portDevice;
	}

	public void setPortDevice(int portDevice) {
		this.portDevice = portDevice;
	}

	public int getPortService() {
		return portService;
	}

	public void setPortService(int portService) {
		this.portService = portService;
	}

	
}
