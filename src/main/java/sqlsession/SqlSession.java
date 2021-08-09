package sqlsession;
/**
 * 用于创建dao接口的代理对象
 */
public interface SqlSession {
    /**
     * 根据参数创建代理对象
     * */
    <T> T getMapper(Class<T> daoInterfaceClass);
    /**
     * 释放资源
     */
    void close();
}