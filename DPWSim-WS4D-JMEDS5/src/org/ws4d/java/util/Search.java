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

import java.io.IOException;
import java.io.InputStream;

/**
 * This class implements some search algorithms.
 */
public class Search {

	/**
	 * Creates a fault function for a given pattern.
	 * 
	 * @param pattern pattern to search as a byte array.
	 * @return array of offset corrections.
	 */	
	public static int[] createFaultFunction(byte[] pattern) {		
		int[] faultFunction = new int[pattern.length + 1];
		
		int pos = 0;
		int preLen = -1;
		faultFunction[0] = -1;
		
		while (pos < pattern.length) {
			while (preLen >= 0 && pattern[pos] != pattern[preLen]) {
				preLen = faultFunction[preLen];
			}
			pos++;
			preLen++;
			faultFunction[pos] = preLen;
		}
		
		return faultFunction;
	}

	/**
	 * Encapsulates the search for the pattern on stream in an other stream.
	 * Using the <a
	 * href="http://en.wikipedia.org/wiki/Knuth-Morris-Pratt_algorithm"
	 * >Knuth-Morris-Pratt algorithm</a>.
	 * 
	 * @param in input stream.
	 * @param pattern pattern to search as a byte array.
	 * @return the input stream which encapsulates the search.
	 */
	public static InputStream getSearchPatternWrapper(InputStream in, byte[] pattern) {
		return new KMPAlgoInputStream(in, pattern);
	}

	
	public static InputStream getSearchPatternWrapper(InputStream in, byte[] pattern, int[] faultFunction) {
		return new KMPAlgoInputStream(in, pattern);
	}

	/**
	 * This stream encapsulates the search with the Knuth-Morris-Pratt algorithm
	 * for a pattern.
	 */
	private static class KMPAlgoInputStream extends InputStream {

		private InputStream	in					= null;

		private byte[]		pattern				= null;

		private int[]		faultFunction		= null;
		
		private int 		patternPos			= 0;
		
		private int 		virtualBufferSize	= 0;
		
		private int 		returnedBytesCount	= 0;
		
		private int 		readByte;
		

		/**
		 * Creates a Knuth-Morris-Pratt algorithm input stream.
		 * 
		 * @param in input stream.
		 * @param pattern pattern to search as a byte array.
		 */
		public KMPAlgoInputStream(InputStream in, byte[] pattern) {
			this(in, pattern, createFaultFunction(pattern));
		}

		public KMPAlgoInputStream(InputStream in, byte[] pattern, int[] faultFunction) {
			this.in = in;
			this.pattern = pattern;
			this.faultFunction = faultFunction;
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#available()
		 */
		public synchronized int available() throws IOException {
			if (virtualBufferSize == -42) {
				return 0;
			}

			int bytesToReturn = virtualBufferSize - returnedBytesCount - patternPos;
			if (bytesToReturn > 0) {
				return bytesToReturn;
			}
			
			return 0;
		}

		/*
		 * (non-Javadoc)
		 * @see java.io.InputStream#read()
		 */
		public synchronized int read() throws IOException {
			if (virtualBufferSize == -42) {
				return -1;
			}
			
			int bytesToReturn = virtualBufferSize - returnedBytesCount - patternPos;
			if (bytesToReturn > 0) {
				if (bytesToReturn == 1) {
					int result = (patternPos == 0) ? readByte : pattern[returnedBytesCount];
					virtualBufferSize = patternPos;
					returnedBytesCount = 0;
					return result;
				}
				else {
					return pattern[returnedBytesCount++];
				}
			}

			while (true) {
				readByte = in.read();
				virtualBufferSize++;

				if (readByte != -1) {
					while (patternPos >= 0 && readByte != pattern[patternPos]) {
						patternPos = faultFunction[patternPos];
					} 
					patternPos++;
				}

				if (patternPos == pattern.length) {
					virtualBufferSize = -42;
					return -1;
				}

				if (virtualBufferSize > patternPos) {
					
					if (virtualBufferSize == 1) {
						virtualBufferSize = 0;
						return readByte;
					}
					
					if (virtualBufferSize - patternPos == 1) {
						virtualBufferSize = patternPos;
					}
					else {
						returnedBytesCount = 1;
					}
					return pattern[0];
				}
			}
		}

	}

}
