package hr.fer.zemris.util.scheduling.support;

public class SchedulingException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private boolean propagate = false;
	
	public SchedulingException() {
	}

	public SchedulingException(String message) {
		super(message);
	}
	
	public SchedulingException(String message, boolean propagate) {
		super(message);
		this.propagate=propagate;
	}

	public SchedulingException(Throwable cause) {
		super(cause);
	}

	public SchedulingException(String message, Throwable cause) {
		super(message, cause);
	}

	public boolean isPropagate() {
		return propagate;
	}

	public void setPropagate(boolean propagate) {
		this.propagate = propagate;
	}

	
}
