package hr.fer.zemris.jcms.service2.course.parameters;

import java.io.BufferedInputStream;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.beans.CIP1RoomParams;
import hr.fer.zemris.jcms.beans.CIP1TermDuration;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.AssessmentTag;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.model.Room;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseParameters1Data;
import hr.fer.zemris.jcms.web.actions.data.CourseParameters2Data;
import hr.fer.zemris.jcms.web.actions.data.CourseParametersListData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

public class CourseParametersService {

	private static final Logger logger = Logger.getLogger(CourseParametersService.class.getCanonicalName());
	
	private static class CIP1 {
		List<CIP1RoomParams> rooms;
		List<CIP1TermDuration> terms;
	}
	
	public static class ParameterAttributes {
		boolean modifiable;
		boolean visible;
		Date modifiableUntil;
		CourseInstanceKeyValue keyValue;
		
		public ParameterAttributes() {
		}

		public CourseInstanceKeyValue getKeyValue() {
			return keyValue;
		}
		
		public void setKeyValue(CourseInstanceKeyValue keyValue) {
			this.keyValue = keyValue;
		}
		
		public boolean isModifiable() {
			return modifiable;
		}

		public void setModifiable(boolean modifiable) {
			this.modifiable = modifiable;
		}

		public boolean isVisible() {
			return visible;
		}

		public void setVisible(boolean visible) {
			this.visible = visible;
		}

		public Date getModifiableUntil() {
			return modifiableUntil;
		}

		public void setModifiableUntil(Date modifiableUntil) {
			this.modifiableUntil = modifiableUntil;
		}
	}
	
	public static void showCourseParametersList(EntityManager em, CourseParametersListData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ParameterAttributes miSchedPA = getMIScheduleParameterAttributes(em, data.getCourseInstance());
		data.setMiSched(miSchedPA);
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		
	}

	public static void showCourseParameters1(EntityManager em, CourseParameters1Data data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ParameterAttributes miSchedPA = getMIScheduleParameterAttributes(em, data.getCourseInstance());
		data.setMiSched(miSchedPA);
		if(!miSchedPA.isVisible()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		CourseInstanceKeyValue cikv = miSchedPA.getKeyValue();
		CIP1 cip1;
		if(cikv!=null && !StringUtil.isStringBlank(cikv.getValue())) {
			cip1 = deserialize(cikv.getValue());
			if(miSchedPA.isModifiable()) {
				consolidate(em, cip1);
			}
		} else {
			cip1 = buildDefault(em, data.getCourseInstance());
			sort(cip1);
			String ser = serialize(cip1);
			if(cikv!=null) {
				cikv.setValue(ser);
			} else {
				cikv = new CourseInstanceKeyValue(data.getCourseInstance(), "paramMISched", ser);
				DAOHelperFactory.getDAOHelper().getCourseInstanceKeyValueDAO().save(em, cikv);
			}
		}
		
		int ukupnoAsistenata = 0;
		int predvidenoAsistenata = 0;
		int ukupnoStudenata = 0;
		int predvidenoStudenata = 0;
		List<String> zeroRooms = new ArrayList<String>();
		for(CIP1RoomParams r : cip1.rooms) {
			ukupnoAsistenata += r.getAssistants();
			predvidenoAsistenata += r.getDefaultAsistants();
			ukupnoStudenata += r.getStudents();
			predvidenoStudenata += r.getDefaultStudents();
			if(r.getStudents()==0) {
				zeroRooms.add(r.getRoomName());
			}
		}

		if(!zeroRooms.isEmpty()) {
			StringBuilder sb = new StringBuilder(200);
			if(zeroRooms.size()==1) {
				sb.append("Kapacitet dvorane ").append(zeroRooms.get(0)).append(" je postavljen na nula. Je li to pogreška?");
			} else {
				sb.append("Kapaciteti dvorana ");
				for(int i = 0; i < zeroRooms.size(); i++) {
					if(i>0) {
						if(i==zeroRooms.size()-1) {
							sb.append(" i ");
						} else {
							sb.append(", ");
						}
					}
					sb.append(zeroRooms.get(i));
				}
				sb.append(" su postavljeni na nula. Je li to pogreška?");
			}
			data.getMessageLogger().addWarningMessage(sb.toString());
		}
		
		if(ukupnoAsistenata>(int)(predvidenoAsistenata*1.25)) {
			data.getMessageLogger().addWarningMessage("Broj traženih asistenata bitno je veći od uobičajenog. Je li to pogreška?");
		}
		if(ukupnoStudenata<(int)(predvidenoStudenata*0.75)) {
			data.getMessageLogger().addWarningMessage("Kapaciteti dvorana bitno su smanjeni u odnosu na uobičajene. Je li to pogreška?");
		}
		
		data.setRooms(cip1.rooms);
		data.setTerms(cip1.terms);
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		
	}

	public static void updateCourseParameters1(EntityManager em, CourseParameters1Data data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		ParameterAttributes miSchedPA = getMIScheduleParameterAttributes(em, data.getCourseInstance());
		data.setMiSched(miSchedPA);
		if(!miSchedPA.isVisible() || !miSchedPA.isModifiable()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		CourseInstanceKeyValue cikv = miSchedPA.getKeyValue();
		CIP1 cip1;
		if(cikv!=null && !StringUtil.isStringBlank(cikv.getValue())) {
			cip1 = deserialize(cikv.getValue());
			if(miSchedPA.isModifiable()) {
				consolidate(em, cip1);
			}
		} else {
			cip1 = buildDefault(em, data.getCourseInstance());
			sort(cip1);
			String ser = serialize(cip1);
			if(cikv!=null) {
				cikv.setValue(ser);
			} else {
				cikv = new CourseInstanceKeyValue(data.getCourseInstance(), "paramMISched", ser);
				DAOHelperFactory.getDAOHelper().getCourseInstanceKeyValueDAO().save(em, cikv);
			}
		}

		// Usporedi ono sto je popunjeno i ono sto smo mi pripremili:
		boolean uspjeh = provjeriIUsporedi(data.getMessageLogger(), cip1, data.getTerms(), data.getRooms());
		if(!uspjeh) {
			data.getMessageLogger().addInfoMessage("Ništa nije pohranjeno u bazu. Molim revidirati parametre.");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		String ser = serialize(cip1);
		cikv.setValue(ser);
		
		data.setRooms(cip1.rooms);
		data.setTerms(cip1.terms);
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void showCourseParameters2(EntityManager em, CourseParameters2Data data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "wikiEnabled");
		data.setWikiEnabled(cikv!=null && cikv.getValue()!=null && cikv.getValue().equals("1"));
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		
	}

	public static void updateCourseParameters2(EntityManager em, CourseParameters2Data data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "wikiEnabled");
		boolean current = cikv!=null && cikv.getValue()!=null && cikv.getValue().equals("1");
		if(data.isWikiEnabled()!=current) {
			if(cikv==null) {
				cikv = new CourseInstanceKeyValue(data.getCourseInstance(), "wikiEnabled", current ? "0" : "1");
				dh.getCourseInstanceKeyValueDAO().save(em, cikv);
			} else {
				cikv.setValue(current ? "0" : "1");
			}
		}
		
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		
	}

	public static void exportRoomParameters1(EntityManager em, CourseParameters1Data data) {

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().isAdmin();
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		String requestedSemesterID = data.getYearSemesterID();
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "miScheduleParam"); // Vrijednost mora biti oznaka semestra! Npr. 2009Z
			if(kv!=null && kv.getValue()!=null && !kv.getValue().isEmpty()) {
				requestedSemesterID = kv.getValue();
			}
		}
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			requestedSemesterID = BasicServiceSupport.getCurrentSemesterID(em);
		}
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			data.getMessageLogger().addErrorMessage("Nije definiran željeni semestar.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, requestedSemesterID);
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Traženi semestar ne postoji.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<CourseInstance> ciList = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForSemester(em, ys.getId());

		Map<String,Integer> defDurations = loadDefaultDurations(ys.getId(), null);
		List<AssessmentTag> tags = resolveTags(em);
		List<Room> rooms = resolveAcceptableRooms(em);

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Room> allRooms = dh.getRoomDAO().list(em);
		Map<String, Room> roomsMap = new HashMap<String, Room>(allRooms.size()*2);
		for(Room r : allRooms) {
			roomsMap.put(r.getId(), r);
		}
		
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("SCH", null);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmpFile)), "UTF-8"));
				for(CourseInstance courseInstance : ciList) {
					CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, courseInstance, "paramMISched");
					CIP1 cip1;
					if(cikv!=null && !StringUtil.isStringBlank(cikv.getValue())) {
						cip1 = deserialize(cikv.getValue());
					} else {
						cip1 = buildDefault(em, courseInstance, defDurations, tags, rooms);
					}
					bw.write(courseInstance.getCourse().getIsvuCode());
					for(CIP1RoomParams r : cip1.rooms) {
						bw.write("\t");
						Room ro = roomsMap.get(r.getRoomId());
						if(ro==null) {
							bw.write("error/error");
						} else {
							bw.write(ro.getVenue().getShortName());
							bw.write("/");
							bw.write(ro.getShortName());
						}
						bw.write("\t");
						bw.write(Integer.toString(r.getStudents()));
						bw.write("\t");
						bw.write(Integer.toString(r.getAssistants()));
					}
					bw.write("\n");
				}
				bw.flush();
			} catch(Exception ex) {
				tmpFile.delete();
				data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			} finally {
				if(bw != null) try { bw.close(); } catch(Exception ignorable) {}
			}
			DeleteOnCloseFileInputStream docis = new DeleteOnCloseFileInputStream(tmpFile);
			docis.setFileName("parametriDvorana"+ys.getId()+".txt");
			docis.setMimeType("application/octet-stream");
			data.setStream(docis);
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		} catch (IOException e) {
			e.printStackTrace();
			data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
	}

	public static void exportRoomParameters1B(EntityManager em, CourseParameters1Data data) {

		// Dozvole
		boolean canView = JCMSSecurityManagerFactory.getManager().isAdmin();
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		String requestedSemesterID = data.getYearSemesterID();
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "miScheduleParam"); // Vrijednost mora biti oznaka semestra! Npr. 2009Z
			if(kv!=null && kv.getValue()!=null && !kv.getValue().isEmpty()) {
				requestedSemesterID = kv.getValue();
			}
		}
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			requestedSemesterID = BasicServiceSupport.getCurrentSemesterID(em);
		}
		if(StringUtil.isStringBlank(requestedSemesterID)) {
			data.getMessageLogger().addErrorMessage("Nije definiran željeni semestar.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		YearSemester ys = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, requestedSemesterID);
		if(ys==null) {
			data.getMessageLogger().addErrorMessage("Traženi semestar ne postoji.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<CourseInstance> ciList = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForSemester(em, ys.getId());

		Map<String,Integer> defDurations = loadDefaultDurations(ys.getId(), null);
		List<AssessmentTag> tags = resolveTags(em);

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		File tmpFile = null;
		try {
			tmpFile = File.createTempFile("SCH", null);
			BufferedWriter bw = null;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(tmpFile)), "UTF-8"));
				for(CourseInstance courseInstance : ciList) {
					CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, courseInstance, "paramMISched");
					CIP1 cip1;
					if(cikv!=null && !StringUtil.isStringBlank(cikv.getValue())) {
						cip1 = deserialize(cikv.getValue());
					} else {
						cip1 = buildDefault(em, courseInstance, defDurations, tags, new ArrayList<Room>());
					}
					for(CIP1TermDuration t : cip1.terms) {
						bw.write(courseInstance.getCourse().getIsvuCode());
						bw.write("\t");
						bw.write(findAssessmentTagShortName(tags, t.getAssessmentTagID()));
						bw.write("\t");
						bw.write(Integer.toString(t.getDuration()));
						bw.write("\n");
					}
				}
				bw.flush();
			} catch(Exception ex) {
				tmpFile.delete();
				data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			} finally {
				if(bw != null) try { bw.close(); } catch(Exception ignorable) {}
			}
			DeleteOnCloseFileInputStream docis = new DeleteOnCloseFileInputStream(tmpFile);
			docis.setFileName("trajanjaIspita"+ys.getId()+".txt");
			docis.setMimeType("application/octet-stream");
			data.setStream(docis);
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		} catch (IOException e) {
			e.printStackTrace();
			data.getMessageLogger().addErrorMessage("Greška prilikom izrade datoteke.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
	}

	private static String findAssessmentTagShortName(List<AssessmentTag> tags, long id) {
		for(AssessmentTag at : tags) {
			if(at.getId().longValue()==id) return at.getShortName();
		}
		return "???";
	}
	
	private static boolean provjeriIUsporedi(IMessageLogger messageLogger, CIP1 cip1, List<CIP1TermDuration> terms, List<CIP1RoomParams> rooms) {
		boolean greske = false;
		for(CIP1TermDuration t : terms) {
			CIP1TermDuration t2 = pronadi(cip1, t);
			if(t2==null) {
				messageLogger.addWarningMessage("Ispit \"" + t.getCaption() + "\" više ne postoji.");
				continue;
			}
			if(t.getDuration()<1) {
				messageLogger.addWarningMessage("Trajanje ispita \"" + t.getCaption() + "\" nije prihvatljivo. Vrijednost je resetirana.");
				greske = true;
				continue;
			} else {
				t2.setDuration(t.getDuration());
			}
		}
		for(CIP1RoomParams r : rooms) {
			CIP1RoomParams r2 = pronadi(cip1, r);
			if(r2==null) {
				messageLogger.addWarningMessage("Prostorija \"" + r.getRoomName() + "\" više ne postoji.");
				continue;
			}
			if(r.getAssistants()<1) {
				messageLogger.addWarningMessage("Broj asistenata ("+r.getAssistants()+") na \"" + r.getRoomName() + "\" nije prihvatljiv.");
				greske = true;
				continue;
			} else {
				r2.setAssistants(r.getAssistants());
			}
			if(r.getStudents()<0) {
				messageLogger.addWarningMessage("Broj studenata ("+r.getStudents()+") na \"" + r.getStudents() + "\" nije prihvatljiv.");
				greske = true;
				continue;
			} else {
				r2.setStudents(r.getStudents());
			}
		}
		return !greske;
	}

	private static CIP1RoomParams pronadi(CIP1 cip1, CIP1RoomParams r) {
		for(CIP1RoomParams ri : cip1.rooms) {
			if(ri.getRoomId().equals(r.getRoomId())) return ri;
		}
		return null;
	}

	private static CIP1TermDuration pronadi(CIP1 cip1, CIP1TermDuration t) {
		for(CIP1TermDuration ti : cip1.terms) {
			if(ti.getAssessmentTagID()==t.getAssessmentTagID()) return ti;
		}
		return null;
	}

	private static void sort(CIP1 cip1) {
		Collections.sort(cip1.rooms, new Comparator<CIP1RoomParams>() {
			@Override
			public int compare(CIP1RoomParams o1, CIP1RoomParams o2) {
				return StringUtil.HR_COLLATOR.compare(o1.getRoomName(), o2.getRoomName());
			}
		});
		Collections.sort(cip1.terms, new Comparator<CIP1TermDuration>() {
			@Override
			public int compare(CIP1TermDuration o1, CIP1TermDuration o2) {
				int i1 = 0;
				if(o1.getCaption().startsWith("Prvi ")) {
					i1 = 1;
				} else if(o1.getCaption().startsWith("Drugi ")) {
					i1 = 2;
				} else if(o1.getCaption().startsWith("Završni ")) {
					i1 = 3;
				}
				int i2 = 0;
				if(o2.getCaption().startsWith("Prvi ")) {
					i2 = 1;
				} else if(o2.getCaption().startsWith("Drugi ")) {
					i2 = 2;
				} else if(o2.getCaption().startsWith("Završni ")) {
					i2 = 3;
				}
				if(i1>0 && i2>0) {
					if(i1!=i2) return i1-i2;
				}
				return StringUtil.HR_COLLATOR.compare(o1.getCaption(), o2.getCaption());
			}
		});
	}

	private static void consolidate(EntityManager em, CIP1 cip1) {
		// Korak 1: konsolidacija soba
		List<Room> rooms = resolveAcceptableRooms(em);
		// 1-a: koje su sobe stvarno moguce?
		Set<String> existingRoomIDs = new HashSet<String>();
		for(Room r: rooms) {
			existingRoomIDs.add(r.getId());
		}
		// 1-b: ako imam na popisu sobu koja vise nije moguca, makni je
		Iterator<CIP1RoomParams> it1 = cip1.rooms.iterator();
		while(it1.hasNext()) {
			CIP1RoomParams p = it1.next();
			if(!existingRoomIDs.contains(p.getRoomId())) it1.remove();
		}
		// 1-c: koje ja imam sobe
		existingRoomIDs.clear();
		for(CIP1RoomParams p: cip1.rooms) {
			existingRoomIDs.add(p.getRoomId());
		}
		// 1-d: ako je moguca neka soba koju ja nemam, dodaj je...
		for(Room r: rooms) {
			if(!existingRoomIDs.contains(r.getId())) {
				cip1.rooms.add(new CIP1RoomParams(r.getId(),r.getShortName()+" ("+r.getVenue().getShortName()+")",r.getAssessmentPlaces(),r.getAssessmentAssistants(),r.getAssessmentPlaces(),r.getAssessmentAssistants()));
				existingRoomIDs.add(r.getId());
			}
		}
		
		// Korak 2: konsolidacija tagova
		List<AssessmentTag> tags = resolveTags(em);
		// 1-a: koji su tagovi stvarno moguci?
		Set<Long> existingTagIDs = new HashSet<Long>();
		for(AssessmentTag t : tags) {
			existingTagIDs.add(t.getId());
		}
		// 1-b: ako imam na popisu tag koji vise nije moguc, makni ga
		Iterator<CIP1TermDuration> it2 = cip1.terms.iterator();
		while(it2.hasNext()) {
			CIP1TermDuration p = it2.next();
			if(!existingTagIDs.contains(p.getAssessmentTagID())) it1.remove();
		}
		// 1-c: koje ja imam tagove
		existingTagIDs.clear();
		for(CIP1TermDuration p: cip1.terms) {
			existingTagIDs.add(p.getAssessmentTagID());
		}
		// 1-d: ako je moguci neki tag kojeg ja nemam, dodaj ga...
		for(AssessmentTag t: tags) {
			if(!existingTagIDs.contains(t.getId())) {
				cip1.terms.add(new CIP1TermDuration(t.getId(), t.getName(), 120));
				existingTagIDs.add(t.getId());
			}
		}
	}

	private static String serialize(CIP1 cip1) {
		StringBuilder sb = new StringBuilder(4096);
		sb.append("v1\n");
		for(CIP1TermDuration t : cip1.terms) {
			sb.append("t\t").append(t.getAssessmentTagID()).append("\t").append(t.getCaption()).append("\t").append(t.getDuration()).append("\n");
		}
		for(CIP1RoomParams r : cip1.rooms) {
			sb.append("r\t").append(r.getRoomId()).append("\t").append(r.getRoomName()).append("\t").append(r.getStudents()).append("\t").append(r.getAssistants()).append("\t").append(r.getDefaultStudents()).append("\t").append(r.getDefaultAsistants()).append("\n");
		}
		return sb.toString();
	}

	private static CIP1 deserialize(String value) {
		BufferedReader br = new BufferedReader(new StringReader(value));
		try {
			String version = br.readLine();
			if("v1".equals(version)) return deserializeV1(br);
			return emptyCIP1();
		} catch(Exception ex) {
			return emptyCIP1();
		}
	}

	private static CIP1 deserializeV1(BufferedReader br) throws IOException {
		CIP1 cip1 = new CIP1();
		cip1.rooms = new ArrayList<CIP1RoomParams>();
		cip1.terms = new ArrayList<CIP1TermDuration>();
		while(true) {
			String line = br.readLine();
			if(line == null) break;
			line = line.trim();
			if(line.isEmpty()) continue;
			String[] elems = line.split("\t");
			if(elems[0].equals("t")) {
				cip1.terms.add(new CIP1TermDuration(Long.parseLong(elems[1]), elems[2], Integer.parseInt(elems[3])));
				continue;
			}
			if(elems[0].equals("r")) {
				cip1.rooms.add(new CIP1RoomParams(elems[1], elems[2], Integer.parseInt(elems[3]), Integer.parseInt(elems[4]), Integer.parseInt(elems[5]), Integer.parseInt(elems[6])));
			}
		}
		return cip1;
	}

	private static CIP1 emptyCIP1() {
		CIP1 cip1 = new CIP1();
		cip1.rooms = new ArrayList<CIP1RoomParams>();
		cip1.terms = new ArrayList<CIP1TermDuration>();
		return cip1;
	}

	private static CIP1 buildDefault(EntityManager em, CourseInstance ci, Map<String,Integer> defDurations, List<AssessmentTag> tags, List<Room> rooms) {
		if(tags==null) tags = resolveTags(em);
		if(rooms==null) rooms = resolveAcceptableRooms(em);
		CIP1 cip1 = emptyCIP1();
		for(AssessmentTag t : tags) {
			Integer dd = defDurations.get((ci.getCourse().getIsvuCode()+"\t$\t"+t.getShortName()).toUpperCase());
			int defDur = 120;
			if(dd!=null) {
				defDur = dd.intValue();
			} else if(t.getShortName().equalsIgnoreCase("MI1") || t.getShortName().equalsIgnoreCase("MI2")) {
				defDur = 90;
			}
			cip1.terms.add(new CIP1TermDuration(t.getId(), t.getName(), defDur));
		}
		for(Room r : rooms) {
			cip1.rooms.add(new CIP1RoomParams(r.getId(),r.getShortName()+" ("+r.getVenue().getShortName()+")",r.getAssessmentPlaces(),r.getAssessmentAssistants(),r.getAssessmentPlaces(),r.getAssessmentAssistants()));
		}
		return cip1;
	}

	private static CIP1 buildDefault(EntityManager em, CourseInstance ci, Map<String,Integer> defDurations) {
		return buildDefault(em, ci, defDurations, null, null);
	}

	private static CIP1 buildDefault(EntityManager em, CourseInstance ci) {
		Map<String,Integer> defDurations = loadDefaultDurations(ci.getYearSemester().getId(), ci.getCourse().getIsvuCode());
		return buildDefault(em, ci, defDurations);
	}

	private static Map<String, Integer> loadDefaultDurations(String semesterID, String isvuCode) {
		Map<String, Integer> res = new HashMap<String, Integer>();
		try {
			String fileName = "courseParams/courseParam1_1_"+semesterID+".txt";
			InputStream is = CourseParametersService.class.getClassLoader().getResourceAsStream(fileName);
			if(is!=null) {
				try {
					BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(is),"UTF-8"));
					while(true) {
						String line = br.readLine();
						if(line == null) break;
						line = line.trim();
						if(line.isEmpty() || line.charAt(0)=='#') continue;
						String[] el = StringUtil.split(line, '\t');
						if(el==null || el.length!=3) {
							logger.error("File "+fileName+" contains missformatted line: ["+line+"]. Skipped.");
							continue;
						}
						if(isvuCode!=null && !isvuCode.equals(el[0])) continue;
						Integer value = null;
						try {
							value = Integer.parseInt(el[2]);
						} catch(Exception ex) {
							logger.error("File "+fileName+" contains uninterpretable integer at line: ["+line+"]. Skipped.");
							continue;
						}
						res.put(el[0]+"\t$\t"+el[1], value);
					}
				} finally {
					if(is!=null) is.close();
				}
			} else {
				logger.warn("File "+fileName+" needed to build default configuration does not exists.");
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		return res;
	}

	private static List<Room> resolveAcceptableRooms(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Room> rooms = dh.getRoomDAO().list(em);
		Iterator<Room> it = rooms.iterator();
		while(it.hasNext()) {
			Room r = it.next();
			if(!r.getPublicRoom()) {
				it.remove();
				continue;
			}
			if(r.getAssessmentAssistants()<1 || r.getAssessmentPlaces()<1) {
				it.remove();
				continue;
			}
		}
		return rooms;
	}

	private static List<AssessmentTag> resolveTags(EntityManager em) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<AssessmentTag> list = dh.getAssessmentTagDAO().list(em);
		KeyValue kv = dh.getKeyValueDAO().get(em, "miScheduleParamFlags");
		Set<String> toKeep = new HashSet<String>();
		if(kv!=null && !StringUtil.isStringBlank(kv.getValue())) {
			String[] tags = kv.getValue().split(",");
			for(String tag : tags) {
				toKeep.add(tag);
			}
		} else {
			// Ako nije zadano u repozitoriju, onda hardkodirano gledam samo ove:
			toKeep.add("MI1");
			toKeep.add("MI2");
			toKeep.add("ZI");
		}
		Iterator<AssessmentTag> it = list.iterator();
		while(it.hasNext()) {
			AssessmentTag at = it.next();
			if(toKeep.contains(at.getShortName())) continue;
			it.remove();
		}
		Collections.sort(list, new Comparator<AssessmentTag>() {
			@Override
			public int compare(AssessmentTag o1, AssessmentTag o2) {
				return o1.getShortName().compareTo(o2.getShortName());
			}
		});
		
		return list;
	}

	private static ParameterAttributes getMIScheduleParameterAttributes(EntityManager em, CourseInstance ci) {
		ParameterAttributes pa = new ParameterAttributes();
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean schedVisible = false;
		boolean schedModif;
		KeyValue kv = dh.getKeyValueDAO().get(em, "miScheduleParam"); // Vrijednost mora biti oznaka semestra! Npr. 2009Z
		if(kv==null || kv.getValue()==null || kv.getValue().isEmpty()) {
			schedModif = false;
		} else {
			schedModif = ci.getYearSemester().getId().equals(kv.getValue());
		}
		// Ako se cini da je formular za raspored modifikabilan, idemo vidjeti jos ima li pridruzen datum
		Date date;
		kv = dh.getKeyValueDAO().get(em, "miScheduleParamDate"); // Vrijednost mora biti datum: yyyy-MM-dd HH:mm:ss
		if(kv==null || kv.getValue()==null || kv.getValue().isEmpty()) {
			date = null;
		} else {
			try {
				date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(kv.getValue());
			} catch(Exception ex) {
				date = null;
			}
		}
		// Ako nema datuma, ili ako je on u proslosti, vise ne mozemo modificirati, bez obzira na prethodni zakljucak
		if(date==null || date.before(new Date())) {
			schedModif = false;
		}
		// Kada je raspored vidljiv: (1) ako vec postoji u bazi ili (2) ako ne postoji ali je mofikabilan pa ga idem stvoriti
		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, ci, "paramMISched");
		if(schedModif) {
			schedVisible = true;
		} else {
			if(cikv!=null) {
				schedVisible = true;
			}
		}
	
		pa.setModifiable(schedModif);
		pa.setVisible(schedVisible);
		pa.setModifiableUntil(date);
		pa.setKeyValue(cikv);
		
		return pa;
	}
}
