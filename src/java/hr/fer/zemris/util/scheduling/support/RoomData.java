package hr.fer.zemris.util.scheduling.support;

import java.io.Serializable;


public class RoomData implements Serializable{

	private static final long serialVersionUID = -8251122923664422676L;
	String name;
	String id;
	int capacity;
	
	public RoomData(){
		
	}
	
	public RoomData(String id, String name, int capacity){
		this.id=id;
		this.name=name;
		this.capacity =capacity;
	}

	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getCapacity() {
		return capacity;
	}

	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		result = prime * result + capacity;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof RoomData))
			return false;
		RoomData other = (RoomData) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		if(getCapacity()!=other.getCapacity())
			return false;
		
		return true;
	}

	
}

