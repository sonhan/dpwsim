/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.structures;

/**
 * Dynamic byte array.
 */
public class ByteArray {

	private static int	DEFAULT_SIZE	= 8192;

	private int[]		resizefac		= { 2, 4, 6, 8, 16, 32, 48, 64, 128, 256, 384, 512 };

	private int			resize			= 0;

	private byte[]		buffer			= null;

	private int			pointer			= -1;

	/**
	 * Dynamic byte array.
	 */
	public ByteArray() {
		buffer = new byte[DEFAULT_SIZE];
	}

	/**
	 * Appends a byte to this array.
	 * 
	 * @param b the byte to append.
	 */
	public void append(byte b) {
		pointer++;
		if (resize > 0) {
			resize--;
		}
		if (pointer == buffer.length) {
			resize();
		}
		buffer[pointer] = b;
	}

	/**
	 * The size of this byte array.
	 * 
	 * @return the size.
	 */
	public int size() {
		return pointer + 1;
	}

	/**
	 * Returns the byte at the given position.
	 * 
	 * @param index the position.
	 * @return the byte at the position.
	 */
	public byte byteAt(int index) throws ArrayIndexOutOfBoundsException {
		if (index < 0 || index > pointer) throw new ArrayIndexOutOfBoundsException(index + " > " + pointer);
		return buffer[index];
	}

	/**
	 * Truncate the byte array to the correct size.
	 */
	public void trunc() {
		if (pointer < buffer.length) {
			byte[] nbuffer = new byte[pointer];
			System.arraycopy(buffer, 0, nbuffer, 0, pointer);
			buffer = nbuffer;
		}
	}

	/**
	 * Get the byte array from this object.
	 * 
	 * @return the byte array.
	 */
	public byte[] getBytes() {
		trunc();
		return buffer;
	}

	/**
	 * Resize this array if more space needed.
	 */
	private void resize() {
		byte[] nbuffer = new byte[buffer.length * resizefac[resize]];
		System.arraycopy(buffer, 0, nbuffer, 0, buffer.length);
		buffer = nbuffer;
		if (resize < resizefac.length) {
			resize++;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return new String(buffer);
	}

}
