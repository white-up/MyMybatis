package sqlsession;

import configurationEntity.Configuration;

public class DefaultSqlSessionFactory implements SqlSessionFactory{
    private Configuration configuration;
    public DefaultSqlSessionFactory(Configuration configuration) {
    this.configuration=configuration;
    }

    @Override
    public SqlSession openSession() {
        return null;
    }
}