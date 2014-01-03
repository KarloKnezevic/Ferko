package hr.fer.zemris.jcms.model;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Razred predstavlja ISVU podatke o primjerku kolegija.
 * 
 * @author marcupic
 *
 */
@Entity
@Table(name="course_instance_isvu_data")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class CourseInstanceIsvuData implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private String id;
	private String data;
	private Predmet cdata;
	private boolean open;
	
	public CourseInstanceIsvuData() {
	}
	
	/**
	 * Identifikator. Identifikator je oblika "2007Z/19674".
	 * 
	 * @return
	 */
	@Id
	@Column(length=16)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}

	@Column(nullable=true,length=10000)
	public String getData() {
		return data;
	}
	public void setData(String data) {
		open = false;
		this.data = data;
	}

	@Transient
	public Predmet getCdata() {
		if(!open) {
			open = true;
			cdata = new Predmet(getData());
		}
		return cdata;
	}

	@Transient
	public boolean isOpen() {
		return open;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof CourseInstanceIsvuData))
			return false;
		final CourseInstanceIsvuData other = (CourseInstanceIsvuData) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}
	
	public static class Predmet {
		String[] opterecenja;
		List<String> nositelji;
		List<String[]> izvodaci;
		String opis;
		List<String> liter;
		
		public Predmet(String text) {
			fromText(text);
		}
		
		protected void fromText(String text) {
			Properties prop = new Properties();
			if(text!=null && text.length()!=0) {
				try {
					prop.load(new StringReader(text));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			String t = prop.getProperty("opterecenja","");
			if(t!=null && t.length()!=0) {
				opterecenja = t.split("\n");
			} else {
				opterecenja = new String[] {"?", "?", "?", "?", "?"};
			}
			t = prop.getProperty("opis","");
			if(t!=null && t.length()!=0) {
				opis = t;
			} else {
				opis = "";
			}
			t = prop.getProperty("nositelji","");
			if(t!=null && t.length()!=0) {
				nositelji = Arrays.asList(t.split("\n"));
			} else {
				nositelji = new ArrayList<String>();
			}
			t = prop.getProperty("izvodaci","");
			if(t!=null && t.length()!=0) {
				String[] elems = t.split("\n");
				izvodaci = new ArrayList<String[]>(elems.length);
				for(String e : elems) {
					izvodaci.add(e.split("\\|"));
				}
				izvodaci = Arrays.asList();
			} else {
				izvodaci = new ArrayList<String[]>();
			}
			t = prop.getProperty("liter","");
			if(t!=null && t.length()!=0) {
				liter = Arrays.asList(t.split("\n"));
			} else {
				liter = new ArrayList<String>();
			}
		}
		
		public String[] getOpterecenja() {
			return opterecenja;
		}
		public List<String> getNositelji() {
			return nositelji;
		}
		public List<String[]> getIzvodaci() {
			return izvodaci;
		}
		public String getOpis() {
			return opis;
		}
		public List<String> getLiter() {
			return liter;
		}
	}
}
