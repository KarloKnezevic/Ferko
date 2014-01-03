<%@ page contentType="text/csv; charset=UTF-8" pageEncoding="UTF-8" %> <%@ taglib prefix="s" uri="/struts-tags" %>Question ID, Question text, AnsweredPoll ID, Group ID, Group name, Answer text
<s:iterator value="data.answers"><s:property value="question.id" />,"<s:property value="question.questionText" />",<s:property value="answeredPoll.id" />,<s:property value="answeredPoll.group.id" />,"<s:property value="answeredPoll.group.name" />","<s:property value="answerText" />"
</s:iterator>
