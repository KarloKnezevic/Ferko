package hr.fer.zemris.jcms.beans.cached;

import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;

public class STEScore extends ScoreTableEntry {

	private static final long serialVersionUID = 1L;
	
	private Long assignerID;
	private double score;
	private double rawScore;
	private boolean error;
	private boolean present;
	private AssessmentStatus status;
	private short rank;
	private AssessmentStatus effectiveStatus;
	private double effectiveScore;
	private boolean effectivePresent;
	private short effectiveRank;
	
	public STEScore(Long id, boolean present, double score, double rawScore,
			AssessmentStatus status, short rank, Long assignerID, boolean error,
			AssessmentStatus effectiveStatus, double effectiveScore, boolean effectivePresent, short effectiveRank) {
		super(id);
		this.present = present;
		this.score = score;
		this.rawScore = rawScore;
		this.status = status;
		this.rank = rank;
		this.assignerID = assignerID;
		this.error = error;
		this.effectivePresent = effectivePresent;
		this.effectiveScore = effectiveScore;
		this.effectiveStatus = effectiveStatus;
		this.effectiveRank = effectiveRank;
	}
	
	public STEScore(AssessmentScore as) {
		super(as!=null ? as.getId() : null);
		if(as!=null) {
			this.present = as.getPresent();
			this.score = as.getScore();
			this.rawScore = as.getRawScore();
			this.status = as.getStatus();
			this.rank = as.getRank();
			this.assignerID = as.getAssigner()!=null ? as.getAssigner().getId() : null;
			this.error = as.isError();
			this.effectivePresent = as.getEffectivePresent();
			this.effectiveScore = as.getEffectiveScore();
			this.effectiveStatus = as.getEffectiveStatus();
			this.effectiveRank = as.getEffectiveRank();
		} else {
			this.present = false;
			this.score = 0;
			this.rawScore = 0;
			this.status = AssessmentStatus.FAILED;
			this.rank = 30000;
			this.assignerID = null;
			this.error = false;
			this.effectivePresent = false;
			this.effectiveScore = 0;
			this.effectiveStatus = AssessmentStatus.FAILED;
			this.effectiveRank = 30000;
		}
	}

	public Long getAssignerID() {
		return assignerID;
	}
	public String getScoreAsString() {
		return String.format("%1$.2f", score);
	}
	public String getEffectiveScoreAsString() {
		return String.format("%1$.2f", effectiveScore);
	}
	public double getScore() {
		return score;
	}
	public String getRawScoreAsString() {
		return String.format("%1$.2f", rawScore);
	}
	public double getRawScore() {
		return rawScore;
	}
	public boolean isError() {
		return error;
	}
	public boolean isPresent() {
		return present;
	}
	public AssessmentStatus getStatus() {
		return status;
	}
	public short getRank() {
		return rank;
	}

	public String getRankAsString() {
		if(rank>=30000) return "";
		return String.valueOf(rank);
	}
	
	public String getEffectiveRankAsString() {
		if(effectiveRank>=30000) return "";
		return String.valueOf(effectiveRank);
	}
	
	@Override
	public String toString() {
		if(error) return "*";
		if(!effectivePresent) return "";
		return effectiveScore + "|" + effectiveStatus + "("+(effectiveRank>=30000 ? "" : String.valueOf(effectiveRank))+")";
	}
	
	@Override
	public byte getType() {
		return (byte)1;
	}

	public AssessmentStatus getEffectiveStatus() {
		return effectiveStatus;
	}

	public double getEffectiveScore() {
		return effectiveScore;
	}

	public boolean getEffectivePresent() {
		return effectivePresent;
	}

	public short getEffectiveRank() {
		return effectiveRank;
	}

}
