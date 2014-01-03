package hr.fer.zemris.jcms.model;

import hr.fer.zemris.jcms.parsers.TextService;

public class GradingStat {
	
	private int passed;
	private int failed;
	private double[] gradeTresholds;
	private int[] gradeCounts;
	
	public String serialize() {
		StringBuilder sb = new StringBuilder(1024);
		sb.append("1$");
		sb.append(passed);
		sb.append("$");
		sb.append(failed);
		sb.append("$");
		if(gradeTresholds!=null) {
			for(int i = 0; i < gradeTresholds.length; i++) {
				if(i>0) sb.append('#');
				sb.append(Double.doubleToRawLongBits(gradeTresholds[i]));
			}
		}
		sb.append("$");
		if(gradeCounts!=null) {
			for(int i = 0; i < gradeCounts.length; i++) {
				if(i>0) sb.append('#');
				sb.append(gradeCounts[i]);
			}
		}
		return sb.toString();
	}
	
	public static GradingStat deserialize(String text) {
		if(text==null || text.isEmpty()) return null;
		GradingStat gs = new GradingStat();
		String[] l1 = TextService.split(text, '$');
		if(l1[0].equals("1")) {
			gs.passed = Integer.parseInt(l1[1]);
			gs.failed = Integer.parseInt(l1[2]);
			if(!l1[3].isEmpty()) {
				String[] l2 = TextService.split(l1[3], '#');
				double[] el = new double[l2.length];
				for(int i = 0; i < l2.length; i++) {
					el[i] = Double.longBitsToDouble(Long.parseLong(l2[i]));
				}
				gs.gradeTresholds = el;
			}
			if(!l1[4].isEmpty()) {
				String[] l2 = TextService.split(l1[4], '#');
				int[] el = new int[l2.length];
				for(int i = 0; i < l2.length; i++) {
					el[i] = Integer.parseInt(l2[i]);
				}
				gs.gradeCounts = el;
			}
		}
		return gs;
	}
	
	public int getPassed() {
		return passed;
	}
	public void setPassed(int passed) {
		this.passed = passed;
	}
	public int getFailed() {
		return failed;
	}
	public void setFailed(int failed) {
		this.failed = failed;
	}
	public double[] getGradeTresholds() {
		return gradeTresholds;
	}
	public void setGradeTresholds(double[] gradeTresholds) {
		this.gradeTresholds = gradeTresholds;
	}
	public int[] getGradeCounts() {
		return gradeCounts;
	}
	public void setGradeCounts(int[] gradeCounts) {
		this.gradeCounts = gradeCounts;
	}
}
