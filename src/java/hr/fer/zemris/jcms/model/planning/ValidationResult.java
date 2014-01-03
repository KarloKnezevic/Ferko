package hr.fer.zemris.jcms.model.planning;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper for plan validation.
 * @author Ivan
 *
 */
public class ValidationResult {

	private static boolean success = true;
	private static List<String> messages = new ArrayList<String>();	
	
	public static void clear(){
		messages.clear();
		success = true;
	}
	
	public static void addMessage(String message){
		messages.add(message);
		success = false;
	}
	
	public static void addContextMessage(String message){
		messages.add(message);
	}
	
	public static List<String> getMessages(){
		return messages;
	}
	
	public static boolean isSuccess() {
		return success;
	}

}
