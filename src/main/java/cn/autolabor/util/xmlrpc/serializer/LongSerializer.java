package cn.autolabor.util.xmlrpc.serializer;

import cn.autolabor.util.xmlrpc.XMLRPCException;
import cn.autolabor.util.xmlrpc.XMLUtil;
import cn.autolabor.util.xmlrpc.xmlcreator.XmlElement;
import org.w3c.dom.Element;

/**
 * @author Tim Roes
 */
class LongSerializer implements Serializer {

    public Object deserialize(Element content) throws XMLRPCException {
        return Long.parseLong(XMLUtil.getOnlyTextContent(content.getChildNodes()));
    }

    public XmlElement serialize(Object object) {
        return XMLUtil.makeXmlTag(SerializerHandler.TYPE_LONG,
                ((Long) object).toString());
    }

}
