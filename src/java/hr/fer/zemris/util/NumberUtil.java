package hr.fer.zemris.util;

/**
 * Pomocni razred koji cuva threadSafe metode za rad s brojevima, za razliku od nesigurnog DecimalFormatter-a.
 * 
 * @author marcupic
 *
 */
public class NumberUtil {

	private static final char[][] paddingsArray = new char[][] {
		{'0'},
		{'0','0'},
		{'0','0','0'},
		{'0','0','0','0'},
		{'0','0','0','0','0'},
		{'0','0','0','0','0','0'},
		{'0','0','0','0','0','0','0'}
	}; 

	/**
	 * Thread-safe metoda koja pretvara decimalni broj u string, zaokruzen na zadan broj decimala. Vazna napomena:
	 * ovo nece raditi za niz granicnih slucajeva, obzirom da se pretvorba radi prebacivanjem u long. Medutim, za
	 * "uobicajene" potrebe moze posluziti. Koristi se decimalna tocka.
	 * @param d broj koji pretvaramo
	 * @param decimals zeljeni broj decimala
	 * @return string koji predstavlja predani broj
	 */
	public static String simpleDoubleToString(double d, int decimals) {
		if(Double.isNaN(d)) return "NaN";
		if(Double.isInfinite(d)) return d<0 ? "-Inf" : "Inf";
		if(decimals<1) {
			if(d<0) {
				long val = -(long)(-d + 0.5);
				return Long.toString(val);
			} else {
				long val = (long)(d + 0.5);
				return Long.toString(val);
			}
		} else {
			int mul = 10;
			for(int i = 2; i <= decimals; i++) {
				mul = mul*10;
			}
			StringBuilder sb = new StringBuilder();
			long val;
			if(d<0) {
				val = -(long)(-d*mul + 0.5);
			} else {
				val = (long)(d*mul + 0.5);
			}
			sb.append(val);
			int toAdd = 1+decimals - sb.length();
			boolean negative = sb.charAt(0)=='-';
			if(negative) toAdd++;
			if(toAdd>0) {
				if(toAdd<=7) {
					sb.insert(negative ? 1 : 0, paddingsArray[toAdd-1]);
				} else {
					char[] c = new char[toAdd];
					for(int i = 0; i < c.length; i++) {
						c[i] = '0';
					}
					sb.insert(negative ? 1 : 0, c);
				}
			}
			sb.insert(sb.length()-decimals, '.');
			return sb.toString();
		}
	}

}
