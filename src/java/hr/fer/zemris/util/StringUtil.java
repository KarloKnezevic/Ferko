package hr.fer.zemris.util;

import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.MessageDigest;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * String Utility Class This is used to encode passwords programmatically
 *
 * <p>
 * <a h
 * ref="StringUtil.java.html"><i>View Source</i></a>
 * </p>
 * 
 * @author <a href="mailto:matt@raibledesigns.com">Matt Raible</a>
 */
public class StringUtil {
    //~ Static fields/initializers =============================================

    private final static Log log = LogFactory.getLog(StringUtil.class);

    public final static Locale HR_LOCALE = new Locale("HR");
    public final static Collator HR_COLLATOR = Collator.getInstance(HR_LOCALE);
    
    //~ Methods ================================================================

    /**
     * Encode a string using algorithm specified in web.xml and return the
     * resulting encrypted password. If exception, the plain credentials
     * string is returned
     *
     * @param password Password or other credentials to use in authenticating
     *        this username
     * @param algorithm Algorithm used to do the digest
     *
     * @return encypted password based on the algorithm.
     */
    public static String encodePassword(String password, String algorithm) {
        byte[] unencodedPassword = password.getBytes();

        MessageDigest md = null;

        try {
            // first create an instance, given the provider
            md = MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            log.error("Exception: " + e);

            return password;
        }

        md.reset();

        // call the update method one or more times
        // (useful when you don't know the size of your data, eg. stream)
        md.update(unencodedPassword);

        // now calculate the hash
        byte[] encodedPassword = md.digest();

        StringBuilder buf = new StringBuilder(encodedPassword.length << 1);

        for (int i = 0; i < encodedPassword.length; i++) {
            if ((encodedPassword[i] & 0xff) < 0x10) {
                buf.append("0");
            }

            buf.append(Integer.toString(encodedPassword[i] & 0xff, 16));
        }

        return buf.toString();
    }

    /**
     * Encode a string using Base64 encoding. Used when storing passwords
     * as cookies.
     *
     * This is weak encoding in that anyone can use the decodeString
     * routine to reverse the encoding.
     *
     * @param str
     * @return String
     */
    public static String encodeString(String str)  {
        sun.misc.BASE64Encoder encoder = new sun.misc.BASE64Encoder();
        return encoder.encodeBuffer(str.getBytes()).trim();
    }

    /**
     * Decode a string using Base64 encoding.
     *
     * @param str
     * @return String
     */
    public static String decodeString(String str) {
        sun.misc.BASE64Decoder dec = new sun.misc.BASE64Decoder();
        try {
            return new String(dec.decodeBuffer(str));
        } catch (IOException io) {
        	throw new RuntimeException(io.getMessage(), io.getCause());
        }
    }
    
    public static String[] split(String s, char delimiter) {
    	int broj = 0;
    	for(int i = s.length()-1; i >= 0; i--) {
    		if(s.charAt(i)==delimiter) broj++;
    	}
    	String[] res = new String[broj+1];
    	int pos; int curr=0;
    	for(int i = 0; i < broj; i++) {
    		pos = s.indexOf(delimiter, curr);
    		res[i] = s.substring(curr, pos);
    		curr = pos+1;
    	}
    	res[broj] = s.substring(curr);
    	return res;
    }
    
    /**
     * Metoda obavlja split predanog teksta koji se moze protezati kroz vise redaka. Pri tome se
     * kao delimiteri prepoznaju znak tab, te CR i LF. Vraceno polje sadrzi samo neprazne
     * elemente koji su trimani.
     * @param s
     * @return
     */
    public static String[] splitTabbedMultiline(String s) {
    	StringTokenizer stok = new StringTokenizer(s, "\r\n\t", false);
    	List<String> list = new ArrayList<String>();
    	while(stok.hasMoreTokens()) {
    		String token = stok.nextToken().trim();
    		if(token.isEmpty()) continue;
    		list.add(token);
    	}
    	String[] res = new String[list.size()];
    	list.toArray(res);
    	return res;
    }
    
    public static final Comparator<String> STRING_COMPARATOR = new Comparator<String>() {
	
		@Override
		public int compare(String o1, String o2) {
			if(o1==null) {
				if(o2==null) return 0;
				return -1;
			} else if(o2==null) {
				return 1;
			}
			return StringUtil.HR_COLLATOR.compare(o1, o2);
		}
	};
	
    /**
     * Uspoređuje dva korisnika najprije po prezimenu, potom po imenu, potom po jmbag-u.
     */
    public static final Comparator<User> USER_COMPARATOR = new Comparator<User>() {
		@Override
		public int compare(User o1, User o2) {
			int res;
			res = StringUtil.HR_COLLATOR.compare(o1.getLastName(), o2.getLastName());
			if(res != 0) return res;
			res = StringUtil.HR_COLLATOR.compare(o1.getFirstName(), o2.getFirstName());
			if(res != 0) return res;
			res = o1.getJmbag().compareTo(o2.getLastName());
			return 0;
		}
	};
	
    /**
     * Uspoređuje dva grupna događaja najprije po vremenu, pa po imenu.
     */
	public static final Comparator<GroupWideEvent> GROUP_WIDE_EVENT_COMPARATOR = new Comparator<GroupWideEvent>() {
	
		@Override
		public int compare(GroupWideEvent e1, GroupWideEvent e2) {
			int r = e1.getStart().compareTo(e2.getStart());
			if(r!=0) return r;
			if(e1.getRoom()==null) {
				if(e2.getRoom()!=null) return -1;
			} else {
				if(e2.getRoom()==null) return 1;
				r = e1.getRoom().getName().compareTo(e2.getRoom().getName());
				if(r!=0) return r;
			}
			return e1.getTitle().compareTo(e2.getTitle());
		}
	};
	
    /**
     * Uspoređuje dva korisnika najprije po prezimenu, potom po imenu, potom po jmbag-u i konačno
     * po grupi.
     */
    public static final Comparator<UserGroup> USER_GROUP_COMPARATOR1 = new Comparator<UserGroup>() {
		@Override
		public int compare(UserGroup o1, UserGroup o2) {
			int res = USER_COMPARATOR.compare(o1.getUser(), o2.getUser());
			if(res != 0) return res;
			return StringUtil.HR_COLLATOR.compare(o1.getGroup().getName(), o2.getGroup().getName());
		}
	};
    
    /**
     * Uspoređuje dva korisnika najprije po grupi, potom po prezimenu, potom po imenu, potom po jmbag-u.
     */
    public static final Comparator<UserGroup> USER_GROUP_COMPARATOR2 = new Comparator<UserGroup>() {
		@Override
		public int compare(UserGroup o1, UserGroup o2) {
			int res = StringUtil.HR_COLLATOR.compare(o1.getGroup().getName(), o2.getGroup().getName());
			if(res != 0) return res;
			return USER_COMPARATOR.compare(o1.getUser(), o2.getUser());
		}
	};
	
	/**
	 * Uspoređuje grupe po imenu.
	 */
	public static final Comparator<Group> GROUP_COMPARATOR = new Comparator<Group>() {
		@Override
		public int compare(Group o1, Group o2) {
			return StringUtil.HR_COLLATOR.compare(o1.getName(), o2.getName());
		}
	};
	
	/**
	 * Usporedba kolegija po imenu pa po isvu sifri.
	 */
	public static final Comparator<CourseInstance> COURSEINSTANCE_COMPARATOR = new Comparator<CourseInstance>() {
		@Override
		public int compare(CourseInstance o1, CourseInstance o2) {
			int res = StringUtil.HR_COLLATOR.compare(o1.getCourse().getName(),o2.getCourse().getName());
			if(res!=0) return res;
			return o1.getCourse().getIsvuCode().compareTo(o2.getCourse().getIsvuCode());
		}
	};

	/**
	 * Usporedba akademskih godina po godini pa po semestru.
	 */
	public static final Comparator<YearSemester> YEARSEMESTER_COMPARATOR = new Comparator<YearSemester>() {
		@Override
		public int compare(YearSemester o1, YearSemester o2) {
			int res = o1.getAcademicYear().compareTo(o2.getAcademicYear());
			if(res!=0) return res;
			// Ako je specijalni slučaj:
			if((o1.getSemester().equalsIgnoreCase("zimski")||o1.getSemester().equalsIgnoreCase("ljetni")) && (o2.getSemester().equalsIgnoreCase("zimski")||o2.getSemester().equalsIgnoreCase("ljetni"))) {
				// zimski je prije ljetnog pa reverziraj klasičnu usporedbu...
				return -o1.getSemester().compareToIgnoreCase(o2.getSemester());
			}
			// Inače ih usporedi abecedno
			return o1.getSemester().compareTo(o2.getSemester());
		}
	};

	/**
	 * Usporedba soba po imenu.
	 */
	public static final Comparator<AssessmentRoom> ASSESSMENTROOM_COMPARATOR = new Comparator<AssessmentRoom>() {
	
		@Override
		public int compare(AssessmentRoom o1, AssessmentRoom o2) {
			return StringUtil.HR_COLLATOR.compare(o1.getRoom().getName(), o2.getRoom().getName());
		}
	};
	
	/**
	 * Usporedba {@link AssessmentScore} objekata po pripadnim {@link User} objektima.
	 */
	public static final Comparator<AssessmentScore> ASSESSMENTSCORE_USER_COMPARATOR = new Comparator<AssessmentScore>() {
	
		@Override
		public int compare(AssessmentScore score1, AssessmentScore score2) {
			return USER_COMPARATOR.compare(score1.getUser(), score2.getUser());
		}
	};
	
	/**
	 * Uspoređuje dva stringa i vraća jesu li jednaki. Metoda je otporna na null reference, i štoviše, ako su oba stringa
	 * null, proglasit će se jednakima.
	 *  
	 * @param s1 prvi string
	 * @param s2 drugi string
	 * @return true ako su jednaki, false inače
	 */
	public static boolean stringEquals(String s1, String s2) {
		if(s1==null) {
			return s2==null;
		}
		if(s2==null) {
			return false;
		}
		return s1.equals(s2);
	}

	/**
	 * Uspoređuje dva stringa i vraća jesu li jednaki. Metoda je otporna na null reference, i štoviše, ako su oba stringa
	 * null, proglasit će se jednakima. Takoder, vrijednosti null i prazan string se tretiraju jednako!
	 *  
	 * @param s1 prvi string
	 * @param s2 drugi string
	 * @return true ako su jednaki, false inače
	 */
	public static boolean stringEqualsLoosly(String s1, String s2) {
		if(s1==null) {
			return s2==null || s2.isEmpty();
		}
		if(s2==null) {
			return s1.isEmpty();
		}
		return s1.equals(s2);
	}

	/**
	 * Uspoređuje dva stringa i vraća jesu li jednaki ne uzimajući u obzir razliku zbog velikih i malih slova. 
	 * Metoda je otporna na null reference, i štoviše, ako su oba stringa null, proglasit će se jednakima.
	 *  
	 * @param s1 prvi string
	 * @param s2 drugi string
	 * @return true ako su jednaki, false inače
	 */
	public static boolean stringEqualsIgnoreCase(String s1, String s2) {
		if(s1==null) {
			return s2==null;
		}
		if(s2==null) {
			return false;
		}
		return s1.equalsIgnoreCase(s2);
	}
	
	/**
	 * Pomoćna metoda koja pretvara string u Double. Ako je string null ili 
	 * prazan, vratit će vrijednost null. U suprotnom će pokušati pretvoriti
	 * tekst u broj. Metoda pokušava biti otporna na probleme decimalnih točaka
	 * i decimalnih zareza, pa ako konverzija ne uspije iz prve, pokušava
	 * zamijeniti problematični simbol te radi konverziju nanovo. 
	 * 
	 * @param value tekst koji treba pretvoriti u broj
	 * @return Double vrijednost teksta ili null
	 * @throws NumberFormatException u slučaju pogrešnog formata
	 */
	public static Double stringToDouble(String value) {
		if(value==null) return null;
		if(value.length()==0) return null;
		Double d;
		NumberFormatException nex = null;
		try {
			d = Double.valueOf(value);
			return d;
		} catch(NumberFormatException ex) {
			nex = ex;
		}
		// Ajmo vidjet zasto je puklo. Ako je problem u separatoru,
		// pokusajmo ga rijesiti:
		int pos = value.indexOf(',');
		if(pos==-1) {
			// Nema zareza; mozda je bila tocka a mi hocemo zarez?
			value = value.replaceAll("\\.", ",");
		} else {
			// Zamijeni zarez s točkom:
			value = value.replaceAll(",", ".");
		}
		try {
			d = Double.valueOf(value);
			return d;
		} catch(NumberFormatException ignorable) {
		}
		// Ako nismo uspjeli riješiti problem, izazovi originalnu iznimku.
		throw nex; 
	}

	public static boolean isStringBlank(String s) {
		if(s==null || s.trim().length()==0) return true;
		return false;
	}
	
	/**
	 * Provjerava je li text zapravo datum formata "yyyy-MM-dd".
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inaće
	 */
	public static boolean checkStandardDateFormat(String text) {
		if(text==null || text.length()!=10) return false;
		for(int i=0; i<10; i++) {
			switch(i) {
			case 4:
			case 7:
				if(text.charAt(i)!='-') return false;
				break;
			default:
				char c = text.charAt(i);
				if(c<'0' || c>'9') return false;
				break;
			}
		}
		return true;
	}

	/**
	 * Provjerava je li text zapravo datum i vrijeme formata "yyyy-MM-dd HH:mm:ss".
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inaće
	 */
	public static boolean checkStandardDateTimeFullFormat(String text) {
		if(text==null || text.length()!=19) return false;
		for(int i=0; i<19; i++) {
			switch(i) {
			case 10:
				if(text.charAt(i)!=' ') return false;
				break;
			case 13:
			case 16:
				if(text.charAt(i)!=':') return false;
				break;
			case 4:
			case 7:
				if(text.charAt(i)!='-') return false;
				break;
			default:
				char c = text.charAt(i);
				if(c<'0' || c>'9') return false;
				break;
			}
		}
		return true;
	}

	/**
	 * Provjerava je li text zapravo datum i vrijeme formata "yyyy-MM-dd HH:mm".
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inaće
	 */
	public static boolean checkStandardDateTimeShortFormat(String text) {
		if(text==null || text.length()!=16) return false;
		for(int i=0; i<16; i++) {
			switch(i) {
			case 10:
				if(text.charAt(i)!=' ') return false;
				break;
			case 13:
				if(text.charAt(i)!=':') return false;
				break;
			case 4:
			case 7:
				if(text.charAt(i)!='-') return false;
				break;
			default:
				char c = text.charAt(i);
				if(c<'0' || c>'9') return false;
				break;
			}
		}
		return true;
	}

	/**
	 * Provjerava je li text zapravo vrijeme formata "HH:mm:ss".
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inaće
	 */
	public static boolean checkTimeFullFormat(String text) {
		if(text==null || text.length()!=8) return false;
		for(int i=0; i<8; i++) {
			switch(i) {
			case 2:
			case 5:
				if(text.charAt(i)!=':') return false;
				break;
			default:
				char c = text.charAt(i);
				if(c<'0' || c>'9') return false;
				break;
			}
		}
		return true;
	}

	/**
	 * Provjerava je li text zapravo vrijeme formata "HH:mm:ss".
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inaće
	 */
	public static boolean checkTimeShortFormat(String text) {
		if(text==null || text.length()!=5) return false;
		for(int i=0; i<5; i++) {
			switch(i) {
			case 2:
				if(text.charAt(i)!=':') return false;
				break;
			default:
				char c = text.charAt(i);
				if(c<'0' || c>'9') return false;
				break;
			}
		}
		return true;
	}
	
	/**
	 * Pomoćna metoda koja null string pretvara u prazni string, a ostalo
	 * prenosi kako je dobila.
	 * 
	 * @param text tekst
	 * @return prazan string ako je ulaz null, inaće vraća ulaz
	 */
	public static String denullify(String text) {
		if(text==null) return "";
		return text;
	}
	
	/**
	 * Pomoćna metoda koja predano polje spaja u jedan string koristeći predani
	 * separator.
	 * 
	 * @param str
	 *            polje
	 * @param shouldReturnNull
	 *            ako je <code>true</code> a polje nema barem jedan element,
	 *            metoda će vratiti <code>null</code>; za <code>false</code> u
	 *            tom slučaju vraća prazan string.
	 * @return spojeno polje
	 */
	public static String joinToString(String[] str, boolean shouldReturnNull) {
		if (str == null || str.length == 0) {
			return shouldReturnNull ? null : "";
		}

		StringBuilder sb = new StringBuilder(25);

		for (int i = 0; i < str.length - 1; i++) {
			sb.append(str[i]).append('\t');
		}
		sb.append(str[str.length - 1]);

		return sb.toString();
	}

	/**
	 * Generira "sigurno" ime za datoteku. Kao smjernica je uzet tekst na:
	 * http://www.portfoliofaq.com/pfaq/FAQ00352.htm
	 * @param name
	 * @return
	 */
	public static String getSafeFileName(String name) {
		if(name==null || name.isEmpty()) return "X";
		char[] chars = name.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			switch(chars[i]) {
			case '*': chars[i] = '_'; break;
			case '"': chars[i] = '_'; break;
			case '\'': chars[i] = '_'; break;
			case '<': chars[i] = '_'; break;
			case '>': chars[i] = '_'; break;
			case '|': chars[i] = '_'; break;
			case '&': chars[i] = '_'; break;
			case '?': chars[i] = '_'; break;
			case '%': chars[i] = '_'; break;
			case '#': chars[i] = '_'; break;
			case '$': chars[i] = '_'; break;
			case ';': chars[i] = '_'; break;
			case ':': chars[i] = '_'; break;
			case ',': chars[i] = '_'; break;
			case '(': chars[i] = '_'; break;
			case ')': chars[i] = '_'; break;
			case '\\': chars[i] = '_'; break;
			case '/': chars[i] = '_'; break;
			case '~': chars[i] = '_'; break;
			case '+': chars[i] = '_'; break;
			case '{': chars[i] = '_'; break;
			case '}': chars[i] = '_'; break;
			case '[': chars[i] = '_'; break;
			case ']': chars[i] = '_'; break;
			}
		}
		if(chars[0]=='.') chars[0]='_';
		if(chars[chars.length-1]=='.') chars[chars.length-1]='_';
		return new String(chars);
	}

	/**
	 * Metoda deserijalizira tekstovni zapis i vraća odgovarajući objekt Properties.
	 * 
	 * @param text serijalizirani podatci; može biti i <code>null</code>
	 * @return objekt Properties
	 */
	public static Properties getPropertiesFromString(String text) {
		Properties p = new Properties();
		if(StringUtil.isStringBlank(text)) return p;
		try {
			p.load(new StringReader(text));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return p;
	}
	
	/**
	 * Serijalizira objekt Properties u tekst. Ako je prazan, vraća <code>null</code>.
	 * 
	 * @param p objekt Properties koji treba serijalizirati u tekst
	 * @return tekst ili <code>null</code>
	 */
	public static String getStringFromProperties(Properties p) {
		StringWriter sw = new StringWriter();
		try {
			p.store(sw, null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(p.isEmpty()) return null;
		return sw.toString();
	}

	/**
	 * Pomoćna metoda koja iz predanog objekta očitava string. Metoda dozvoljava da argument bude String, pa ga vraća,
	 * ili pak da argument bude polje stringova od barem jednog elementa (i u tom slučaju vraća prvi element).
	 * Ukoliko je string koji bi se vratio prazan, bit će vraćena <code>null</code> referenca.
	 * U svim ostalim slučajevima (kada argument nije String niti polje String-ova), vratit će se <code>null</code>).
	 * 
	 * @param object objekt
	 * @return string ili null
	 */
	public static String getString(Object object) {
		if(object instanceof String) {
			String s = (String)object;
			s = s.trim();
			if(s.isEmpty()) return null;
			return s;
		}
		if(object instanceof String[]) {
			String[] ss = (String[])object;
			if(ss.length<1) return null;
			String s = ss[0];
			s = s.trim();
			if(s.isEmpty()) return null;
			return s;
		}
		return null;
	}
	
	/**
	 * Pretvara polje stringova u polje objekata tipa Double.
	 * 
	 * @param strings polje stringova
	 * @return polje double-ova
	 */
	public static Double[] stringArrayToDoubleArray(String[] strings) {
		if (strings == null) {
			return null;
		}
		
		Double[] doubles = new Double[strings.length];
		
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = StringUtil.stringToDouble(strings[i]);
		}
		
		return doubles;
	}

	/**
	 * Pretvara polje stringova u polje objekata tipa Double, pri čemu polje stringova gleda tek od zadane pozicije.
	 * 
	 * @param strings polje stringova
	 * @param startIndex od koje pozicije?
	 * @return polje double-ova
	 */
	public static Double[] stringArrayToDoubleArray(String[] strings, int startIndex) {
		if (strings == null) {
			return null;
		}

		int len = strings.length - startIndex;
		if(len<1) return new Double[0];
		
		Double[] doubles = new Double[len];
		
		for (int i = 0; i < doubles.length; i++) {
			doubles[i] = StringUtil.stringToDouble(strings[i+startIndex]);
		}
		
		return doubles;
	}

}
