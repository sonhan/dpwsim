/*******************************************************************************
 * Copyright (c) 2009 MATERNA Information & Communications. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html. For further
 * project-related information visit http://www.ws4d.org. The most recent
 * version of the JMEDS framework can be obtained from
 * http://sourceforge.net/projects/ws4d-javame.
 ******************************************************************************/
package org.ws4d.java.schema;

import org.ws4d.java.structures.Iterator;
import org.ws4d.java.types.QName;
import org.ws4d.java.util.StringUtil;

/**
 * Abstract class for everything with a name inside XML Schema.
 */
public abstract class NamedObject extends Annotation {

	protected QName		name			= null;

	protected boolean	abstractValue	= false;

	private Schema		parentSchema	= null;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer(StringUtil.formatClassName(getClass()));
		QName name = getName();
		if (name == null) {
			sb.append(" [ anonymous ]");
		} else {
			sb.append(" [ name=").append(name.getLocalPart());
			sb.append(", namespace=").append(name.getNamespace());
			sb.append(" ]");
		}
		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		// if (getClass() != obj.getClass()) return false;
		NamedObject other = (NamedObject) obj;
		if (name == null) {
			if (other.name != null) return false;
		} else if (!name.equals(other.name)) return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	/**
	 * Returns the qualified name.
	 * 
	 * @return the qualified name.
	 */
	public QName getName() {
		return name;
	}

	/**
	 * Sets the qualified name.
	 * 
	 * @param name the qname to set.
	 */
	public void setName(QName name) {
		this.name = name;
	}

	/**
	 * Returns whether this is an abstract schema object or not.
	 * <p>
	 * An abstract object cannot be used inside a XML instance document. It is
	 * only usable in XML Schema.
	 * </p>
	 * 
	 * @return <code>true</code> if this object is abstract, <code>false</code>
	 *         otherwise.
	 */
	public boolean isAbstract() {
		return abstractValue;
	}

	/**
	 * Sets whether this object is abstract or not.
	 * <p>
	 * An abstract object cannot be used inside a XML instance document. It is
	 * only usable in XML Schema.
	 * </p>
	 * 
	 * @param value <code>true</code> if this object is abstract,
	 *            <code>false</code> otherwise.
	 */
	public void setAbstract(boolean value) {
		abstractValue = value;
	}

	/**
	 * Sets a default namespace if no one is set.
	 * 
	 * @param no NamedObject.
	 */
	public void checkNamespace(NamedObject no) {
		if (no != null) {
			QName name = no.getName();
			if (name != null) {
				String namespace = name.getNamespace();
				String localPart = name.getLocalPart();
				String prefix = name.getPrefix();
				int priority = name.getPriority();

				if ("".equals(namespace)) {
					namespace = this.name.getNamespace();
					name = new QName(localPart, namespace, prefix, priority);
					no.setName(name);
					checkSubs(no);
				}
			}
		}
	}

	/**
	 * Returns the <code>xsd:Schema</code> for this object.
	 * <p>
	 * Can be <code>null</code> if this object was not created by parsing a schema.
	 * </p>
	 * 
	 * @return the <code>xsd:Schema</code> for this object, or <code>null</code>.
	 */
	public Schema getParentSchema() {
		return parentSchema;
	}
	
	void setParentSchema(Schema schema) {
		parentSchema = schema;
	}

	private void checkSubs(NamedObject no) {
		Iterator it = null;

		if (no instanceof Type) {
			Type t = (Type) no;

			it = t.attributes();
			while (it.hasNext()) {
				Attribute a = (Attribute) it.next();
				t.checkNamespace(a);
			}

			it = t.attributeGroups();
			while (it.hasNext()) {
				AttributeGroup g = (AttributeGroup) it.next();
				t.checkNamespace(g);
			}

			if (t instanceof ComplexType) {
				ComplexType ct = (ComplexType) t;

				it = ct.elements();

				while (it.hasNext()) {
					Element e = (Element) it.next();
					ct.checkNamespace(e);
				}

				if (ct instanceof ComplexContent) {
					ComplexContent cc = (ComplexContent) ct;

					cc.checkNamespace(cc.base);
				}
			} else if (t instanceof RestrictedSimpleType) {
				RestrictedSimpleType rs = (RestrictedSimpleType) t;

				rs.checkNamespace(rs.base);
			} else if (t instanceof SimpleContent) {
				SimpleContent sc = (SimpleContent) t;

				sc.checkNamespace(sc.base);
			}
		} else if (no instanceof Reference) {
			if (no instanceof Element) {
				Element e = (Element) no;

				e.checkNamespace(e.type);
			} else if (no instanceof Attribute) {
				Attribute a = (Attribute) no;

				a.checkNamespace(a.type);
			} else if (no instanceof AttributeGroup) {
				AttributeGroup ag = (AttributeGroup) no;

				it = ag.attributes();
				while (it.hasNext()) {
					Attribute a = (Attribute) it.next();
					ag.checkNamespace(a);
				}

				it = ag.attributeGroups();
				while (it.hasNext()) {
					AttributeGroup g = (AttributeGroup) it.next();
					ag.checkNamespace(g);
				}
			} else if (no instanceof Group) {
				//
			}
		}
	}

}
