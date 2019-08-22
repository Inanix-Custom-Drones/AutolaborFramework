package cn.autolabor.util.json;

import cn.autolabor.util.Sugar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonParser {

    private final String json;
    private final int jsonSize;
    private int index = 0;
    private CommentFlag commentFlag;
    public JsonParser(String json) {
        this.json = json;
        this.jsonSize = json.length();
        this.commentFlag = CommentFlag.UNCOMMENT;
    }

    public static void main(String[] args) {
        String json = "{\"web-app\":{\"servlet\":[{\"servlet-name\":\"cofaxCDS\",\"servlet-class\":\"org.cofax.cds.CDSServlet\",\"init-param\":{\"configGlossary:installationAt\":\"Philadelphia, PA\",\"configGlossary:adminEmail\":\"ksm@pobox.com\",\"configGlossary:poweredBy\":\"Cofax\",\"configGlossary:poweredByIcon\":\"/images/cofax.gif\",\"configGlossary:staticPath\":\"/content/static\",\"templateProcessorClass\":\"org.cofax.WysiwygTemplate\",\"templateLoaderClass\":\"org.cofax.FilesTemplateLoader\",\"templatePath\":\"templates\",\"templateOverridePath\":\"\",\"defaultListTemplate\":\"listTemplate.htm\",\"defaultFileTemplate\":\"articleTemplate.htm\",\"useJSP\":false,\"jspListTemplate\":\"listTemplate.jsp\",\"jspFileTemplate\":\"articleTemplate.jsp\",\"cachePackageTagsTrack\":200,\"cachePackageTagsStore\":200,\"cachePackageTagsRefresh\":60,\"cacheTemplatesTrack\":100,\"cacheTemplatesStore\":50,\"cacheTemplatesRefresh\":15,\"cachePagesTrack\":200,\"cachePagesStore\":100,\"cachePagesRefresh\":10,\"cachePagesDirtyRead\":10,\"searchEngineListTemplate\":\"forSearchEnginesList.htm\",\"searchEngineFileTemplate\":\"forSearchEngines.htm\",\"searchEngineRobotsDb\":\"WEB-INF/robots.db\",\"useDataStore\":true,\"dataStoreClass\":\"org.cofax.SqlDataStore\",\"redirectionClass\":\"org.cofax.SqlRedirection\",\"dataStoreName\":\"cofax\",\"dataStoreDriver\":\"com.microsoft.jdbc.sqlserver.SQLServerDriver\",\"dataStoreUrl\":\"jdbc:microsoft:sqlserver://LOCALHOST:1433;DatabaseName=goon\",\"dataStoreUser\":\"sa\",\"dataStorePassword\":\"dataStoreTestQuery\",\"dataStoreTestQuery\":\"SET NOCOUNT ON;select test='test';\",\"dataStoreLogFile\":\"/usr/local/tomcat/logs/datastore.log\",\"dataStoreInitConns\":10,\"dataStoreMaxConns\":100,\"dataStoreConnUsageLimit\":100,\"dataStoreLogLevel\":\"debug\",\"maxUrlLength\":500}},{\"servlet-name\":\"cofaxEmail\",\"servlet-class\":\"org.cofax.cds.EmailServlet\",\"init-param\":{\"mailHost\":\"mail1\",\"mailHostOverride\":\"mail2\"}},{\"servlet-name\":\"cofaxAdmin\",\"servlet-class\":\"org.cofax.cds.AdminServlet\"},{\"servlet-name\":\"fileServlet\",\"servlet-class\":\"org.cofax.cds.FileServlet\"},{\"servlet-name\":\"cofaxTools\",\"servlet-class\":\"org.cofax.cms.CofaxToolsServlet\",\"init-param\":{\"templatePath\":\"toolstemplates/\",\"log\":1,\"logLocation\":\"/usr/local/tomcat/logs/CofaxTools.log\",\"logMaxSize\":\"\",\"dataLog\":1,\"dataLogLocation\":\"/usr/local/tomcat/logs/dataLog.log\",\"dataLogMaxSize\":\"\",\"removePageCache\":\"/content/admin/remove?cache=pages&id=\",\"removeTemplateCache\":\"/content/admin/remove?cache=templates&id=\",\"fileTransferFolder\":\"/usr/local/tomcat/webapps/content/fileTransferFolder\",\"lookInContext\":1,\"adminGroupID\":4,\"betaServer\":true}}],\"servlet-mapping\":{\"cofaxCDS\":\"/\",\"cofaxEmail\":\"/cofaxutil/aemail/*\",\"cofaxAdmin\":\"/admin/*\",\"fileServlet\":\"/static/*\",\"cofaxTools\":\"/tools/*\"},\"taglib\":{\"taglib-uri\":\"cofax.tld\",\"taglib-location\":\"/WEB-INF/tlds/cofax.tld\"}}}";
        //        String json = "{\"menu\":{\"header\":\"SVG Viewer\",\"items\":[{\"id\":\"Open\"},{\"id\":\"OpenNew\",\"label\":\"Open New\"},null,{\"id\":\"ZoomIn\",\"label\":\"Zoom In\"},{\"id\":\"ZoomOut\",\"label\":\"Zoom Out\"},{\"id\":\"OriginalView\",\"label\":\"Original View\"},null,{\"id\":\"Quality\"},{\"id\":\"Pause\"},{\"id\":\"Mute\"},null,{\"id\":\"Find\",\"label\":\"Find...\"},{\"id\":\"FindAgain\",\"label\":\"Find Again\"},{\"id\":\"Copy\"},{\"id\":\"CopyAgain\",\"label\":\"Copy Again\"},{\"id\":\"CopySVG\",\"label\":\"Copy SVG\"},{\"id\":\"ViewSVG\",\"label\":\"View SVG\"},{\"id\":\"ViewSource\",\"label\":\"View Source\"},{\"id\":\"SaveAs\",\"label\":\"Save As\"},null,{\"id\":\"Help\"},{\"id\":\"About\",\"label\":\"About Adobe CVG Viewer...\"}]}}";
        //        String json = "{\"widget\":{\"debug\":\"on\",\"window\":{\"title\":\"Sample Konfabulator Widget\",\"name\":\"main_window\",\"width\":500,\"height\":500},\"image\":{\"src\":\"Images/Sun.png\",\"name\":\"sun1\",\"hOffset\":250,\"vOffset\":250,\"alignment\":\"center\"},\"text\":{\"data\":\"Click Here\",\"size\":36,\"style\":\"bold\",\"name\":\"text1\",\"hOffset\":250,\"vOffset\":100,\"alignment\":\"center\",\"onMouseUp\":\"sun1.opacity = (sun1.opacity / 100) * 90;\"}}}";
        JsonParser parser = new JsonParser(json);
        JsonNode jsonNode = parser.parseNode();
        System.out.println(jsonNode.prettyPrint(0, true));
        System.out.println(jsonNode.getListRaw("web-app.servlet"));
        //        List<JsonNode> jsonNodes = jsonNode.getList("web-app.servlet");
        //
        //        for (int i = 0; i<jsonNodes.size(); i++){
        //            System.out.println(jsonNodes.get(i).prettyPrint(0, true));
        //        }
        //        System.out.println(subJsonNode.getString("init-param.fileTransferFolder"));
    }

    public JsonNode parseNode() {
        JsonNode jsonNode = new JsonNode();
        char c = getValidChar();
        if (isEnd(c)) {
            return null;
        }
        if (!isObjectStart(c) && !isArrayStart(c)) {
            parseKey(jsonNode);
            parseKeyValueSeparation();
        }
        parseValue(jsonNode);
        return jsonNode;
    }

    private void parseValue(JsonNode jsonNode) {
        char c = getValidChar();
        if (c == 'T' || c == 't') {
            if (checkChars("true")) {
                jsonNode.setValue(true);
                jsonNode.setNodeType(JsonNode.NodeType.BOOLEAN);
            }
        } else if (c == 'F' || c == 'f') {
            if (checkChars("false")) {
                jsonNode.setValue(false);
                jsonNode.setNodeType(JsonNode.NodeType.BOOLEAN);
            }
        } else if (c == 'N' || c == 'n') {
            if (checkChars("null")) {
                jsonNode.setValue(null);
                jsonNode.setNodeType(JsonNode.NodeType.NULL);
            }
        } else if (isStringStart(c)) {
            jsonNode.setValue(parseString());
            jsonNode.setNodeType(JsonNode.NodeType.STRING);
        } else if (isArrayStart(c)) {
            jsonNode.setValue(parseArray());
            jsonNode.setNodeType(JsonNode.NodeType.ARRAY);
        } else if (isObjectStart(c)) {
            jsonNode.setValue(parseObject());
            jsonNode.setNodeType(JsonNode.NodeType.MAP);
        } else {
            Object number = parseNumber();
            jsonNode.setValue(number);
            jsonNode.setNodeType(Double.class.isInstance(number) ? JsonNode.NodeType.NUMBER_DOUBLE : JsonNode.NodeType.NUMBER_INT);
        }
    }

    private boolean checkChars(String str) {
        char[] chars = str.toCharArray();
        int tmpIndex = index;
        for (char ch : chars) {
            if (tmpIndex >= this.jsonSize) {
                return false;
            }
            char c = this.json.charAt(tmpIndex++);
            if (Character.toLowerCase(c) != Character.toLowerCase(ch)) {
                return false;
            }
        }
        index = tmpIndex;
        return true;
    }

    private Map<String, JsonNode> parseObject() {
        Map<String, JsonNode> map = new HashMap<>();
        boolean parseFlag = false;
        boolean loopFlag = true;
        char c;
        while (loopFlag) {
            c = getValidChar();
            if (isEnd(c)) {
                break;
            }
            if (parseFlag) {
                if (isObjectEnd(c)) {
                    loopFlag = false;
                } else {
                    if (!isObjectSeparation(c)) {
                        JsonNode subNode = parseNode();
                        map.put(subNode.getKey(), subNode);
                        continue;
                    }
                }
            } else {
                if (isObjectStart(c)) {
                    parseFlag = true;
                }
            }
            index++;
        }
        return map;
    }

    private List<JsonNode> parseArray() {
        ArrayList<JsonNode> list = new ArrayList<>();
        boolean parseFlag = false;
        boolean loopFlag = true;
        char c;
        while (loopFlag) {
            c = getValidChar();
            if (isEnd(c)) {
                break;
            }
            if (parseFlag) {
                if (isArrayEnd(c)) {
                    loopFlag = false;
                } else {
                    if (!isArraySeparation(c)) {
                        JsonNode subNode = new JsonNode();
                        parseValue(subNode);
                        list.add(subNode);
                        continue;
                    }
                }
            } else {
                if (isArrayStart(c)) {
                    parseFlag = true;
                }
            }
            index++;
        }
        return list;
    }

    private Object parseNumber() {
        StringBuilder sb = new StringBuilder();
        boolean containDot = false;
        char c;
        while (true) {
            c = getCurrentChar();
            if (isEnd(c)) {
                break;
            }
            if (isNumber(c)) {
                if (!containDot && c == '.') {
                    containDot = true;
                }
                sb.append(c);
            } else {
                break;
            }
            index++;
        }

        String numberString = sb.toString();
        try {
            if (containDot) {
                return Double.parseDouble(numberString);
            } else {
                return Integer.parseInt(numberString);
            }
        } catch (NumberFormatException e) {
            throw Sugar.makeThrow("invalid number value: %s", numberString);
        }
    }

    private String parseString() {
        StringBuilder sb = new StringBuilder();
        boolean parseFlag = false;
        boolean loopFlag = true;
        char c;
        while (loopFlag) {
            c = getCurrentChar();
            if (isEnd(c)) {
                break;
            }
            if (parseFlag) {
                if (isEscape(c)) {
                    char next = getNextChar();
                    switch (next) {
                        case 'r':
                            sb.append('\r');
                            break;
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'b':
                            sb.append('\b');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case '\"':
                            sb.append('\"');
                            break;
                        default:
                            throw Sugar.makeThrow("unknown escape char \\%s", next);
                    }
                } else if (isStringEnd(c)) {
                    loopFlag = false;
                } else {
                    sb.append(c);
                }
            } else {
                if (isStringStart(c)) {
                    parseFlag = true;
                }
            }
            index++;
        }
        return sb.toString();
    }


    private void parseKeyValueSeparation() {
        char c = getValidChar();
        if (isKeyValueSeparation(c)) {
            index++;
        } else {
            throw Sugar.makeThrow("unexpected separation %s between key and value", c);
        }
    }

    private void parseKey(JsonNode jsonNode) {
        char c = getValidChar();
        if (isObjectStart(c) || isArrayStart(c)) {
            jsonNode.setKey(null);
        } else {
            StringBuilder sb = new StringBuilder();
            boolean parseFlag = false;
            boolean loopFlag = true;
            boolean isStringKey = false;
            while (loopFlag) {
                c = getCurrentChar();
                if (isEnd(c)) {
                    break;
                }
                if (parseFlag) {
                    if (isSpace(c) || isStringEnd(c)) {
                        loopFlag = false;
                    } else if (!isStringKey && isKeyValueSeparation(c)) {
                        break;
                    } else {
                        sb.append(c);
                    }
                } else {
                    if (!isStringStart(c)) {
                        sb.append(c);
                        isStringKey = false;
                    } else {
                        isStringKey = true;
                    }
                    parseFlag = true;
                }
                index++;
            }
            jsonNode.setKey(sb.toString().trim());
        }
    }

    private char getValidChar() {
        boolean loopFlag = true;
        char c = 0;
        while (loopFlag) {
            c = getCurrentChar();
            if (isEnd(c)) {
                break;
            }
            switch (commentFlag) {
                case UNCOMMENT:
                    if (isSpace(c) || isReturn(c)) {
                        index++;
                    } else if (isCommentFlag(c)) {
                        commentFlag = CommentFlag.COMMENT;
                        index++;
                    } else {
                        loopFlag = false;
                    }
                    break;
                case COMMENT:
                    if (isReturn(c)) {
                        commentFlag = CommentFlag.COMMENT_RETURN;
                        index++;
                    } else {
                        index++;
                    }
                    break;
                case COMMENT_RETURN:
                    if (isReturn(c)) {
                        index++;
                    } else {
                        commentFlag = CommentFlag.UNCOMMENT;
                        index++;
                    }
                    break;
            }
        }
        return c;
    }

    private boolean isKeyValueSeparation(char c) {
        return (c == ':' || c == '=');
    }

    private boolean isNumber(char c) {
        return (c >= '0' && c <= '9' || c == '+' || c == '-' || c == '.');
    }

    private boolean isEscape(char c) {
        return c == '\\';
    }

    private boolean isObjectStart(char c) {
        return c == '{';
    }

    private boolean isObjectSeparation(char c) {
        return c == ',';
    }

    private boolean isObjectEnd(char c) {
        return c == '}';
    }

    private boolean isArrayStart(char c) {
        return c == '[';
    }

    private boolean isArraySeparation(char c) {
        return c == ',';
    }

    private boolean isArrayEnd(char c) {
        return c == ']';
    }

    private boolean isStringStart(char c) {
        return c == '\"';
    }

    private boolean isStringEnd(char c) {
        return c == '\"';
    }

    private boolean isCommentFlag(char c) {
        return c == '#';
    }

    private boolean isSpace(char c) {
        return c == ' ' || c == '\t';
    }

    private boolean isReturn(char c) {
        return c == '\r' || c == '\n';
    }

    private boolean isEnd(char c) {
        return c == 0x00;
    }

    private char getCurrentChar() {
        return isEndString() ? 0x00 : json.charAt(index);
    }

    private char getNextChar() {
        index++;
        return getCurrentChar();
    }

    private boolean isEndString() {
        return this.index >= this.jsonSize;
    }

    private void assertIndex() {
        if (this.index >= this.jsonSize) {
            throw Sugar.makeThrow("|%s| -> unexpected string end.", json);
        }
    }

    private enum CommentFlag {
        COMMENT, UNCOMMENT, COMMENT_RETURN
    }


}
