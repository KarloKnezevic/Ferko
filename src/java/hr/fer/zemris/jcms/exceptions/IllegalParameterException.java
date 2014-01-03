package hr.fer.zemris.jcms.exceptions;

public class IllegalParameterException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	
	public IllegalParameterException(){
		super();
	}
	
	public IllegalParameterException(String message){
		super(message);
	}

}
