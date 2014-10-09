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

import org.ws4d.java.structures.DataStructure;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.util.StringUtil;

/**
 * Class represents metadata of dpws:ThisModel.
 * 
 * @author mspies
 */
public class ThisModelMData extends UnknownDataContainer {

	// public static final URI THIS_MODEL_METADATA_DIALECT = new
	// URI(DPWSConstants.METADATA_DIALECT_THISMODEL);

	/** HashMap<String language, LocalizedString dpws:Manufacturer> */
	private HashMap	manufacturerNames	= new HashMap();

	/** manufacturer url */
	private URI		manufacturerUrl		= new URI("http://dpws.materna.de");

	/** HashMap<String language, LocalizedString dpws:ModelName> */
	private HashMap	modelNames			= new HashMap();

	/** model number */
	private String	modelNumber			= "1";

	/** model url */
	private URI		modelUrl			= new URI("http://dpws.materna.de/model");

	/** presentation url */
	private URI		presentationUrl		= new URI("http://dpws.materna.de/model/presentation.html");

	/**
	 * Constructor.
	 */
	public ThisModelMData() {
		super();
	}

	/**
	 * Copy Constructor.
	 * 
	 * @param metadata
	 */
	public ThisModelMData(ThisModelMData metadata) {
		super(metadata);

		if (metadata == null) {
			return;
		}

		if (metadata.manufacturerNames != null) {
			for (Iterator it = metadata.manufacturerNames.values().iterator(); it.hasNext();) {
				LocalizedString name = (LocalizedString) it.next();
				this.addManufacturerName(name);
			}
		}
		if (metadata.manufacturerUrl != null) {
			manufacturerUrl = metadata.manufacturerUrl;
		}
		if (metadata.modelNames != null) {
			for (Iterator it = metadata.modelNames.values().iterator(); it.hasNext();) {
				LocalizedString name = (LocalizedString) it.next();
				this.addModelName(name);
			}
		}
		if (metadata.modelNumber != null) {
			modelNumber = metadata.modelNumber;
		}
		if (metadata.modelUrl != null) {
			modelUrl = metadata.modelUrl;
		}
		if (metadata.presentationUrl != null) {
			presentationUrl = metadata.presentationUrl;
		}
	}

	// ---------------------------------------------------------------------

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		sb.append(" [ manufacturerNames=").append(manufacturerNames);
		sb.append(", manufacturerUrl=").append(manufacturerUrl);
		sb.append(", modelNames=").append(modelNames);
		sb.append(", modelNumber=").append(modelNumber);
		sb.append(", modelUrl=").append(modelUrl);
		sb.append(", presentationUrl=").append(presentationUrl);
		sb.append(" ]");
		return sb.toString();
	}

	// ------------------------------- GETTER -----------------------------

	public DataStructure getManufacturerNames() {
		if (manufacturerNames == null) {
			return null;
		}

		return manufacturerNames.values();
	}

	public LocalizedString getManufacturerName(String lang) {
		HashMap manufacturerNames = this.manufacturerNames;

		if (manufacturerNames == null) {
			return null;
		}

		return (LocalizedString) manufacturerNames.get(lang);
	}

	public URI getManufacturerUrl() {
		return manufacturerUrl;
	}

	public DataStructure getModelNames() {
		HashMap modelNames = this.modelNames;

		if (modelNames == null) {
			return null;
		}

		return modelNames.values();
	}

	public LocalizedString getModelName(String lang) {
		HashMap modelNames = this.modelNames;

		if (modelNames == null) {
			return null;
		}

		return (LocalizedString) modelNames.get(lang);
	}

	public String getModelNumber() {
		return modelNumber;
	}

	public URI getModelUrl() {
		return modelUrl;
	}

	public URI getPresentationUrl() {
		return presentationUrl;
	}

	// ------------------------------- SETTER -----------------------------

	/**
	 * Sets manufacturer names.
	 * 
	 * @param manufacturerNames HashMap with manufacturer names, key attribute
	 *            must be the language. HashMap<String language, LocalizedString
	 *            dpws:Manufacturer>
	 */
	public void setManufacturerNames(HashMap manufacturerNames) {
		this.manufacturerNames = manufacturerNames;
	}

	/**
	 * Adds a manufacturer name to model by language.
	 * 
	 * @param manufacturerName manufacturer name
	 */
	public void addManufacturerName(LocalizedString manufacturerName) {
		if (manufacturerNames == null) {
			manufacturerNames = new HashMap();
		}
		manufacturerNames.put(manufacturerName.getLanguage(), manufacturerName);
	}

	/**
	 * Sets manufacturer url.
	 * 
	 * @param manufacturerUrl
	 */
	public void setManufacturerUrl(URI manufacturerUrl) {
		this.manufacturerUrl = manufacturerUrl;
	}

	/**
	 * Sets model names.
	 * 
	 * @param modelNames metadata element map with model names, key attribute
	 *            must be the language. HashMap<String language, LocalizedString
	 *            dpws:ModelName>
	 */
	public void setModelNames(HashMap modelNames) {
		this.modelNames = modelNames;
	}

	/**
	 * Adds name to model by language.
	 * 
	 * @param modelName model name
	 */
	public void addModelName(LocalizedString modelName) {
		if (modelNames == null) {
			modelNames = new HashMap();
		}
		modelNames.put(modelName.getLanguage(), modelName);
	}

	/**
	 * Sets model number.
	 * 
	 * @param modelNumber
	 */
	public void setModelNumber(String modelNumber) {
		this.modelNumber = modelNumber;
	}

	/**
	 * Sets model url.
	 * 
	 * @param modelUrl
	 */
	public void setModelUrl(URI modelUrl) {
		this.modelUrl = modelUrl;
	}

	/**
	 * Sets presentation url.
	 * 
	 * @param presentationUrl
	 */
	public void setPresentationUrl(URI presentationUrl) {
		this.presentationUrl = presentationUrl;
	}
}
