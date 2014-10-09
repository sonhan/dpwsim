/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.types;

import org.ws4d.java.configuration.FrameworkProperties;
import org.ws4d.java.structures.ArrayList;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.List;

/**
 * 
 */
public class ReferenceParametersMData extends UnknownDataContainer {

	public static class ReferenceParameter {

		private final String	namespace;

		private final String	name;

		/*
		 * each odd element is a namespace URI, each even element is a
		 * concatenated XML substring between prefixes
		 */
		private List			contentChunks;

		/**
		 * @param namespace
		 * @param name
		 */
		public ReferenceParameter(String namespace, String name) {
			super();
			this.namespace = namespace;
			this.name = name;
		}

		/*
		 * (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		public String toString() {
			StringBuffer sb = new StringBuffer();
			if (contentChunks != null) {
				sb.append('<').append(namespace).append(':').append(name);
				for (Iterator it = contentChunks.iterator(); it.hasNext();) {
					sb.append(it.next());
				}
			}
			return sb.toString();
		}

		/**
		 * @return the namespace
		 */
		public String getNamespace() {
			return namespace;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}

		/**
		 * @param chunk the next content chunk to append
		 */
		public void appendChunk(String chunk) {
			if (contentChunks == null) {
				contentChunks = new ArrayList();
			}
			contentChunks.add(chunk);
		}

		/**
		 * Returns an array containing all the chunks of unknown XML character
		 * data stored within this reference parameter instance. In case there
		 * are no chunks, an array of length zero is returned. The returned
		 * array contains always a namespace URI at each of its
		 * <strong>odd</strong> elements and a chunk of XML characters at the
		 * <strong>even</strong> ones.
		 * 
		 * @return an array containing all the chunks
		 */
		public String[] getChunks() {
			return contentChunks == null ? EMPTY_STRING_ARRAY : (String[]) contentChunks.toArray(new String[contentChunks.size()]);
		}

	}

	private static final String[]				EMPTY_STRING_ARRAY		= new String[0];

	private static final ReferenceParameter[]	EMPTY_PARAMETER_ARRAY	= new ReferenceParameter[0];

	private String								wseIdentifier;

	private List								parameters;

	/**
	 * 
	 */
	public ReferenceParametersMData() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (wseIdentifier != null) {
			sb.append("wseIdentifier=").append(wseIdentifier);
		}
		if (parameters != null) {
			if (wseIdentifier != null) {
				sb.append('\n');
			}
			for (Iterator it = parameters.iterator(); it.hasNext();) {
				sb.append(it.next());
				if (it.hasNext()) {
					sb.append('\n');
				}
			}
		}
		return sb.toString();
	}

	/**
	 * @return the wseIdentifier
	 */
	public String getWseIdentifier() {
		return wseIdentifier;
	}

	/**
	 * @param wseIdentifier the wseIdentifier to set
	 */
	public void setWseIdentifier(String wseIdentifier) {
		this.wseIdentifier = wseIdentifier;
	}

	/**
	 * @param parameter the next reference parameter to add
	 */
	public void add(ReferenceParameter parameter) {
		if (parameters == null) {
			parameters = new ArrayList();
		}
		parameters.add(parameter);
	}

	public boolean isEmpty() {
		boolean empty = false;
		if (!FrameworkProperties.REFERENCE_PARAM_MODE && wseIdentifier != null) {
			if (parameters == null && unknownElements_QN_2_List == null && unknownAttributes == null) {
				empty = true;
			} else if (parameters.isEmpty() && unknownElements_QN_2_List.isEmpty() && unknownAttributes.isEmpty()) {
				empty = true;
			}
		}
		return empty;
	}

	/**
	 * Returns an array containing all the parameters stored within this
	 * reference parameters instance. If there are no parameters, an array of
	 * length zero is returned.
	 * 
	 * @return an array containing all the parameters
	 */
	public ReferenceParameter[] getParameters() {
		return parameters == null ? EMPTY_PARAMETER_ARRAY : (ReferenceParameter[]) parameters.toArray(new ReferenceParameter[parameters.size()]);
	}

}
