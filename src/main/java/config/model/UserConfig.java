package config.model;

import java.util.Set;

public class UserConfig {

	public final static int PRIVI_READ_ONLY = 1; 
	public final static int PRIVI_READ_WRITE = 0; 
	
	public final static int SELECT_LIMIT = -1;

	private final String id; 
	private String name;
	private String password;
	private volatile int privilege; 
	private volatile Set<String> schemas;
	//use for public datapush
	private volatile int selectMaxRows;

	public UserConfig(String id, String name) {
		this.id = id;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Set<String> getSchemas() {
		return schemas;
	}

	public void setSchemas(Set<String> schemas) {
		this.schemas = schemas;
	}

	public String getId() {
		return id;
	}

	public int getPrivilege() {
		return privilege;
	}

	public void setPrivilege(int privilege) {
		this.privilege = privilege;
	}

	public int getSelectMaxRows() {
		return selectMaxRows;
	}

	public void setSelectMaxRows(int selectMaxRows) {
		this.selectMaxRows = selectMaxRows;
	}
	
	@Override
	public String toString() {
		
		return new StringBuilder().append("UserConfig [")
				.append("id =").append(id)
				.append(", user =").append(name)
				.append(", password =").append(password)
				.append(", privilege =").append(privilege)
				.append(", selectMaxRows =").append(selectMaxRows)
				.append(", schemas = ").append(schemas)
				.append(" ]").toString();
	}

}
