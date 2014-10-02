import org.ws4d.java.communication.DPWSProtocolVersion;

import eu.tsp.sa.dpws.DPWSUtil;


public class TestMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		int testcase = 1;
		String op = "SwitchOn";
		//String op = "SwitchOff";
		
		System.out.println(DPWSProtocolVersion.DPWS_VERSION_2009);
		DPWSUtil.invokeOperation("urn:uuid:a72e55e0-dc46-11e3-bfd5-d0a27e467046", 
				"http://192.168.1.132:4567/L1400168176188Device", op, "param", "");
		
		if (testcase > 1)
		DPWSUtil.invokeOperation("urn:uuid:a1a1be00-dc46-11e3-bfd1-d0a27e467046", 
				"http://192.168.1.132:4567/L1400168166877Device", op, "param", "");
		
		if (testcase > 2)
		DPWSUtil.invokeOperation("urn:uuid:ac568e00-dc43-11e3-bfc2-d0a27e467046", 
				"http://192.168.1.132:4567/L1400166896350Device", op, "param", "");
		
		
		if (testcase > 3)
			DPWSUtil.invokeOperation("urn:uuid:abea5750-dc46-11e3-bfd9-d0a27e467046", 
					"http://192.168.1.132:4567/L1400168184131Device", op, "param", "");
			
		if (testcase > 4)
			DPWSUtil.invokeOperation("urn:uuid:b98bae00-dc45-11e3-bfca-d0a27e467046", 
					"http://192.168.1.132:4567/L1400167777501Device", op, "param", "");
			
		if (testcase > 5)
			DPWSUtil.invokeOperation("urn:uuid:32ee89a0-dc43-11e3-bfba-d0a27e467046", 
					"http://192.168.1.132:4567/L1400166692569Device", op, "param", "");
			
		if (testcase > 6)
			DPWSUtil.invokeOperation("urn:uuid:b639ce70-dc46-11e3-bfe1-d0a27e467046", 
					"http://192.168.1.132:4567/L1400168201430Device", op, "param", "");
			
		if (testcase > 7)
			DPWSUtil.invokeOperation("urn:uuid:c3235160-dc46-11e3-bfe9-d0a27e467046", 
					"http://192.168.1.132:4567/L1400168223092Device", op, "param", "");
		
		if (testcase > 8)
			DPWSUtil.invokeOperation("urn:uuid:bb56b490-dc46-11e3-bfe5-d0a27e467046", 
					"http://192.168.1.132:4567/L1400168210007Device", op, "param", "");
		
		if (testcase > 9)
			DPWSUtil.invokeOperation("urn:uuid:b0b7e4f0-dc46-11e3-bfdd-d0a27e467046", 
					"http://192.168.1.132:4567/L1400168192189Device", op, "param", "");
		
		System.exit(0);
	}

}
