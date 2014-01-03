package hr.fer.zemris.jcms.dao;

import java.util.List;
import hr.fer.zemris.jcms.model.planning.PlanDescriptor;
import hr.fer.zemris.jcms.model.planning.PlanningStorage;
import hr.fer.zemris.jcms.model.planning.ScheduleDescriptor;

import javax.persistence.EntityManager;

public interface PlanningDAO {

	public PlanDescriptor get(EntityManager em, Long planID);
	public ScheduleDescriptor getSchedule(EntityManager em, Long scheduleID);
	public List<PlanDescriptor> listPlans(EntityManager em, String courseInstanceID, Long userID);
	public void savePlan(EntityManager em, PlanDescriptor p);
	public List<ScheduleDescriptor> listSchedulesForPlan(EntityManager em, Long planID);
	public void saveSchedule(EntityManager em, ScheduleDescriptor s);
	
	public PlanningStorage getPlanningData(EntityManager em, Long storageID);
	public void savePlanningData(EntityManager em, PlanningStorage p);
	
	public void removePlan(EntityManager em, PlanDescriptor pd);
	public void removePlanningData(EntityManager em, PlanningStorage ps);
}
