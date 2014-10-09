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
import java.io.OutputStream;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.ProtocolData;
import org.ws4d.java.util.Log;

/**
 * This is a stream wrapper which allows to pass-through data to a given
 * <code>OutputStream</code> for monitoring.
 */
public class MonitoredOutputStream extends OutputStream {

	private static int		count	= 0;

	private OutputStream	out		= null;

	private StreamMonitor	stMon	= null;

	private int				len		= 0;

	private boolean			monWarn	= true;

	public MonitoredOutputStream(OutputStream out, ProtocolData pd) {
		MonitorStreamFactory monFac = DPWSFramework.getMonitorStreamFactory();
		if (monFac != null) {
			stMon = monFac.getOutputMonitor(pd);
			monFac.linkOut(pd, stMon);
		}
		this.out = out;
		count++;
	}

	public void write(int arg0) throws IOException {
		len++;
		out.write(arg0);
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.write(arg0);
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredOutputStream.write(" + intToString(arg0) + ")");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
	}

	public void write(byte[] b) throws IOException {
		out.write(b);
		len += b.length;
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.write(b);
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredOutputStream.write(" + byteArrayToString(b) + ")");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
	}

	public void write(byte[] b, int off, int len) throws IOException {
		out.write(b, off, len);
		this.len += len;
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.write(b, off, len);
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredOutputStream.write(off: " + off + ", len: " + len + ", " + byteArrayToString(b)+ ")");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.io.OutputStream#flush()
	 */
	public void flush() throws IOException {
		out.flush();
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.flush();
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredOutputStream.flush()");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
	}

	public void close() throws IOException {
		flush();
		out.close();
		if (stMon != null && stMon.getOutputStream() != null) {
			OutputStream os = stMon.getOutputStream();
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					if (monWarn) {
						Log.error("Monitoring failed in MonitoredOutputStream.close()");
						Log.printStackTrace(e);
						monWarn = false;
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((out == null) ? 0 : out.hashCode());
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
		MonitoredOutputStream other = (MonitoredOutputStream) obj;
		if (out == null) {
			if (other.out != null) return false;
		} else if (!out.equals(other.out)) return false;
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
