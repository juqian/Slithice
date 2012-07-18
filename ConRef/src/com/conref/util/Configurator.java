/* 
 * @author Ju Qian{jqian@live.com}
 * @date 2006-03-22
 * @version 0.01
 */

package com.conref.util;

import java.io.*;
import java.util.Properties;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class Configurator {
    //使用DefaultHandler的好处 是 不必陈列出所有方法,
    private class ConfigParser extends DefaultHandler{ 
        private Properties _props;   //用Properties存放各种配置信息
        //private String _currentSet;
        //private String _currentName;
        private StringBuffer _currentValue = new StringBuffer();

        public ConfigParser(){
            this._props = new Properties();
        }

        public Properties getProps() {
            return this._props;
        }

        // 定义开始解析元素的方法. 这里是将<xxx>中的名称xxx提取出来.
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            _currentValue.delete(0, _currentValue.length());
            //_currentName = qName;
        }

        // 这里是将<xxx></xxx>之间的值加入到currentValue
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            _currentValue.append(ch, start, length);
        }

        // 在遇到</xxx>结束后,将之前的名称和值一一对应保存在props中
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            _props.put(qName.toLowerCase(),_currentValue.toString().trim());
        }
    }
    
    /**
     * Parse configuration XML into a Properties project
     * @param xmlpath  
     */
    public Properties parse(String xmlpath){
    	Properties props = null;
        try {
            ConfigParser handler = new ConfigParser();            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();

            try {
                parser.parse(xmlpath, handler);                
                props = handler.getProps();
            } finally {
                factory = null;
                parser = null;
                handler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return props;
    }    

    public Properties parse(InputStream is){
    	Properties props = null;
        try {
            ConfigParser handler = new ConfigParser();            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();

            try {
                parser.parse(is, handler);                
                props = handler.getProps();
            } finally {
                factory = null;
                parser = null;
                handler = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return props;
    }
    
    
    //tests
    public static void main(String args[]){
    	Configurator conf = new Configurator();
        Properties props = conf.parse("./src/config.xml");
        String path=props.getProperty("subject_path");
        
        System.out.println(props);
        System.out.println("Path:"+path);        
    }
}
