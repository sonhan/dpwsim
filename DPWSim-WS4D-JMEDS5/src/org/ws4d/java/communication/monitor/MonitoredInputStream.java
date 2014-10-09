/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.communication.monitor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.util.Log;

/**
 * This is a stream wrapper which allows to pass-through data to a given
 * <code>OutputStream</code> for monitoring.
 */
public final class MonitoredInputStream extends InputStream {

	private static int		count	= 0;

	private InputStream		in		= null;

	private StreamMonitor	stMon	= null;

	private int				len		= 0;

	private boolean			monWarn	= true;

	public MonitoredInputStream(InputStream in, ProtocolData pd) {
		MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
		if (monFac != null) {
			stMon = monFac.getInputMonitor(pd);
			monFac.linkIn(pd, stMon);
		}
		this.in = in;
		count++;
	}

	public int read() throws IOException {
		len++;
		int i = in.read();
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.write(i);
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredInputStream.read() (" + intToString(i) + ")");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
		return i;
	}

	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		int l = in.read(b, off, len);
		this.len += l;
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.write(b, off, l);
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredInputStream.read(byte[" + b.length + "], off: " + off + ", len: " + len + ") (bytes read: " + l + ", " + byteArrayToString(b)+ ")");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}

		}
		return l;
	}

	public void close() throws IOException {
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredInputStream.close()");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
		in.close();
	}

	public int available() throws IOException {
		return in.available();
	}

	public void reset() throws IOException {
		in.reset();
	}

	public long skip(int len) throws IOException {
		return in.skip(len);
	}

	public void mark(int readlimit) {
		in.mark(readlimit);
	}

	public boolean markSupported() {
		return in.markSupported();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((in == null) ? 0 : in.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		MonitoredInputStream other = (MonitoredInputStream) obj;
		if (in == null) {
			if (other.in != null) return false;
		} else if (!in.equals(other.in)) return false;
		return true;
	}

	private String byteArrayToString(byte[] b) {
		char[] chars = new char[b.length + 1];
		chars[b.length] = '}';
		for (int i = 0; i < b.length; i++) {
			chars[i] = (char)b[i];
		}
		return "byte[" + b.length + "]: {" + new String(chars);
	}
	
	private String intToString(int i) {
		if (i > 32 && i < 127) {
			return "" + (char)i;
		}
		else if (i == 32) {
			return "\"space character\"";
		}
		else {
			return "dec: " + i;
		}
	}
}
