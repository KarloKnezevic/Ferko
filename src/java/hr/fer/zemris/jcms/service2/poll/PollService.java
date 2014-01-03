package hr.fer.zemris.jcms.service2.poll;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;

import hr.fer.zemris.jcms.beans.PollOptionBean;
import hr.fer.zemris.jcms.beans.PollQuestionBean;
import hr.fer.zemris.jcms.dao.CourseDAO;
import hr.fer.zemris.jcms.dao.CourseInstanceDAO;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.GroupDAO;
import hr.fer.zemris.jcms.dao.PollDAO;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.poll.Answer;
import hr.fer.zemris.jcms.model.poll.AnsweredPoll;
import hr.fer.zemris.jcms.model.poll.MultiChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.Option;
import hr.fer.zemris.jcms.model.poll.OptionAnswer;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.model.poll.PollUser;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.SingleChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.TextAnswer;
import hr.fer.zemris.jcms.model.poll.TextQuestion;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.PollBean;
import hr.fer.zemris.jcms.service.PollResults;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.PollForm;
import hr.fer.zemris.jcms.web.data.poll.CSVResultsData;
import hr.fer.zemris.jcms.web.data.poll.CoursePollData;
import hr.fer.zemris.jcms.web.data.poll.CoursePollOverviewData;
import hr.fer.zemris.jcms.web.data.poll.CoursePollResults;
import hr.fer.zemris.jcms.web.data.poll.PollAnswerData;
import hr.fer.zemris.jcms.web.data.poll.PollEditData;
import hr.fer.zemris.jcms.web.data.poll.PollIndexData;

public class PollService {

	public static void getIndexData(EntityManager em, PollIndexData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		CourseDAO courseDAO = DAOHelperFactory.getDAOHelper().getCourseDAO();
		List<PollBean> polls = new LinkedList<PollBean>();
		List<PollUser> pus = pollDAO.getUnanswerdPUsForUser(em, data.getCurrentUser().getId());
		// TODO: better solution?
		for(PollUser pu : pus) {
			PollBean bean = new PollBean(pu.getPoll());
			if(pu.getGroup()!=null) {
				StringBuilder sb = new StringBuilder();
				sb.append(pu.getPoll().getTitle()).append(" (");
				String isvuCode = (pu.getGroup().getCompositeCourseID().split("/"))[1];
				sb.append(courseDAO.get(em, isvuCode).getName()).append(", grupa ");
				sb.append(pu.getGroup().getName());
				sb.append(")");
				bean.setTitle(sb.toString());
			}
			bean.setPollUser(pu);
			polls.add(bean);
		}
		data.setUnansweredPolls(polls);
		
		//List<Poll> results = pollDAO.all(em);
		//data.setPollResults(results);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void getPollResults(EntityManager em, PollResults pr) {

		pr.setResult(PollResults.RESULT_SUCCESS);
	}
	
	public static void getCoursePollResults(EntityManager em, CoursePollResults pr) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, pr.getId());
		pr.setPoll(poll);
		
		IJCMSSecurityManager manager = JCMSSecurityManagerFactory.getManager();
		
		CourseInstance ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, pr.getCourseInstanceID());
		pr.setCourseInstance(ci);
		
		List<Group> pollGroups = pollDAO.getAllGroupsForPollOnCourse(em, ci, poll);
		List<Group> groupPollResultsGroups = manager.getGroupsForViewGroupPollResults(ci);
		List<Group> singlePollResultsGroups = manager.getGroupsForViewSinglePollResults(ci);
		singlePollResultsGroups.retainAll(pollGroups);
		
		pollGroups.retainAll(groupPollResultsGroups);
		List<Group> selectGroups = null;
		if(pollGroups.isEmpty()) {
			pr.setResult(CoursePollResults.RESULT_FATAL);
			return;
		}
		if(pr.getShowGroups()!=null) {
			Set<Long> set = new HashSet<Long>();
			selectGroups = new LinkedList<Group>();
			selectGroups.addAll(pollGroups);
			for(String s : pr.getShowGroups())	set.add(Long.valueOf(s));
			for(Iterator<Group> i = selectGroups.iterator(); i.hasNext();) {
				Long id = i.next().getId();
				if(!set.contains(id)) {
					i.remove();
				}
			}
			pr.setSelected(set);
		} else {
			selectGroups = pollGroups;
			String[] sg = new String[pollGroups.size()];
			Set<Long> set = new HashSet<Long>();
			for(int k=0; k<pollGroups.size(); k++) {
				sg[k] = String.valueOf(pollGroups.get(k).getId()); // potrebno?
				set.add(pollGroups.get(k).getId());
			}
			pr.setShowGroups(sg);
			pr.setSelected(set);
		}
		
		if(manager.canEditPoll(poll)) pr.getAdministrationPermissions().add("canEditPoll");
		if(manager.canProlongPoll(poll)) pr.getAdministrationPermissions().add("canProlongPoll");
		if(manager.canDeletePoll(poll)) pr.getAdministrationPermissions().add("canDeletePoll");
		
		AnsweredPoll ap = pollDAO.getFirstAnsweredPoll(em, poll, singlePollResultsGroups);
		if(ap!=null) {
			pr.setAnsweredPollId(ap.getId());
			pr.getAdministrationPermissions().add("canViewSingleResults");
		}
		
		List<TextAnswer> textAnswers;
		List<PollOptionBean> optionBeans;

		textAnswers = pollDAO.getAllTextAnswers(em, poll, selectGroups);
		optionBeans = pollDAO.countAllOptionAnswers(em, poll, selectGroups);
	
		Map<Question, PollQuestionBean> questionMap = new HashMap<Question, PollQuestionBean>();
		for(Question q : poll.getQuestions()) {
			questionMap.put(q, new PollQuestionBean(q)); // use JPQL for this?
		}
		for(PollOptionBean bean : optionBeans) {
			Question q = bean.getQuestion();
			if(!questionMap.containsKey(q)) continue;
			questionMap.get(q).addOptionBean(bean);
		}
		for(TextAnswer a : textAnswers) {
			Question q = a.getQuestion();
			if(!questionMap.containsKey(q)) continue;
			questionMap.get(a.getQuestion()).addTextAnswer(a);
		}
		
		// problem s agregacijom, nadopuni optione koji nedostaju:
		for(Question q : poll.getQuestions()) {
			if(q instanceof SingleChoiceQuestion || q instanceof MultiChoiceQuestion) {
				Set<Option> options;
				if(q instanceof SingleChoiceQuestion) {
					options = ((SingleChoiceQuestion) q).getOptions();
				} else {
					options = ((MultiChoiceQuestion) q).getOptions();
				}
				for(Option o : options) {
					PollQuestionBean bean = questionMap.get(q);
					if(!bean.getOptionAnswers().contains(o)) {
						bean.addOptionBean(new PollOptionBean(o, q, 0));
					}
				}

			}
		}
		// !
		List<PollQuestionBean> beans = new LinkedList<PollQuestionBean>(); 
		beans.addAll(questionMap.values());
		Collections.sort(beans);
		pr.setQuestions(beans);

		Collections.sort(pollGroups, new Comparator<Group> () {
			@Override
			public int compare(Group o1, Group o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		pr.setGroups(pollGroups);
		
		pr.setAnsweredPollsCounter(pollDAO.countAnsweredPolls(em, poll, selectGroups));

		pr.setResult(PollResults.RESULT_SUCCESS);
	}
	
	public static void getCSVPollResults(EntityManager em, CSVResultsData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		
		IJCMSSecurityManager manager = JCMSSecurityManagerFactory.getManager();
		
		CourseInstance ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, data.getCourseInstanceID());
	
		List<Group> pollGroups = pollDAO.getAllGroupsForPollOnCourse(em, ci, poll);
		List<Group> singlePollResultsGroups = manager.getGroupsForViewSinglePollResults(ci);
		singlePollResultsGroups.retainAll(pollGroups);
		
		if(singlePollResultsGroups.isEmpty()) {
			data.setResult(CSVResultsData.RESULT_FATAL);
			return;
		}
		
		List<Answer> answers = pollDAO.getAllAnswers(em, poll, singlePollResultsGroups);
		data.setAnswers(answers);
		
		data.setResult(CSVResultsData.RESULT_SUCCESS);
	}
	
	public static void getAnswerData(EntityManager em, PollAnswerData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		PollUser pu = pollDAO.getPollUser(em, data.getPollUserId());
		if(!pu.getUser().getId().equals(data.getCurrentUser().getId()) || pu.getAnswered()) {
			data.setResult(PollAnswerData.RESULT_FATAL);
			return;
		}
		Poll poll = pu.getPoll();
		CourseDAO courseDAO = DAOHelperFactory.getDAOHelper().getCourseDAO();
		StringBuilder sb = new StringBuilder();
		sb.append(pu.getPoll().getTitle()).append(" (");
		String isvuCode = (pu.getGroup().getCompositeCourseID().split("/"))[1];
		sb.append(courseDAO.get(em, isvuCode).getName()).append(", grupa ");
		sb.append(pu.getGroup().getName());
		sb.append(")");
		PollBean bean = new PollBean(poll);
		bean.setTitle(sb.toString());
		data.setPoll(bean);
		data.setPollForm(new PollForm(poll));
		data.setResult(PollAnswerData.RESULT_INPUT);
	}
	
	@SuppressWarnings("unchecked")
	private static void getQuestionsFromHttpRequest(HttpServletRequest request, Poll poll) {
		Map<Integer, String> questionType = new HashMap<Integer, String>();
		Map<Integer, String> questionText = new HashMap<Integer, String>();
		Map<Integer, String> questionOptions = new HashMap<Integer, String>();
		
    	String name = null;
    	String[] nameParts = null;
    	Enumeration<String> e = request.getParameterNames();
    	while(e.hasMoreElements()) {
    		name = (String)e.nextElement();
    		if(name.startsWith("question_")) {
    			nameParts = name.split("_");
    			if(nameParts[2].equals("text")) {
    				questionText.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    			if(nameParts[2].equals("type")) {
    				questionType.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    			if(nameParts[2].equals("options")) {
    				questionOptions.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    		}
    	}
    	
    	for(Integer i : questionType.keySet()) {
			String type = questionType.get(i);
			Question q = null;
			if(type.equals("bigText")) {
				q = new TextQuestion();
				q.setValidation("bigtext");
			}
			if(type.equals("multiChoice")) {
				q = new MultiChoiceQuestion();
				String[] options = questionOptions.get(i).split("\n");
				int br = 0;
				for(String option : options) {
					option = option.trim();
					if(option.length()==0) continue;
					Option o = new Option();
					o.setOrdinal(br);
					o.setText(option);
					o.setQuestion(q);
					((MultiChoiceQuestion)q).getOptions().add(o);
					br++;
				}
			}
			if(type.equals("singleChoice")) {
				q = new SingleChoiceQuestion();
				String[] options = questionOptions.get(i).split("\n");
				int br = 0;
				for(String option : options) {
					option = option.trim();
					if(option.length()==0) continue;
					Option o = new Option();
					o.setOrdinal(br);
					o.setText(option);
					o.setQuestion(q);
					((SingleChoiceQuestion)q).getOptions().add(o);
					br++;
				}
			}
			if(type.equals("rating")) {
				q = new SingleChoiceQuestion();
				for(int k=1; k<=5; k++) {
					Option o = new Option();
					o.setOrdinal(k);
					o.setText(String.valueOf(k));
					o.setQuestion(q);
					((SingleChoiceQuestion)q).getOptions().add(o);
				}
				q.setValidation("rating");
			}
			if(q == null) continue;
			q.setOrdinal(i);
			q.setQuestionText(questionText.get(i));
			q.setPoll(poll);
			poll.getQuestions().add(q);
		}
	}
	
	public static void createPoll(EntityManager em, HttpServletRequest request, PollEditData data) {
		CourseInstance ci = data.getCourseInstance();
		if(ci == null) {
			data.setResult(PollEditData.RESULT_FATAL);
			return;
		}
		IJCMSSecurityManager securityManager = JCMSSecurityManagerFactory.getManager();
		if(!securityManager.canCreatePoll(ci)) {
			data.setResult(PollEditData.RESULT_FATAL);
			return;
		}
		
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		
		if(request.getMethod().equals("GET")) {
			if(securityManager.isAdmin()) { // pravilnije bi bilo da pita za tagiranje
				data.setCanTag(true);
				data.setPollTags(pollDAO.getPollTags(em));
			}
			data.setResult(PollEditData.RESULT_INPUT);
			return;
		}
		
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
    	Date start = null;
		try {
			start = sdf.parse(data.getStartDate() + " " + data.getStartTime());
		} catch (ParseException e1) {
			data.getErrors().put("startDate", "Greška u formatu datuma ili vremena.");
		}
    	Date end = null;
		try {
			end = sdf.parse(data.getEndDate() + " " + data.getEndTime());
		} catch (ParseException e1) {
			data.getErrors().put("endDate", "Greška u formatu datuma ili vremena.");
		}
		
		if(data.getTitle().trim().length()==0) {
			data.getErrors().put("title", "Niste unjeli naslov.");
		}
		
		if(end != null && end.before(new Date())) {
			data.getErrors().put("endDate", "Provjerite datum!");
		}
		if(end != null && start != null && end.before(start)) {
			data.getErrors().put("endDate", "Provjerite datum!");
		}
		
		if(!data.getErrors().isEmpty()) {
			data.setJSONDescriptionOfQuestions(PollHelpers.getJSONQuestionsDescription(request));
			if(securityManager.isAdmin()) { 
				data.setCanTag(true);
				data.setPollTags(pollDAO.getPollTags(em));
			}
			data.setResult(PollEditData.RESULT_INPUT);
			return;
		}
    	
    	Poll poll = new Poll();

		poll.setDescription(data.getDescription());
		poll.setTitle(data.getTitle());
		poll.setViewablePublic(false);
		poll.setStartDate(start);
		poll.setEndDate(end);

		getQuestionsFromHttpRequest(request, poll);
		
		if(data.getPollTagId() != null && !data.getPollTagId().isEmpty() && securityManager.isAdmin()) {
			PollTag pollTag = pollDAO.getPollTag(em, Long.parseLong(data.getPollTagId()));
			poll.setPollTag(pollTag);
		}
		
		poll.getOwners().add(data.getCurrentUser());
		pollDAO.save(em, poll);
		data.setId(poll.getId());
		data.setResult(PollEditData.RESULT_SUCCESS);
		return;
	}
	
	public static void editPoll(EntityManager em, HttpServletRequest request, PollEditData data) {
		CourseInstance ci = data.getCourseInstance();
		if(ci == null) {
			data.setResult(PollEditData.RESULT_FATAL);
			return;
		}
		IJCMSSecurityManager securityManager = JCMSSecurityManagerFactory.getManager();
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		if(!securityManager.canEditPoll(poll)) {
			data.setResult(PollEditData.RESULT_FATAL);
			return;
		}
		
		if(request.getMethod().equals("GET")) {
			if(securityManager.isAdmin()) {
				data.setCanTag(true);
				data.setPollTags(pollDAO.getPollTags(em));
			}
			data.setTitle(poll.getTitle());
			data.setDescription(poll.getDescription());
			if(poll.getPollTag()!=null) data.setPollTagId(String.valueOf(poll.getPollTag().getId()));
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfTime = new SimpleDateFormat("H:m");
			data.setStartDate(sdfDate.format(poll.getStartDate()));
			data.setStartTime(sdfTime.format(poll.getStartDate()));
			data.setEndDate(sdfDate.format(poll.getEndDate()));
			data.setEndTime(sdfTime.format(poll.getEndDate()));
			data.setJSONDescriptionOfQuestions(PollHelpers.getJSONQuestionsDescription(poll.getQuestions()));
			data.setResult(PollEditData.RESULT_INPUT);
			return;
		}
		
	   	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
    	Date start = null;
		try {
			start = sdf.parse(data.getStartDate() + " " + data.getStartTime());
		} catch (ParseException e1) {
			data.getErrors().put("startDate", "Greška u formatu datuma ili vremena.");
		}
    	Date end = null;
		try {
			end = sdf.parse(data.getEndDate() + " " + data.getEndTime());
		} catch (ParseException e1) {
			data.getErrors().put("endDate", "Greška u formatu datuma ili vremena.");
		}
		
		if(data.getTitle().trim().length()==0) {
			data.getErrors().put("title", "Niste unjeli naslov.");
		}
		
		if(end != null && end.before(new Date())) {
			data.getErrors().put("endDate", "Provjerite datum!");
		}
		if(end != null && start != null && end.before(start)) {
			data.getErrors().put("endDate", "Provjerite datum!");
		}
		
		if(!data.getErrors().isEmpty()) {
			data.setJSONDescriptionOfQuestions(PollHelpers.getJSONQuestionsDescription(request));
			if(securityManager.isAdmin()) { 
				data.setCanTag(true);
				data.setPollTags(pollDAO.getPollTags(em));
			}
			data.setResult(PollEditData.RESULT_INPUT);
			return;
		}
		
		pollDAO.removeAllQuestions(em, poll);
		poll.getQuestions().clear();
		
		poll.setDescription(data.getDescription());
		poll.setTitle(data.getTitle());
		poll.setViewablePublic(false);
		poll.setStartDate(start);
		poll.setEndDate(end);

		getQuestionsFromHttpRequest(request, poll);
		
		if(data.getPollTagId() != null && !data.getPollTagId().isEmpty() && securityManager.isAdmin()) {
			PollTag pollTag = pollDAO.getPollTag(em, Long.parseLong(data.getPollTagId()));
			poll.setPollTag(pollTag);
		}
		
		pollDAO.save(em, poll);
		data.setId(poll.getId());
		data.setResult(PollEditData.RESULT_SUCCESS);
		return;
	}
	
	public static void answerPoll(EntityManager em, PollAnswerData data, HttpServletRequest request) {
		// TODO: prvo provjeri jesu odogovori valid!
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		PollUser pu = pollDAO.getPollUser(em, data.getPollUserId());
		if(!pu.getUser().getId().equals(data.getCurrentUser().getId()) || pu.getAnswered()) {
			data.setResult(PollAnswerData.RESULT_FATAL);
			return;
		}
		Poll poll = pu.getPoll();
		AnsweredPoll ap = new AnsweredPoll();
		ap.setPoll(poll);
		ap.setGroup(pu.getGroup());
		pollDAO.saveAnsweredPoll(em, ap);
		Set<Question> questions = poll.getQuestions();
		for(Question question : questions) {
			if(question instanceof TextQuestion) {
				if(request.getParameter("question_"+question.getId()).length()==0) continue;
				TextAnswer answer = new TextAnswer(request.getParameter("question_"+question.getId()));
				answer.setQuestion((TextQuestion)question);
				answer.setAnsweredPoll(ap);
				pollDAO.saveAnswer(em, answer);
			}
			if(question instanceof SingleChoiceQuestion) {
				Set<Option> options = ((SingleChoiceQuestion)question).getOptions();
				Map<Long, Option> optionsMap = new HashMap<Long, Option>();
				for(Option option : options) optionsMap.put(option.getId(), option);
				String value = request.getParameter("question_"+question.getId());
				if(value==null) continue;
				Long oid = Long.parseLong(value);
				Option option = null;
				if(!optionsMap.containsKey(oid)) {
					continue;
				} else {
					option = optionsMap.get(oid);
				}
				OptionAnswer answer = new OptionAnswer();
				answer.setQuestion((SingleChoiceQuestion)question);
				answer.setAnsweredPoll(ap);
				((OptionAnswer)answer).setOption(option);
				pollDAO.saveAnswer(em, answer);
			}
			if(question instanceof MultiChoiceQuestion) {
				Set<Option> options = ((MultiChoiceQuestion)question).getOptions();
				Map<Long, Option> optionsMap = new HashMap<Long, Option>();
				for(Option option : options) optionsMap.put(option.getId(), option);
				String[] values = request.getParameterValues("question_"+question.getId());
				if(values==null) continue;
				for(String value : values) {
					Long oid = Long.parseLong(value);
					Option option = null;
					if(!optionsMap.containsKey(oid)) {
						continue;
					} else {
						option = optionsMap.get(oid);
					}
					OptionAnswer answer = new OptionAnswer();
					answer.setQuestion((MultiChoiceQuestion)question);
					answer.setAnsweredPoll(ap);
					((OptionAnswer)answer).setOption(option);
					pollDAO.saveAnswer(em, answer);
				}
			}
		}
		pu.setAnswered(true);
		pollDAO.savePollUser(em, pu);
		data.setResult(PollAnswerData.RESULT_SUCCESS);
	}
	
	public static void removePoll(EntityManager em, PollEditData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		pollDAO.remove(em, poll);
		data.setResult(PollEditData.RESULT_SUCCESS);
	}

	public static void addUsers(EntityManager em, HttpServletRequest request) {
		String users = request.getParameter("users");
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, Long.parseLong(request.getParameter("id")));
		String[] users_array = users.split(",");
		for(String user : users_array) {
			try {
				if(user.length()<=1) continue;
				PollUser pu = new PollUser();
				String[] user_apart = user.split("/");
				pu.setPoll(poll);
				User u = dh.getUserDAO().getUserById(em, Long.valueOf(user_apart[0]));
				pu.setUser(u);
				Group g = dh.getGroupDAO().get(em, Long.valueOf(user_apart[1]));
				pu.setGroup(g);
				pu.setAnswered(false);
				pollDAO.savePollUser(em, pu);
			} catch(NumberFormatException e) {
				// TODO
			}
		}
	}

	public static void getSinglePollResults(EntityManager em,
			CoursePollResults pr) {
		// TODO: ovo bi trebalo razdvojiti od pregleda grupnih rezultata i napraviti
		// posebnu logiku radi boljih performansi
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		AnsweredPoll ap = pollDAO.getAnsweredPoll(em, pr.getAnsweredPollId());
		
		IJCMSSecurityManager manager = JCMSSecurityManagerFactory.getManager();
		
		CourseInstance ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, pr.getCourseInstanceID());
		pr.setCourseInstance(ci);
		
		List<Group> singlePollResultsGroups = manager.getGroupsForViewSinglePollResults(ci);
		if(!singlePollResultsGroups.contains(ap.getGroup())) {
			pr.setResult(CoursePollResults.RESULT_FATAL);
			return;
		}
		
		Poll poll = ap.getPoll();
		pr.setPoll(poll);
		List<TextAnswer> textAnswers;
		List<PollOptionBean> optionBeans;
		textAnswers = pollDAO.getAllTextAnswers(em, poll.getId(), ap);
		optionBeans = pollDAO.countAllOptionAnswers(em, poll.getId(), ap);
		Map<Question, PollQuestionBean> questionMap = new HashMap<Question, PollQuestionBean>();
		for(Question q : poll.getQuestions()) {
			questionMap.put(q, new PollQuestionBean(q));
		}
		for(PollOptionBean bean : optionBeans) {
			Question q = bean.getQuestion();
			if(!questionMap.containsKey(q)) continue;
			questionMap.get(q).addOptionBean(bean);
		}
		for(TextAnswer a : textAnswers) {
			Question q = a.getQuestion();
			if(!questionMap.containsKey(q)) continue;
			questionMap.get(a.getQuestion()).addTextAnswer(a);
		}
		List<PollQuestionBean> beans = new LinkedList<PollQuestionBean>(); 
		beans.addAll(questionMap.values());
		Collections.sort(beans);
		pr.setQuestions(beans);
		pr.setAnsweredPollNeighbours(pollDAO.getAnsweredPollNeighbours(em, ap, singlePollResultsGroups));
		pr.setResult(PollResults.RESULT_SUCCESS);
	}

	public static void addGroups(EntityManager entityManager,
			HttpServletRequest request) {
		// TODO Auto-generated method stub
		
	}

	public static void getCoursePollData(EntityManager em,
			CoursePollData data) {
		IJCMSSecurityManager manager = JCMSSecurityManagerFactory.getManager();
		CourseInstanceDAO ciDAO = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO();
		CourseInstance ci= ciDAO.get(em, data.getCourseInstanceId());
		if(!manager.canCreatePoll(ci)) {
			data.setResult(CoursePollData.RESULT_FATAL);
			return;
		}
		GroupDAO groupDAO = DAOHelperFactory.getDAOHelper().getGroupDAO();
		List<Group> lectureGroups = groupDAO.findLectureSubgroups(em, data.getCourseInstanceId());
		List<Group> labGroups = groupDAO.findSubgroups(em, data.getCourseInstanceId(), "1/%");
		List<Group> privateGroups = groupDAO.findSubgroups(em, data.getCourseInstanceId(), "6/%");
		List<Group> canAssignGroups = manager.getGroupsOnCourseWithPollAssignPermission(ci);
		lectureGroups.retainAll(canAssignGroups);
		labGroups.retainAll(canAssignGroups);
		privateGroups.retainAll(canAssignGroups);
		Comparator<Group> cmp = new Comparator<Group>() {
			public int compare(Group arg0, Group arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		};
		Collections.sort(lectureGroups, cmp);
		Collections.sort(labGroups, cmp);
		Collections.sort(privateGroups, cmp);
		data.setCourseInstance(ci);
		data.setLectureGroups(lectureGroups);
		data.setPrivateGroups(privateGroups);
		data.setLabGroups(labGroups);
		data.setResult(CoursePollData.RESULT_INPUT);
	}

	public static void addGroups(EntityManager em,
			CoursePollData data) {
		IJCMSSecurityManager manager = JCMSSecurityManagerFactory.getManager();
		CourseInstanceDAO ciDAO = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO();
		CourseInstance ci= ciDAO.get(em, data.getCourseInstanceId());
		if(!manager.canCreatePoll(ci)) {
			data.setResult(CoursePollData.RESULT_FATAL);
			return;
		}
		List<Group> canAssignGroups = manager.getGroupsOnCourseWithPollAssignPermission(ci);
		
		GroupDAO groupDAO = DAOHelperFactory.getDAOHelper().getGroupDAO();
		
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		for(String groupId : data.getSelectedGroups()) {
			Group group = groupDAO.get(em, Long.valueOf(groupId));
			if(!canAssignGroups.contains(group)) continue;
			Set<UserGroup> users = group.getUsers();
			for(UserGroup u : users) {
				PollUser pu = new PollUser();
				pu.setGroup(group);
				pu.setUser(u.getUser());
				pu.setPoll(poll);
				pollDAO.savePollUser(em, pu);
			}
		}
		data.setResult(CoursePollData.RESULT_SUCCESS);
	}
	
	public static void getCoursePollOverviewData(EntityManager em, CoursePollOverviewData data) {
		CourseInstanceDAO ciDAO = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO();
		CourseInstance ci = ciDAO.get(em, data.getCourseInstanceID());
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		List<Poll> polls;
		if(JCMSSecurityManagerFactory.getManager().isAdmin()){
			polls = pollDAO.getAllPollsOnCourse(em, ci);
		} else {
			Set<String> roles = JCMSSecurityManagerFactory.getManager().getRolesOnCourse(ci);
			polls = pollDAO.getAllPollsForView(em, data.getCurrentUser(), ci, roles);
		}
		Collections.sort(polls, new Comparator<Poll>() {
			public int compare(Poll arg0, Poll arg1) {
				return arg1.getStartDate().compareTo(arg0.getStartDate());
			}
		});
		data.setPolls(polls);
		data.setCourseInstance(ci);
		data.setResult(CoursePollOverviewData.RESULT_SUCCESS);
	}

	public static void getUpdateData(EntityManager em,
			PollEditData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		/*if(!JCMSSecurityManagerFactory.getManager().canEditPoll(poll)) {
			return;
		}*/
		data.setTitle(poll.getTitle());
		data.setDescription(poll.getDescription());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
		data.setEndDate(dateFormat.format(poll.getEndDate()));
		data.setStartDate(dateFormat.format(poll.getStartDate()));
		data.setStartTime(timeFormat.format(poll.getStartDate()));
		data.setEndTime(timeFormat.format(poll.getEndDate()));
		data.setJSONDescriptionOfQuestions(PollHelpers.getJSONQuestionsDescription(poll.getQuestions()));
	}

	public static void prolongPoll(EntityManager em,
			PollEditData data) {
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		Poll poll = pollDAO.getPoll(em, data.getId());
		if(!JCMSSecurityManagerFactory.getManager().canProlongPoll(poll) || poll == null) {
			data.setResult(PollEditData.RESULT_FATAL);
			return;
		}
		if(data.getEndDate()==null && data.getEndTime()==null) {
			data.setResult(PollEditData.RESULT_INPUT);
			SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdfTime = new SimpleDateFormat("H:m");
			data.setEndDate(sdfDate.format(poll.getEndDate()));
			data.setEndTime(sdfTime.format(poll.getEndDate()));
			return;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd H:m");
		Date end = null;
		try {
			end = sdf.parse(data.getEndDate() + " " + data.getEndTime());
		} catch (ParseException e1) {
			data.getErrors().put("endDate", "Greška u formatu datuma ili vremena.");
		}
		if(!data.getErrors().isEmpty()) {
			data.setResult(PollEditData.RESULT_INPUT);
			return;
		}
		poll.setEndDate(end);
		pollDAO.save(em, poll);
		data.setResult(PollEditData.RESULT_SUCCESS);
	}
}
