package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.XMLUtil;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

import java.math.BigDecimal;

/**
 * This serializer is responsible for floating point numbers.
 *
 * @author Tim Roes
 */
public class DoubleSerializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return Double.valueOf(XMLUtil.getOnlyTextContent(content.getChildNodes()));
    }

    public XmlElement serialize(Object object) {
        // Turn double value of object into a BigDecimal to get the
        // right decimal point format.
        BigDecimal bd = BigDecimal.valueOf(((Number) object).doubleValue());
        return XMLUtil.makeXmlTag(SerializerHandler.TYPE_DOUBLE, bd.toPlainString());
    }

}
