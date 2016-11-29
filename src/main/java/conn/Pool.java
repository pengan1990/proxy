package conn;

import nio.Processor;

import java.io.IOException;

/**
 * Created by pengan on 16-9-30.
 */
public interface Pool<T> {

    /**
     * create an object
     *
     * @return
     */
    T create(Processor processor) throws IOException;

    /**
     * borrow object from pool
     *
     * @return
     */
    T borrowObject(String schema);

    /**
     * clear connection pool
     */
    void clear();

    /**
     * get active connection number
     *
     * @return
     */
    int getNumActive();

    /**
     * get idle connection number
     *
     * @return
     */
    int getNumIdle();

    /**
     * return Object to pool
     *
     * @param obj
     */
    void returnObject(T obj);
}
