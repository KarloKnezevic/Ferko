package hr.fer.zemris.jcms.web.actions.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DataResultMapping {
	boolean registerDelayedMessages() default false;
	String dataResult();
	String struts2Result();
}
