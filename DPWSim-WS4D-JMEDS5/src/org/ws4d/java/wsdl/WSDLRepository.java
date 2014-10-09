/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.wsdl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.ws4d.java.DPWSFramework;
import org.ws4d.java.communication.monitor.ResourceLoader;
import org.ws4d.java.io.fs.FileSystem;
import org.ws4d.java.schema.Schema;
import org.ws4d.java.schema.SchemaException;
import org.ws4d.java.structures.EmptyStructures;
import org.ws4d.java.structures.HashMap;
import org.ws4d.java.structures.HashMap.Entry;
import org.ws4d.java.structures.HashSet;
import org.ws4d.java.structures.Iterator;
import org.ws4d.java.structures.Set;
import org.ws4d.java.types.QName;
import org.ws4d.java.types.URI;
import org.ws4d.java.util.Log;
import org.xmlpull.v1.XmlPullParserException;

/**
 * This class implements the WSDL Manager for handling predefined WSDLs in a so
 * called WSDL Repository.
 */
public class WSDLRepository {

	// TODO: Remove this after OSAMi Demonstation!
	public static final boolean			DEMO_MODE			= false;

	private static final String			DEMO_WSDL_COMMON	= "OSAmICommonSensorService.wsdl";

	private static final String			DEMO_WSDL_PULSE		= "OSAmIPulseService.wsdl";

	private static final String			DEMO_WSDL_O2		= "OSAmISpO2Service.wsdl";

	// TODO extract as framework(?) property
	private static final String			REPO_PATH			= "wsdl_repo";

	private static final String			INDEX_FILE			= "index.idx";

	private static final int			READ_BUFFER_SIZE	= 1024;

	private static final WSDLRepository	INSTANCE			= new WSDLRepository();

	private final FileSystem			fs;

	/*
	 * for WSDL files: key = port type as QName, value = WSDL file name within
	 * repository as String
	 */
	/*
	 * for XML Schema files: key = schema location as String (as found within
	 * include/import statement), value = XML Schema file name within repository
	 * as String
	 */
	private final HashMap				index				= new HashMap();

	public static String getRepoPath() {
		return REPO_PATH;
	}

	public static WSDLRepository getInstance() {
		return INSTANCE;
	}

	public static WSDL loadWsdl(URI wsdlUri) throws IOException {
		try {
			return WSDL.parse(wsdlUri, true);
		} catch (IOException e) {
			Log.error("Unable to obtain WSDL from " + wsdlUri + ": " + e.getMessage());
			throw e;
		} catch (XmlPullParserException e) {
			Log.error("Ill formatted WSDL from " + wsdlUri + ": " + e.getMessage());
			throw new IOException(e.getMessage());
		}
	}

	private WSDLRepository() {
		super();
		FileSystem fs = null;
		try {
			fs = DPWSFramework.getLocalFileSystem();
		} catch (IOException e) {
			/*
			 * no file system available within current runtime or framework not
			 * started
			 */
			Log.error("No local file system available, WSDL repository will not work.");
		}
		this.fs = fs;
		if (fs != null) {
			// initStore();
			try {
				loadIndex();
			} catch (IOException e) {
				if (Log.isDebug()) {
					Log.debug("Unable to load WSDL Repository index file: " + e.getMessage());
				}
			}
		}
	}

	public InputStream getWsdlInputStream(QName portType) {
		if (fs == null) {
			return null;
		}
		String wsdlFilePath;
		synchronized (index) {
			wsdlFilePath = (String) index.get(portType);
		}
		if (wsdlFilePath == null) {
			return null;
		}
		try {
			return fs.readFile(wsdlFilePath);
		} catch (IOException e) {
			Log.error("Unable to read WSDL file " + wsdlFilePath + ": " + e.getMessage());
		}
		return null;
	}

	public WSDL getWsdl(QName portType) {
		try {
			InputStream in = getWsdlInputStream(portType);
			if (in == null) return null;
			WSDL wsdl = WSDL.parse(in, true);
			if (DEMO_MODE) {
				String wsdlFilePath;
				synchronized (index) {
					Iterator it = index.keySet().iterator();
					while (it.hasNext()) {
						QName pt = (QName) it.next();
						if (Log.isDebug()) {
							Log.debug("PORTTYPE: " + pt, Log.DEBUG_LAYER_FRAMEWORK);
						}
					}
					wsdlFilePath = (String) index.get(portType);
				}
				String fileName = wsdlFilePath.substring(REPO_PATH.length() + fs.fileSeparator().length());
				if (Log.isDebug()) {
					Log.debug("OSAMi Demomode: LOAD " + fileName, Log.DEBUG_LAYER_FRAMEWORK);
				}
				if (fileName != null && fileName.equals(DEMO_WSDL_COMMON)) {
					/*
					 * get pulse WSDL
					 */
					WSDL pulse = getWSDL(DEMO_WSDL_PULSE);
					if (pulse != null) {
						pulse.addLinkedWsdl(wsdl);
					}
				} else if (fileName != null && fileName.equals(DEMO_WSDL_O2)) {
					/*
					 * get O2 WSDL
					 */
					WSDL common = getWSDL(DEMO_WSDL_COMMON);
					if (common != null) {
						wsdl.addLinkedWsdl(common);
					}
				} else if (fileName != null && fileName.equals(DEMO_WSDL_PULSE)) {
					/*
					 * get sensor WSDL
					 */
					WSDL common = getWSDL(DEMO_WSDL_COMMON);
					if (common != null) {
						wsdl.addLinkedWsdl(common);
					}
				}
			}
			in.close();
			if (Log.isDebug()) {
				Log.debug("WSDL: " + wsdl.toString(), Log.DEBUG_LAYER_FRAMEWORK);
			}
			return wsdl;
		} catch (XmlPullParserException e) {
			synchronized (index) {
				Log.error("Ill formatted WSDL file " + index.get(portType) + ": " + e.getMessage());
			}
		} catch (IOException e) {
			synchronized (index) {
				Log.error("Unable to read WSDL file " + index.get(portType) + ": " + e.getMessage());
			}
		}
		return null;
	}

	public Schema getSchema(String schemaLocation, String namespace) {
		String filePath;
		synchronized (index) {
			String key = namespace == null ? schemaLocation : schemaLocation + '|' + namespace;
			filePath = (String) index.get(key);
			if (filePath == null) {
				if (Log.isDebug()) {
					Log.debug("Unable to find XML Schema for schema location " + schemaLocation + " and namespace " + namespace + " within WSDL Repository");
				}
				return null;
			}
		}
		try {
			InputStream in = fs.readFile(filePath);
			if (in == null) {
				Log.warn("Unable to read XML Schema file " + filePath);
				return null;
			}
			try {
				return Schema.parse(in, null, true);
			} finally {
				in.close();
			}
		} catch (IOException e) {
			Log.error("Unable to read XML Schema file " + filePath + ": " + e.getMessage());
		} catch (XmlPullParserException e) {
			Log.error("Ill formatted XML Schema file " + filePath + ": " + e.getMessage());
		} catch (SchemaException e) {
			Log.error("Invalid XML Schema file " + filePath + ": " + e.getMessage());
		}
		return null;
	}

	public Iterator getPortTypes() {
		synchronized (index) {
			if (index.isEmpty()) {
				return EmptyStructures.EMPTY_ITERATOR;
			}
			Set portTypes = new HashSet();
			for (Iterator it = index.keySet().iterator(); it.hasNext();) {
				Object o = it.next();
				if (o instanceof QName) {
					// this index entry is for a WSDL port type
					portTypes.add(o);
				}
				// else it is an index entry for an XML Schema file
			}
			return portTypes.iterator();
		}
	}

	public WSDL loadAndStore(URI fromUri, String fileName) throws IOException {
		store(fromUri, fileName);
		if (fileName == null) {
			fileName = fromUri.toString();
		} else if ("".equals(fileName)) {
			return loadWsdl(fromUri);
		}
		// load from repository directory
		return getWSDL(fileName);
	}

	public WSDL loadAndStore(InputStream in, String fileName) throws IOException {
		WSDL wsdl = null;
		try {
			wsdl = WSDL.parse(in, true);
			in.close();
			if (wsdl != null) {
				// TODO make file name unique
				store(wsdl, fileName);
			}
		} catch (XmlPullParserException e) {
			Log.error("Ill formatted WSDL file: " + e.getMessage());
		}
		return wsdl;
	}

	public WSDL loadAndStore(URI wsdlUri) throws IOException {
		return loadAndStore(wsdlUri, wsdlUri.toString());
	}

	public void store(WSDL wsdl, String fileName) {
		if (DEMO_MODE) {
			Log.debug("OSAMi Demomode: STORE " + fileName, Log.DEBUG_LAYER_FRAMEWORK);
			if (fileName != null && fileName.equals(DEMO_WSDL_COMMON)) {
				/*
				 * get pulse WSDL
				 */
				WSDL pulse = getWSDL(DEMO_WSDL_PULSE);
				if (pulse != null) {
					pulse.addLinkedWsdl(wsdl);
				}
				WSDL o2 = getWSDL(DEMO_WSDL_O2);
				if (o2 != null) {
					o2.addLinkedWsdl(wsdl);
				}
			} else if (fileName != null && fileName.equals(DEMO_WSDL_O2)) {
				/*
				 * get O2 WSDL
				 */
				WSDL common = getWSDL(DEMO_WSDL_COMMON);
				if (common != null) {
					wsdl.addLinkedWsdl(common);
				}
			} else if (fileName != null && fileName.equals(DEMO_WSDL_PULSE)) {
				/*
				 * get sensor WSDL
				 */
				WSDL common = getWSDL(DEMO_WSDL_COMMON);
				if (common != null) {
					wsdl.addLinkedWsdl(common);
				}
			}
		}
		String filePath = REPO_PATH + fs.fileSeparator() + fs.escapeFileName(fileName);
		try {
			OutputStream out = fs.writeFile(filePath);
			wsdl.serialize(out);
			out.close();
			index(wsdl, filePath);
			flushIndex();
		} catch (IOException e) {
			Log.error("Unable to write to WSDL file " + filePath + ": " + e.getMessage());
		}
	}

	/**
	 * Imports a WSDL including any referenced WSDL and XML Schema files from
	 * the specified location <code>fromLocation</code>. If
	 * <code>fileName</code> is neither <code>null</code> nor equal to the empty
	 * String <code>&quot;&quot;</code>, the WSDL will be stored within the
	 * repository to a file with that name. Otherwise, if it is
	 * <code>null</code>, a file name will be derived from the URI the WSDL is
	 * loaded from (<code>fromLocation</code>). Finally, if
	 * <code>fileName</code> is equal to the empty String, the WSDL file will be
	 * searched for its target namespace and a file name will be derived there
	 * from.
	 * 
	 * @param fromLocation the location to load the file from
	 * @param fileName the name of the file to store the imported WSDL to within
	 *            the repository; may be <code>null</code> or the empty String
	 * @throws IOException if either accessing the WSDL or any of the files it
	 *             references or writing into the repository fails
	 */
	public void store(URI fromLocation, String fileName) throws IOException {
		store(fromLocation, fileName, true);
	}

	private void store(URI fromLocation, String fileName, boolean flushIndex) throws IOException {
		if (Log.isDebug()) {
			Log.debug("Importing WSDL from " + fromLocation);
		}
		WSDL wsdl = null;
		if (fileName == null) {
			fileName = fromLocation.toString();
		} else if ("".equals(fileName)) {
			try {
				wsdl = WSDL.parse(fromLocation, false);
			} catch (XmlPullParserException e) {
				throw new IOException(e.getMessage());
			}
			fileName = wsdl.getTargetNamespace();
			if (!fileName.endsWith("/")) {
				fileName += '/';
			}
			fileName += "description.wsdl";
		}
		String outputPath = REPO_PATH + fs.fileSeparator() + fs.escapeFileName(fileName);
		if (fs.fileExists(outputPath)) {
			if (Log.isDebug()) {
				Log.debug("WSDL Repository resource already exists: " + outputPath);
			}
			return;
		}
		ResourceLoader rl = DPWSFramework.getResourceAsStream(fromLocation);
		InputStream in = rl.getInputStream();
		if (in == null) {
			throw new IOException("Unable to read from " + fromLocation);
		}
		OutputStream out = fs.writeFile(outputPath);
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int length;
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		out.flush();
		out.close();
		in.close();
		if (wsdl == null) {
			try {
				wsdl = WSDL.parse(fromLocation, false);
			} catch (XmlPullParserException e) {
				throw new IOException(e.getMessage());
			}
		}
		index(wsdl, outputPath);
		storeReferencedFiles(fromLocation, fileName, flushIndex, wsdl);
	}

	/**
	 * @param fromLocation
	 * @param fileName
	 * @param flushIndex
	 * @param wsdl
	 * @throws IOException
	 */
	private void storeReferencedFiles(URI fromLocation, String fileName, boolean flushIndex, WSDL wsdl) throws IOException {
		URI fileNameUri = new URI(fileName);
		for (Iterator it = wsdl.getImports().values().iterator(); it.hasNext();) {
			String importLocation = (String) it.next();
			URI newUri = URI.absolutize(fromLocation, importLocation);
			store(newUri, URI.absolutize(fileNameUri, importLocation).toString(), false);
		}
		for (Iterator it = wsdl.getTypes(); it.hasNext();) {
			Schema schema = (Schema) it.next();
			for (Iterator it2 = schema.getIncludes().iterator(); it2.hasNext();) {
				String schemaLocation = (String) it2.next();
				URI newUri = URI.absolutize(fromLocation, schemaLocation);
				storeSchema(newUri, URI.absolutize(fileNameUri, schemaLocation).toString(), schemaLocation, false);
			}
			for (Iterator it2 = schema.getImports().values().iterator(); it2.hasNext();) {
				String schemaLocation = (String) it2.next();
				URI newUri = URI.absolutize(fromLocation, schemaLocation);
				storeSchema(newUri, URI.absolutize(fileNameUri, schemaLocation).toString(), schemaLocation, false);
			}
		}
		if (flushIndex) {
			flushIndex();
		}
	}

	/**
	 * Imports an XML Schema including any referenced XML Schema files from the
	 * specified location <code>fromLocation</code>. If <code>fileName</code> is
	 * neither <code>null</code> nor equal to the empty String
	 * <code>&quot;&quot;</code>, the schema will be stored within the
	 * repository to a file with that name. Otherwise, if it is
	 * <code>null</code>, a file name will be derived from the URI the schema is
	 * loaded from (<code>fromLocation</code>). Finally, if
	 * <code>fileName</code> is equal to the empty String, the schema file will
	 * be searched for its target namespace and a file name will be derived
	 * there from.
	 * 
	 * @param fromLocation the location to load the file from
	 * @param fileName the name of the file to store the imported schema to
	 *            within the repository; may be <code>null</code> or the empty
	 *            String
	 * @throws IOException if either accessing the schema or any of the files it
	 *             references or writing into the repository fails
	 */
	public void storeSchema(URI fromLocation, String fileName) throws IOException {
		storeSchema(fromLocation, fileName, null, true);
	}

	private void storeSchema(URI fromLocation, String fileName, String schemaLocation, boolean flushIndex) throws IOException {
		if (Log.isDebug()) {
			Log.debug("Importing XML Schema from " + fromLocation);
		}
		Schema schema = null;
		if (fileName == null) {
			fileName = fromLocation.toString();
		} else if ("".equals(fileName)) {
			try {
				schema = Schema.parse(fromLocation, false);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
			fileName = schema.getTargetNamespace();
		}
		String outputPath = REPO_PATH + fs.fileSeparator() + fs.escapeFileName(fileName);
		if (fs.fileExists(outputPath)) {
			if (Log.isDebug()) {
				Log.debug("WSDL Repository resource already exists: " + outputPath);
			}
			return;
		}
		ResourceLoader rl = DPWSFramework.getResourceAsStream(fromLocation);
		InputStream in = rl.getInputStream();
		if (in == null) {
			throw new IOException("Unable to read from " + fromLocation);
		}
		OutputStream out = fs.writeFile(outputPath);
		byte[] buffer = new byte[READ_BUFFER_SIZE];
		int length;
		while ((length = in.read(buffer)) != -1) {
			out.write(buffer, 0, length);
		}
		out.flush();
		out.close();
		in.close();
		if (schema == null) {
			try {
				schema = Schema.parse(fromLocation, false);
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
		if (schemaLocation == null) {
			schemaLocation = fileName;
		}
		synchronized (index) {
			String ns = schema.getTargetNamespace();
			if (ns != null) {
				index.put(schemaLocation + '|' + ns, outputPath);
			} else {
				index.put(schemaLocation, outputPath);
			}
		}
		URI fileNameUri = new URI(fileName);
		for (Iterator it2 = schema.getIncludes().iterator(); it2.hasNext();) {
			String childSchemaLocation = (String) it2.next();
			URI newUri = URI.absolutize(fromLocation, childSchemaLocation);
			storeSchema(newUri, URI.absolutize(fileNameUri, childSchemaLocation).toString(), childSchemaLocation, false);
		}
		for (Iterator it = schema.getImports().values().iterator(); it.hasNext();) {
			String importLocation = (String) it.next();
			URI newUri = URI.absolutize(fromLocation, importLocation);
			storeSchema(newUri, URI.absolutize(fileNameUri, importLocation).toString(), importLocation, false);
		}
		if (flushIndex) {
			flushIndex();
		}
	}

	public WSDL getWSDL(String fileName) {
		String filePath = REPO_PATH + fs.fileSeparator() + fs.escapeFileName(fileName);
		try {
			InputStream in = fs.readFile(filePath);
			try {
				WSDL wsdl = WSDL.parse(in, true);
				return wsdl;
			} catch (IOException e) {
				Log.error("Unable to read WSDL file " + filePath + ": " + e.getMessage());
			} catch (XmlPullParserException e) {
				Log.error("Ill formatted WSDL file " + filePath + ": " + e.getMessage());
			} finally {
				in.close();
			}
		} catch (IOException e) {
			if (Log.isDebug()) {
				Log.debug("WSDL file not found within WSDL Repository: " + filePath);
			}
		}
		return null;
	}

	public void delete(QName portType) {
		synchronized (index) {
			String filePath = (String) index.get(portType);
			if (filePath != null) {
				fs.deleteFile(filePath);
				index.remove(portType);
				try {
					flushIndex();
				} catch (IOException e) {
					Log.warn("Unable to write WSDL Repository index file: " + e.getMessage());
				}
			}
		}
	}

	/**
	 * Removes the entire content of the WSDL repository.
	 */
	public void clear() {
		synchronized (index) {
			for (Iterator it = index.values().iterator(); it.hasNext();) {
				String filePath = (String) it.next();
				fs.deleteFile(filePath);
			}
			fs.deleteFile(REPO_PATH + fs.fileSeparator() + INDEX_FILE);
			index.clear();
		}
	}

	// /**
	// *
	// */
	// public void updateStore() {
	// String[] knownWsdlFiles = fs.listFiles(REPO_PATH);
	// if (knownWsdlFiles == null) {
	// index.clear();
	// return;
	// }
	// for (int i = 0; i < knownWsdlFiles.length; i++) {
	// String wsdlFile = knownWsdlFiles[i];
	// String filePath = REPO_PATH + fs.fileSeparator() + wsdlFile;
	//
	// // add
	// if (!index.containsKey(filePath)) {
	// try {
	// InputStream in = fs.readFile(filePath);
	// try {
	// WSDL wsdl = WSDL.parse(in, false);
	// in.close();
	// index(wsdl, filePath);
	// } catch (XmlPullParserException e) {
	// Log.error("Ill formatted WSDL file " + filePath + ": " + e.getMessage());
	// }
	// } catch (IOException e) {
	// Log.error("Unable to read WSDL file " + filePath + ": " +
	// e.getMessage());
	// }
	// }
	// }
	// ArrayList l = new ArrayList();
	// // remove
	// Iterator itPortType = index.keySet().iterator();
	// while (itPortType.hasNext()) {
	// QName portType = (QName) itPortType.next();
	// int i;
	// for (i = 0; i < knownWsdlFiles.length; i++) {
	// if ((REPO_PATH + fs.fileSeparator() +
	// knownWsdlFiles[i]).equals(index.get(portType))) break;
	// }
	// if (i == knownWsdlFiles.length) l.add(portType);
	// }
	// for (Iterator it = l.iterator(); it.hasNext();)
	// index.remove(it.next());
	// }

	/**
	 * @param wsdl
	 * @param filePath
	 */
	private void index(WSDL wsdl, String filePath) {
		synchronized (index) {
			for (Iterator it = wsdl.getPortTypes(); it.hasNext();) {
				WSDLPortType portType = (WSDLPortType) it.next();
				index.put(portType.getName(), filePath);
			}
		}
	}

	// private void initStore() {
	// String[] knownWsdlFiles = fs.listFiles(REPO_PATH);
	// if (knownWsdlFiles == null) {
	// return;
	// }
	// for (int i = 0; i < knownWsdlFiles.length; i++) {
	// String wsdlFile = knownWsdlFiles[i];
	// String filePath = REPO_PATH + fs.fileSeparator() + wsdlFile;
	// try {
	// InputStream in = fs.readFile(filePath);
	// try {
	// WSDL wsdl = WSDL.parse(in, false);
	// in.close();
	// index(wsdl, filePath);
	// } catch (XmlPullParserException e) {
	// Log.error("Ill formatted WSDL file " + filePath + ": " + e.getMessage());
	// }
	// } catch (IOException e) {
	// Log.error("Unable to read WSDL file " + filePath + ": " +
	// e.getMessage());
	// }
	// }
	// }

	private void loadIndex() throws IOException {
		synchronized (index) {
			InputStream in = fs.readFile(REPO_PATH + fs.fileSeparator() + INDEX_FILE);
			if (in == null) {
				if (Log.isDebug()) {
					Log.debug("No WSDL Repository index file available.");
				}
				return;
			}
			try {
				Reader reader = new InputStreamReader(in);
				int c;
				StringBuffer buffer = new StringBuffer(64);
				while ((c = reader.read()) != -1) {
					int recordCode;
					switch (c) {
						case 'w': // WSDL record, expect QName as key in James
									// Clark's
									// notation
						case 's': // XML Schema record, expect String as key
							recordCode = c;
							break;
						case '\n': // empty line
							continue;
						default: // unexpected record code, consume entire line
							while ((c = reader.read()) != -1 && c != '\n')
								;
							continue;
					}
					if (buffer == null) {
						buffer = new StringBuffer(64);
					} else if (buffer.length() > 0) {
						buffer.delete(0, buffer.length());
					}
					// read the key into buffer
					while ((c = reader.read()) != -1 && c != '=' && c != '\n') {
						buffer.append((char) c);
					}
					switch (c) {
						case -1:
							Log.warn("Unexpected end of stream while reading WSDL Repository index file.");
							return;
						case '\n':
							Log.warn("Unexpected end of line while reading WSDL Repository index file. Buffer contents: " + buffer);
							continue;
						default: // equality sign, start reading value next
					}
					String key = buffer.toString();
					if (buffer.length() > 0) {
						buffer.delete(0, buffer.length());
					}
					while ((c = reader.read()) != -1 && c != '\n') {
						buffer.append((char) c);
					}
					switch (recordCode) {
						case 'w':
							int idx = key.indexOf('}');
							String ns = null;
							if (idx != -1) {
								ns = key.substring(key.charAt(0) == '{' ? 1 : 0, idx);
								key = key.substring(idx + 1);
							}
							QName portType = new QName(key, ns);
							index.put(portType, buffer.toString());
							break;
						case 's':
							index.put(key, buffer.toString());
							break;
					}
				}
			} finally {
				in.close();
			}
		}
	}

	private void flushIndex() throws IOException {
		synchronized (index) {
			if (index.isEmpty()) {
				return;
			}
			OutputStream fout = fs.writeFile(REPO_PATH + fs.fileSeparator() + INDEX_FILE);
			try {
				Writer writer = new OutputStreamWriter(fout);
				for (Iterator it = index.entrySet().iterator(); it.hasNext();) {
					Entry ent = (Entry) it.next();
					Object key = ent.getKey();
					if (key instanceof QName) {
						// WSDL file
						writer.write('w');
					} else {
						// string, i.e. XML Schema
						writer.write('s');
					}
					writer.write(key.toString());
					writer.write('=');
					writer.write(ent.getValue().toString());
					writer.write('\n');
				}
				writer.flush();
				writer.close();
				fout.flush();
				if (Log.isDebug()) {
					Log.debug("Flushing WSDL Repository index file done.");
				}
			} finally {
				fout.close();
			}
		}
	}

}
