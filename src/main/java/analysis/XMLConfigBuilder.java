package analysis;

import annotations.Select;
import configurationEntity.Configuration;
import configurationEntity.Mapper;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析xml配置文件
 * */
public class XMLConfigBuilder {
    /**
     * 解析主配置文件
     * */
    public static Configuration loadConfiguration(InputStream config){
        try{

            Configuration configurationAns = new Configuration();


            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(config);
            Element root = document.getRootElement();
            //获取所有property节点
            List<Element> propertyElements = root.selectNodes("//property");
            //遍历获取数据库配置信息集合
            for(Element propertyElement : propertyElements){
                String name = propertyElement.attributeValue("name");
                if("driver".equals(name)){
                    String driver = propertyElement.attributeValue("value");
                    configurationAns.setDriver(driver);
                }else if("url".equals(name)){
                    String url = propertyElement.attributeValue("value");
                    configurationAns.setUrl(url);
                }else if("username".equals(name)){
                    String username = propertyElement.attributeValue("value");
                    configurationAns.setUsername(username);
                }else if("password".equals(name)){
                    String password = propertyElement.attributeValue("value");
                    configurationAns.setPassword(password);
                }
            }
            //获取mappers中的所有mapper标签
            List<Element> mapperElements = root.selectNodes("//mappers/mapper");
            //遍历集合判断使用注解/xml
            for (Element mapperElement : mapperElements) {
                Attribute attribute = mapperElement.attribute("resource");
                Map<String,Mapper> mappers = null;
                if(attribute!=null){
                    //使用的是xml
                    System.out.println("xml");
                    String mapperPath = attribute.getValue();
                    //调用方法获取配置文件内容
                    mappers = loadMapperConfiguration(mapperPath);
                }else {
                    //使用注解
                    System.out.println("注解");
                    String classPath = mapperElement.attributeValue("class");
                    mappers = loadMapperAnnotation(classPath);
                }
                if(mappers!=null){
                configurationAns.setMappers(mappers);
                }else {
                    System.out.println("获取配置信息mappers失败");
                }
            }
            return configurationAns;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                config.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    /**
     * 解析xml配置信息并返回map集合
     * */
    public static Map<String,Mapper> loadMapperConfiguration(String mapperPath){
        InputStream in = null;
        try {
            Map<String,Mapper> mappers = new HashMap<>();
            in = getResourceAsStream(mapperPath);
            SAXReader saxReader = new SAXReader();
            Document document = saxReader.read(in);
            Element root = document.getRootElement();
            //获取根节点的namespace属性取值
            String namespace = root.attributeValue("namespace");
            //以select为例获取所有select标签信息
            List<Element> selectElements = root.selectNodes("//select");
            for (Element selectElement : selectElements) {
                //取出id属性的值      组成map中key的部分
                String id = selectElement.attributeValue("id");
                //取出resultType属性的值  组成map中value的部分
                String resultType = selectElement.attributeValue("resultType");
                //取出文本内容            组成map中value的部分
                String queryString = selectElement.getText();
                //创建Key
                String key = namespace+"."+id;
                //创建Value
                Mapper mapper = new Mapper();
                mapper.setQueryString(queryString);
                mapper.setResultType(resultType);
                //把key和value存入mappers中
                mappers.put(key,mapper);
            }
            return mappers;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }finally {
            try {
                in.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    /**
     *解析注解配置信息并返回map集合
     * */
    public static Map<String,Mapper> loadMapperAnnotation(String classPath){
        Map<String,Mapper> mappers = new HashMap<>();
        try {
            Class daoClass = Class.forName(classPath);
            //得到dao接口中的方法数组
            Method[] methods = daoClass.getMethods();
            for (Method method : methods) {
                //取出每一个方法，判断是否有select注解
                boolean isAnnotated = method.isAnnotationPresent(Select.class);
                if(isAnnotated){
                    Mapper mapper = new Mapper();
                    Select selectAnnotated = method.getAnnotation(Select.class);
                    String queryString = selectAnnotated.value();
                    mapper.setQueryString(queryString);
                    Type type = method.getGenericReturnType();//List<User>
                    //判断type是不是参数化的类型
                    if(type instanceof ParameterizedType){
                        //强转
                        ParameterizedType parameterizedType = (ParameterizedType)type;
                        //得到参数化类型中的实际类型参数
                        Type[] types = parameterizedType.getActualTypeArguments();
                        //取出第一个
                        Class domainClass = (Class)types[0];
                        //获取domainClass的类名
                        String resultType = domainClass.getName();
                        //给Mapper赋值
                        mapper.setResultType(resultType);
                    }
                    String methodName = method.getName();
                    String className = method.getDeclaringClass().getName();
                    String key = className+"."+methodName;
                    mappers.put(key,mapper);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


        return null;
    }
    public static InputStream getResourceAsStream(String filePath){
        return XMLConfigBuilder.class.getClassLoader().getResourceAsStream(filePath);
    }

}