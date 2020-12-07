package com.sun.xml;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.IOException;

public class ParseXml {
    public static void main(String[] args) throws IOException, SAXException, XPathExpressionException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        // 开启验证
        documentBuilderFactory.setValidating(true);
        documentBuilderFactory.setNamespaceAware(false);
        documentBuilderFactory.setIgnoringComments(true);
        documentBuilderFactory.setIgnoringElementContentWhitespace(false);
        documentBuilderFactory.setCoalescing(false);
        documentBuilderFactory.setExpandEntityReferences(true);

        // 创建documentBuilder
        DocumentBuilder builder = documentBuilderFactory.newDocumentBuilder();
        // 设置异常处理对象
        builder.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(SAXParseException exception) throws SAXException {
                System.out.println("WARN：" + exception.getMessage());
            }

            @Override
            public void error(SAXParseException exception) throws SAXException {
                System.out.println("error：" + exception.getMessage());
            }

            @Override
            public void fatalError(SAXParseException exception) throws SAXException {
                System.out.println("fatalError：" + exception.getMessage());
            }
        });

        // 将文档加载到一个document中
        Document doc = builder.parse("src/main/java/com/sun/xml/test.xml");
        //创建xPathFactory
        XPathFactory xPathFactory = XPathFactory.newInstance();
        // 创建xpath对象
        XPath xPath = xPathFactory.newXPath();
        //String age = xPath.evaluate("//student[name='zhangsan']/age/text()", XPathConstants.NUMBER);
        //System.out.println("age=" + age);

        // 编译xpath表达式
        XPathExpression expr = xPath.compile("//student[name='zhangsan']/age/text()");
        Object result = expr.evaluate(doc, XPathConstants.NODESET);
        NodeList nodeList = (NodeList) result;
        for (int i = 0; i < nodeList.getLength(); i++) {
            System.out.println(nodeList.item(i).getNodeValue());
        }




    }
}
