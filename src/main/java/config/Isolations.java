package config;

/**
 * Created by pengan on 16-9-28.
 */
public interface Isolations {

    int READ_UNCOMMITTED = 1;
    int READ_COMMITTED = 2;
    int REPEATED_READ = 3;
    int SERIALIZABLE = 4;

}
