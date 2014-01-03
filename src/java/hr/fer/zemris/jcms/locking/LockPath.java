package hr.fer.zemris.jcms.locking;

import hr.fer.zemris.util.StringUtil;

/**
 * <p>Implementacija staze zaključavanja. Objekti ovog razreda su nepromjenjivi (immutable).</p>
 * <p>Opći oblik staze je <code>part1\part2\part3\...\partn</code>. Pri tome <code>part1</code>
 * mora biti jednak <code>ml</code> (kratica od master-lock).</p>
 * 
 * @author marcupic
 *
 */
public class LockPath {

	private String[] pathElements;
	
	/**
	 * Stvara novu stazu iz predanog teksta.
	 * 
	 * @param path staza
	 * @throws LockPathException u slučaju pogreške u formatu predane staze
	 */
	public LockPath(String path) throws LockPathException {
		if(path==null) {
			throw new LockPathException("Path for LockPath can not be null.");
		}
		if(StringUtil.isStringBlank(path)) {
			throw new LockPathException("Path for LockPath can not be empty.");
		}
		pathElements = path.split("\\\\");
		for(int i = 0; i < pathElements.length; i++) {
			pathElements[i] = pathElements[i].trim();
			if(pathElements[i].isEmpty()) {
				throw new LockPathException("Path for LockPath can not contain empty parts. Original path was: "+path);
			}
		}
		if(!pathElements[0].equals("ml")) {
			throw new LockPathException("Path for LockPath must have for root 'ml'. Original path was: "+path);
		}
	}
	
	public int size() {
		return pathElements.length;
	}
	
	public String getPart(int index) {
		return pathElements[index];
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(pathElements.length*10);
		sb.append(pathElements[0]);
		for(int i = 1; i < pathElements.length; i++) {
			sb.append('\\').append(pathElements[i]);
		}
		return sb.toString();
	}
}
