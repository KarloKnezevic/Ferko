package hr.fer.zemris.jcms.periodicals;

/**
 * Opisnik jedne periodičke usluge.
 * 
 * @author marcupic
 */
public class PeriodicalServiceDescriptor {

	private String className;
	private IPeriodicalService service;
	private long lastActivatedOn;
	private boolean inCall;
	private long[] callPoints;
	private long nextActivationAt;

	/**
	 * Konstruktor.
	 * 
	 * @param className naziv razreda kojim je usluga ostvarena
	 * @param service primjerak usluge
	 * @param callPoints vremenski trenutci u kojima treba pokretati uslugu
	 */
	public PeriodicalServiceDescriptor(String className, IPeriodicalService service, long[] callPoints) {
		this.className = className;
		this.service = service;
		this.callPoints = callPoints;
	}

	/**
	 * Kojim je razredom usluga ostvarena?
	 * 
	 * @return naziv razreda
	 */
	public String getClassName() {
		return className;
	}

	/**
	 * Objekt koji predstavlja primjerak usluge.
	 * 
	 * @return primjerak usluge
	 */
	public IPeriodicalService getService() {
		return service;
	}

	/**
	 * Trenutak kada je usluga zadnji puta pokrenuta.
	 * 
	 * @return trenutak zadnjeg pokretanja
	 */
	public long getLastActivatedOn() {
		return lastActivatedOn;
	}

	/**
	 * Trenutak kada je usluga zadnji puta pokrenuta.
	 * 
	 * @param lastActivatedOn trenutak zadnjeg pokretanja
	 */
	public void setLastActivatedOn(long lastActivatedOn) {
		this.lastActivatedOn = lastActivatedOn;
	}

	/**
	 * Je li usluga upravo u izvođenju? Koristi se kako
	 * bi se spriječilo višestruko pokretanje usluge u slučaju
	 * dugačkog izvođenja.
	 * 
	 * @return true ako je, false ako nije
	 */
	public boolean isInCall() {
		return inCall;
	}

	/**
	 * Postavljanje statusa izvođenja usluge.
	 * 
	 * @param inCall true ako je izvođenje pokrenuto, false inače
	 */
	public void setInCall(boolean inCall) {
		this.inCall = inCall;
	}

	/**
	 * Jedan CallPoint predstavlja offset u milisekundama
	 * od ponoći, koji govori kada treba pokrenuti ovu uslugu.
	 * 
	 * @return polje call-point-a
	 */
	public long[] getCallPoints() {
		return callPoints;
	}

	/**
	 * Kada je zakazano sljedeće pokretanje?
	 * 
	 * @return vrijeme sljedećeg zakazanog pokretanja
	 */
	public long getNextActivationAt() {
		return nextActivationAt;
	}

	/**
	 * Postavlja vrijeme sljedećeg pokretanja.
	 * 
	 * @param nextActivationAt vrijeme sljedećeg pokretanja
	 */
	public void setNextActivationAt(long nextActivationAt) {
		this.nextActivationAt = nextActivationAt;
	}
}
