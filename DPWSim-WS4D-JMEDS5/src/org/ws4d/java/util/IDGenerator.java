/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/

package org.ws4d.java.util;

import java.util.Random;

import org.ws4d.java.types.URI;

/**
 * This class can be used to create RFC-4122 time based Universally Unique
 * Identifiers with random host node part. Portions adopted from the JUG UUID
 * generator. Also offers methods for generating random numbers.
 * 
 * @see <a href="http://jug.safehaus.org/">http://jug.safehaus.org/</a>
 * @see <a
 *      href="http://www.faqs.org/rfcs/rfc4122.html">http://www.faqs.org/rfcs/rfc4122.html</a>
 */
public final class IDGenerator {

	public static final String	UUID_PREFIX				= "uuid";

	/**
	 * the prefix to be used for DPWS.
	 */
	public static final String	URI_UUID_PREFIX			= "urn:" + UUID_PREFIX + ":";

	/**
	 * Start of the Gregorian calendar.
	 */

	private static final long	CLOCK_OFFSET			= 0x01b21dd213814000L;

	private static final String	HEX_CHARS				= "0123456789abcdef";

	private static final byte	INDEX_CLOCK_HI			= 6;

	private static final byte	INDEX_CLOCK_MID			= 4;

	private static final byte	INDEX_CLOCK_LOW			= 0;

	private static final byte	INDEX_CLOCK_SEQUENCE	= 8;

	private static final byte	INDEX_VARIANT			= 8;

	private static final byte	INDEX_NODE				= 10;

	private static byte[]		nodeIdentifier			= new byte[6];

	private static short		clockSequence;

	private static long			commonSequenceNumber	= 1;

	/** static internal message counter */
	private static int			lastUsedInternalMsgId	= 0;

	static {
		// initialize that nodeidentifier
		Random r = Math.getRandom();
		long rand = r.nextLong();
		for (int i = 0; i < 6; i++) {
			nodeIdentifier[i] = (byte) (rand >>> (56 - i * 8));
		}

		// initialize clock sequence
		clockSequence = (short) (r.nextInt() & 0xffff);
	}

	/**
	 * Utility class. Hides default constructor.
	 */
	private IDGenerator() {

	}

	/**
	 * Returns a RFC-4122 conformant UUID, normal form.
	 * 
	 * @return a RFC-4122 conformant UUID, normal form.
	 */
	public static String getUUID() {
		StringBuffer uuidString = new StringBuffer(36);
		byte[] uuidData = new byte[16];

		/* get the time in 100 nano seconds intervalls since 1582/15/10 */
		long systime = System.currentTimeMillis();

		systime *= 10000L; // 100 nano seconds resolution
		systime += CLOCK_OFFSET;

		int clockHi = (int) (systime >>> 32);
		int clockLo = (int) systime;

		uuidData[INDEX_CLOCK_LOW] = (byte) (clockLo >>> 24);
		uuidData[INDEX_CLOCK_LOW + 1] = (byte) (clockLo >>> 16);
		uuidData[INDEX_CLOCK_LOW + 2] = (byte) (clockLo >>> 8);
		uuidData[INDEX_CLOCK_LOW + 3] = (byte) (clockLo);

		uuidData[INDEX_CLOCK_HI] = (byte) (clockHi >>> 24);
		uuidData[INDEX_CLOCK_HI + 1] = (byte) (clockHi >>> 16);
		// set version
		uuidData[INDEX_CLOCK_HI] &= 0x0f;
		uuidData[INDEX_CLOCK_HI] |= (1 << 4);

		uuidData[INDEX_CLOCK_MID] = (byte) (clockHi >>> 8);
		uuidData[INDEX_CLOCK_MID + 1] = (byte) (clockHi);
		uuidData[INDEX_CLOCK_SEQUENCE] = (byte) (clockSequence >> 16);
		uuidData[INDEX_CLOCK_SEQUENCE + 1] = (byte) (clockSequence);
		clockSequence++;

		uuidData[INDEX_VARIANT] &= (byte) 0x3f;
		uuidData[INDEX_VARIANT] |= (byte) 0x80;

		for (int i = 0; i < 6; i++) {
			uuidData[INDEX_NODE + i] = nodeIdentifier[i];
		}

		for (int i = 0; i < 16; i++) {
			switch (i) {
				case 4:
				case 6:
				case 8:
				case 10:
					uuidString.append("-");
			}
			int hex = uuidData[i] & 0xff;
			uuidString.append(HEX_CHARS.charAt(hex >> 4));
			uuidString.append(HEX_CHARS.charAt(hex & 0xf));
		}

		return uuidString.toString();
	}

	/**
	 * Returns the UUID as URI. e.g.
	 * "urn:uuid:550e8400-e29b-11d4-a716-446655440000". URI schemas:
	 * http://www.iana.org/assignments/uri-schemes.html URN namespaces:
	 * http://www.iana.org/assignments/urn-namespaces/
	 * 
	 * @return the UUID URI.
	 */
	public static URI getUUIDasURI() {
		return new URI(URI_UUID_PREFIX + getUUID());
	}

	/**
	 * Returns a sequence number that is unique within a single VM until this VM
	 * is restarted.
	 * 
	 * @return the sequence number
	 */
	public static synchronized long getSequenceNumber() {
		return commonSequenceNumber++;
	}

	/**
	 * Increment static message id.
	 * 
	 * @return internal message id
	 */
	public static synchronized int getStaticMsgId() {
		return ++lastUsedInternalMsgId;
	}

}
