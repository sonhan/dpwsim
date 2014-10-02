package eu.tsp.sa.dpws;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.ws4d.java.JMEDSFramework;
import org.ws4d.java.client.SearchManager;
import org.ws4d.java.communication.DPWSProtocolInfo;
import org.ws4d.java.communication.DPWSProtocolVersion;
import org.ws4d.java.dispatch.DefaultServiceReference;
import org.ws4d.java.dispatch.DeviceServiceRegistry;
import org.ws4d.java.security.CredentialInfo;
import org.ws4d.java.security.SecurityKey;
import org.ws4d.java.service.Device;
import org.ws4d.java.service.Operation;
import org.ws4d.java.service.Service;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.ParameterValueManagement;
import org.ws4d.java.service.reference.DeviceReference;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.AttributedURI;
import org.ws4d.java.types.EndpointReference;
import org.ws4d.java.types.SearchParameter;
import org.ws4d.java.types.URI;
import org.ws4d.java.types.XAddressInfo;
import org.ws4d.java.types.XAddressInfoSet;

public class DPWSUtil {
	/**
	 * @author Son Han
	 * Static method to invoke an operation with provided info
	 * 
	 * @param epr
	 * @param xAddress
	 * @param opName
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public static String invokeOperation(
			String epr, 
			String xAddress, 
			String opName, 
			String paramName,
			String paramValue){
		
		JMEDSFramework.start(null);
		EndpointReference epref = new EndpointReference(new AttributedURI(epr));
        XAddressInfo xAddressInfo = new XAddressInfo(new URI(xAddress));
        xAddressInfo.setProtocolInfo(new DPWSProtocolInfo(DPWSProtocolVersion.DPWS_VERSION_2009));
        XAddressInfoSet addresses = new XAddressInfoSet(xAddressInfo);
        
        DeviceReference defRef = DeviceServiceRegistry.getDeviceReference(epref, addresses, true);
        try {
            Device dev = defRef.getDevice();

            Iterator servicesReferences = dev.getServiceReferences(SecurityKey.EMPTY_KEY);
            while (servicesReferences.hasNext()) {
                DefaultServiceReference servRef = (DefaultServiceReference) servicesReferences.next();
                Service serv = servRef.getService();
                Iterator ops = serv.getOperations();
                while (ops.hasNext()) {
                    Operation op = (Operation) ops.next();
                    if (op.getName().equals(opName)){
                    	ParameterValue request = op.createInputValue();
        				if (request != null) ParameterValueManagement.setString(request, paramName, paramValue);
                    	ParameterValue result =  op.invoke(request, CredentialInfo.EMPTY_CREDENTIAL_INFO);
                        return ParameterValueManagement.getString(result, "reply");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed";
        }
        
        return "Operation not found";
	}

	/**
	 * Call this static function to discover all the devices 
	 * and store to database file according to the format in
	 * DPWSClient class
	 *  
	 * @param db database File. If db doesn't exits, create a new file.
	 */
	public static boolean searchAndStoreTo(File db) {
		if(!db.exists()){
			try {
				db.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(db))) {
			// This empty implementation is to clear the device list database
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		JMEDSFramework.start(null);
		DPWSClient client = new DPWSClient(db);
		SearchParameter search = new SearchParameter();
		//search.setDeviceTypes(new QNameSet(new QName("LightBulbDevice", "http://www.it-sudparis.eu")));
		SearchManager.searchDevice(search, client, null);
		return true;
	}
}
