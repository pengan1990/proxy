package config.model;

/**
 * Created by jing on 15-12-17.
 */
public class SpecialRouteConfig {
    private final String id;
    private final String tableId;
    private final String column;
    private final String columnValue;
    private final String datanodeId;

    public SpecialRouteConfig(String id,String tableId,String column,String columnValue,String datanodeId){

        this.id = id;
        this.column = column;
        this.tableId = tableId;
        this.columnValue = columnValue;
        this.datanodeId = datanodeId;
    }

    public String getId(){
        return id;
    }

    public String getTableId(){
        return tableId;
    }

    public String getColumn(){
        return column;
    }

    public String getColumnValue(){
        return columnValue;
    }

    public String getDatanodeId(){
        return datanodeId;
    }
}
