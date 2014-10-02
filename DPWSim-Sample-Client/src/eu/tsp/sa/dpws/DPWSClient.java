package eu.tsp.sa.dpws;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.ws4d.java.JMEDSFramework;
import org.ws4d.java.client.DefaultClient;
import org.ws4d.java.communication.CommunicationException;
import org.ws4d.java.communication.DPWSProtocolInfo;
import org.ws4d.java.communication.DPWSProtocolVersion;
import org.ws4d.java.dispatch.DefaultServiceReference;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.eventing.ClientSubscription;
import org.ws4d.java.eventing.EventSource;
import org.ws4d.java.eventing.EventingException;
import org.ws4d.java.security.CredentialInfo;
import org.ws4d.java.security.SecurityKey;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.ParameterValueManagement;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.LocalizedString;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.SearchParameter;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;

public class DPWSClient extends DefaultClient {
	final static Charset ENCODING = StandardCharsets.UTF_8;
	final static String	namespace	= "http://telecom-sudparis.eu/sa";
	final static QName	service		= new QName("BasicServices", namespace);
	
	private File db_location;

	public DPWSClient(File db) {
		db_location = db;
	}
	@Override
	
	public void deviceFound(DeviceReference devRef, SearchParameter search) {
		//JMEDSFramework.start(null);
		try {
			Device device = devRef.getDevice();
			String deviceInfo = device.getFriendlyName(LocalizedString.LANGUAGE_EN) + "," + 
					device.getEndpointReference().getAddress() + "," +
					((XAddressInfo) devRef.getXAddressInfos(true).next()).getXAddress();
			
			//try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
			try (BufferedWriter writer = new BufferedWriter(new FileWriter(db_location, true))) {
				writer.write(deviceInfo);
				writer.newLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.out.println(deviceInfo);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public boolean subscribe(String epr, String xAddress, long duration, String IPBinding){
		EndpointReference epref = new EndpointReference(new AttributedURI(epr));
        XAddressInfo xAddressInfo = new XAddressInfo(new URI(xAddress));
        xAddressInfo.setProtocolInfo(new DPWSProtocolInfo(new DPWSProtocolVersion(0)));
        XAddressInfoSet addresses = new XAddressInfoSet(xAddressInfo);
        
        DeviceReference defRef = DeviceServiceRegistry.getDeviceReference(epref, addresses, true);
        try {
            Device dev = defRef.getDevice();

            Iterator servicesReferences = dev.getServiceReferences(SecurityKey.EMPTY_KEY);
            while (servicesReferences.hasNext()) {
                DefaultServiceReference servRef = (DefaultServiceReference) servicesReferences.next();
                try {
        			// use this code to subscribe to the simple event
        			{
        				// get event source
        				EventSource eventSource = servRef.getService().getAnyEventSource(service, "ExampleEvent");

        				if (eventSource != null) {
        					
        					System.out.println("__________ Event source OK " + eventSource.toString());
        					
        					// add binding
//        					DataStructure bindings = new org.ws4d.java.structures.ArrayList();
//        					HTTPBinding binding = new HTTPBinding(IPAddress.getLocalIPAddress("127.0.0.1"), 10235, "/EventSink", CommunicationManager.ID_NULL);
//        					bindings.add(binding); System.out.println("__________ HTTPBinding OK" + bindings.toString());

        					// subscribe
        					eventSource.subscribe(this, duration, CredentialInfo.EMPTY_CREDENTIAL_INFO); 
        					System.out.println("__________ Subscription OK");
        				}
        			}

        		} catch (EventingException | IOException
        				| CommunicationException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			return false;
        		} 
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        
        return true;
	}
	
	@Override
	public ParameterValue eventReceived(ClientSubscription subscription, URI actionURI, ParameterValue parameterValue) {
		System.err.println("got an event: " + ParameterValueManagement.getString(parameterValue, "name"));
		return null;
	}
	
	@Override
	public void subscriptionTimeoutReceived(ClientSubscription subscription) {
		subscriptionEndReceived(subscription, 0);
	}

	@Override
	public void subscriptionEndReceived(ClientSubscription subscription, int subscriptionEndType) {
		System.err.println("Subscription ended.");
	}
}
