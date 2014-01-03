package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;

import hr.fer.zemris.jcms.service2.course.CourseService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.CourseInstanceImageData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=CourseInstanceImageData.class, defaultNavigBuilder=BuilderDefault.class)
public class CourseInstanceImage extends Ext2ActionSupport<CourseInstanceImageData> {

	private static final long serialVersionUID = 1L;
	
	@WebMethodInfo(loginCheck=false,
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=WRAPPED_STREAM,registerDelayedMessages=false),
			@DataResultMapping(dataResult=AbstractActionData.RESULT_FATAL,struts2Result=SHOW_FATAL_MESSAGE,registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=WRAPPED_STREAM, navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true)),
			@Struts2ResultMapping(struts2Result=SHOW_FATAL_MESSAGE, navigBuilder=BuilderDefault.class)
		}
	)
	public String execute() throws IOException {
		CourseService.getCourseInstanceImage(getEntityManager(), data);
		return null;
	}
	
}
