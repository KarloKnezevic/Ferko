package hr.fer.zemris.jcms.dao.impl;

import java.util.ArrayList;
import java.util.List;
import hr.fer.zemris.jcms.dao.PlanningDAO;
import hr.fer.zemris.jcms.model.planning.PlanDescriptor;
import hr.fer.zemris.jcms.model.planning.PlanningStorage;
import hr.fer.zemris.jcms.model.planning.ScheduleDescriptor;

import javax.persistence.EntityManager;

public class PlanningDAOImpl implements PlanningDAO {

	@Override
	public PlanDescriptor get(EntityManager em, Long planID) {
		return em.find(PlanDescriptor.class, planID);
	} 
	
	@Override
	public ScheduleDescriptor getSchedule(EntityManager em, Long scheduleID){
		return em.find(ScheduleDescriptor.class, scheduleID);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<PlanDescriptor> listPlans(EntityManager em, String courseInstanceID, Long userID) {
		List<PlanDescriptor> plans = null;
		plans = (List<PlanDescriptor>)em.createNamedQuery("PlanDescriptor.listPlansForUserOnCourse")
		.setParameter("courseInstanceID", courseInstanceID)
		.setParameter("userID", userID)
		.getResultList();
		if(plans==null) return new ArrayList<PlanDescriptor>();
		else return plans;
	}

	@Override
	public void savePlan(EntityManager em, PlanDescriptor p) {
		em.persist(p);
	} 
	
	@Override
	public void savePlanningData(EntityManager em, PlanningStorage p) {
		em.persist(p);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ScheduleDescriptor> listSchedulesForPlan(EntityManager em, Long planID) {
		List<ScheduleDescriptor> schedules = null;
		schedules = (List<ScheduleDescriptor>)em.createNamedQuery("ScheduleDescriptor.listForPlan")
		.setParameter("planID", planID)
		.getResultList();
		return schedules;
	}

	@Override
	public void saveSchedule(EntityManager em, ScheduleDescriptor s) {
		em.persist(s);
	}

	@Override
	public PlanningStorage getPlanningData(EntityManager em, Long storageID) {
		return em.find(PlanningStorage.class, storageID);
	}

	@Override
	public void removePlan(EntityManager em, PlanDescriptor pd) {
		em.remove(pd);
	}

	@Override
	public void removePlanningData(EntityManager em, PlanningStorage ps) {
		em.remove(ps);
	}
	
}
