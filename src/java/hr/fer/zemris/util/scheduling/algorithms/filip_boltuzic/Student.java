package hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic;

public class Student implements Comparable<Student>{
	public String jmbag;
	private int conflicts;
	
	public Student(String jmbag) {
		this.jmbag=jmbag;
		this.conflicts=0;
	}
	
	public void setConflicts(int n) {
		conflicts=n;
	}
	public int getConflicts(){
		return this.conflicts; 
	}
	public String getJMBAG(){
		return jmbag;
	}
	
	@Override
	public int compareTo(Student o) {
		if(this.conflicts<o.conflicts)
			return 1;
		if(this.conflicts>o.conflicts)
			return -1;
		return this.jmbag.compareTo(o.jmbag);
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Student == false){
			return false;
		}
		Student s = (Student) obj;
		if (s.jmbag.equals(this.jmbag)){
			return true;
		}else{
			return false;
		}
		
	}
	
}