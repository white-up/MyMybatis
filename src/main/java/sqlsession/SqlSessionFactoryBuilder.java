package sqlsession;

import analysis.XMLConfigBuilder;
import configurationEntity.Configuration;

import java.io.InputStream;

/**
 *  用于创建一个SqlSessionFactory对象
 */
public class SqlSessionFactoryBuilder {
    public SqlSessionFactory build(InputStream config){
        Configuration configuration = XMLConfigBuilder.loadConfiguration(config);
        return  new DefaultSqlSessionFactory(configuration);
    }
}