package sqlsession;

import configurationEntity.Configuration;

import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;

public class DefaultSqlSession implements SqlSession{
    private Configuration configuration;
    private Connection connection;

    public DefaultSqlSession(Configuration configuration) {
        this.configuration = configuration;
        connection = getConnection(configuration);
    }


    @Override
    public <T> T getMapper(Class<T> daoInterfaceClass) {
        return (T) Proxy.newProxyInstance(daoInterfaceClass.getClassLoader(),
                new Class[]{daoInterfaceClass},new MapperProxy(configuration.getMappers(),connection));
    }
    /**
     * 释放资源
     */
    @Override
    public void close() {
        if(connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Connection getConnection(Configuration configuration){
        try {
            Class.forName(configuration.getDriver());
            return DriverManager.getConnection(configuration.getUrl(), configuration.getUsername(), configuration.getPassword());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}