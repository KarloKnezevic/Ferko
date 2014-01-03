package hr.fer.zemris.jcms.beans.ext;


public class ComponentDefBean {
	private String id;
	private String type;
	private String shortName;
	private String name;
	private double start;
	private double end;
	private double step;
	private int position;
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public double getStart() {
		return start;
	}
	public void setStart(double start) {
		this.start = start;
	}
	public double getEnd() {
		return end;
	}
	public void setEnd(double end) {
		this.end = end;
	}
	public double getStep() {
		return step;
	}
	public void setStep(double step) {
		this.step = step;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public int getPosition() {
		return position;
	}
	public void setPosition(int position) {
		this.position = position;
	}
}
