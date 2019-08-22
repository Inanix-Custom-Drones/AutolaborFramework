package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.XMLUtil;
import cn.autolabor.util.xmlrpc.util.Base64;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 * @author Tim Roes
 */
public class Base64Serializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return Base64.decode(XMLUtil.getOnlyTextContent(content.getChildNodes()));
    }

    public XmlElement serialize(Object object) {
        return XMLUtil.makeXmlTag(SerializerHandler.TYPE_BASE64,
                Base64.encode((Byte[]) object));
    }

}