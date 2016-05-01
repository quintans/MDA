//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vhudson-jaxb-ri-2.1-661 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2010.04.08 at 06:44:37 PM BST 
//


package pt.quintans.mda;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for copyType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="copyType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="ignore"/>
 *     &lt;enumeration value="overwrite"/>
 *     &lt;enumeration value="append"/>
 *     &lt;enumeration value="injectGenerated"/>
 *     &lt;enumeration value="injectCustom"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "copyType")
@XmlEnum
public enum CopyType {

    @XmlEnumValue("ignore")
    IGNORE("ignore"),
    @XmlEnumValue("overwrite")
    OVERWRITE("overwrite"),
    @XmlEnumValue("append")
    APPEND("append"),
    @XmlEnumValue("injectGenerated")
    INJECT_GENERATED("injectGenerated"),
    @XmlEnumValue("injectCustom")
    INJECT_CUSTOM("injectCustom");
    private final String value;

    CopyType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static CopyType fromValue(String v) {
        for (CopyType c: CopyType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
