package hr.fer.zemris.util.scheduling.algorithms.PSO;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.util.scheduling.algorithms.PSO.TermRecord;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;
import hr.fer.zemris.util.time.TimeStamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Particle {

	//zauzece studenata
	public Map<String, List<String>> studentOccupancyList = new HashMap<String, List<String>>();
	//dogadjaji koristeni termini u njima
	public Map<String, List<TermRecord>> eventsTermList = new HashMap<String, List<TermRecord>>();
	//svi slobodni termini u planu
	public List<TermRecord> planTermList = new ArrayList<TermRecord>();
	String planName;
	Map<String, Event> eventData = new HashMap<String, Event>();
	public int fitness = Integer.MAX_VALUE;
	Particle ownBestResult;
	boolean equalStudentDistribution = false;
	Random random = new Random();
	
	//stvara kopiju cestice
	public Particle(Particle particle)
	{
		planName = particle.planName;
		fitness = particle.fitness;
		//ownBestResult = particle.ownBestResult;
		ownBestResult = null;

		for(String event : particle.eventsTermList.keySet())
		{
			eventsTermList.put(event, new ArrayList<TermRecord>());
			for(TermRecord term : particle.eventsTermList.get(event))
				eventsTermList.get(event).add(new TermRecord(term));
		}
		
		for(String student : particle.studentOccupancyList.keySet())
		{
			studentOccupancyList.put(student, new ArrayList<String>());
			for(String term : particle.studentOccupancyList.get(student))
				studentOccupancyList.get(student).add(new String(term));
		}	
		
		for(TermRecord term : particle.planTermList)
			planTermList.add(new TermRecord(term));
		
		for(String event : particle.eventData.keySet())
			eventData.put(event, new Event(particle.eventData.get(event)));		
	}

	//stvara novu cesticu iz postojeceg rjesenja
	public Particle(ISchedulingResult result, IPlan planData, Map<String, ISchedulingData> eventsSchedulingData)
	{
		IPlan plan = result.getPlan();
		planName = plan.getName();
		List<TermRecord> rawTerms = null;
		
		equalStudentDistribution = plan.isEqualStudentDistributionInEachEvent();
		
		for(IEvent event : plan.getPlanEvents())
		{			
			if(eventsSchedulingData.get(event.getId()) != null)
				rawTerms = prepareTermData(event.getId(), eventsSchedulingData.get(event.getId()).getTermData());
			if(eventsSchedulingData.get(event.getId()) != null)
				prepareJmbagData(event.getId(),eventsSchedulingData.get(event.getId()).getPeopleData());
			
			eventsTermList.put(event.getId(), new ArrayList<TermRecord>());
			for(ITerm term : event.getTerms())
			{
				TermRecord tempTerm = new TermRecord(term, event.getId());
				for(IPrecondition precondition : event.getPreconditionEvents())
					tempTerm.preconditions.add(precondition.getEvent().getId());
				
				if(eventsSchedulingData.get(term.getId()) != null)
					rawTerms = prepareTermData(event.getId(), eventsSchedulingData.get(term.getId()).getTermData());
				if(eventsSchedulingData.get(term.getId()) != null)
					prepareJmbagData(event.getId(),eventsSchedulingData.get(term.getId()).getPeopleData());
				if(event.getEventDistribution().getType() == Definition.GIVEN_DISTRIBUTION)
				{
					tempTerm.termId = term.getId();
					tempTerm.termName = term.getName();
					tempTerm.rawTerms = rawTerms;
				}
				
				for(String student : term.getDefinition().getIndividuals())
					studentOccupancyList.get(student).add(event.getId() + "|" + tempTerm.toString());
				
				eventsTermList.get(event.getId()).add(tempTerm);	
			}
			syncPlanWith(eventsTermList.get(event.getId()));
		}
		
		for(IEvent event : planData.getPlanEvents())
		{
			if(event.getPreconditionEvents().size() == 0)
				eventData.put(event.getId(), new Event(event.getName(), null, 
					event.getDefinition().getTimeParameters(), event.getDefinition().getLocationParameters()));
			else
				eventData.put(event.getId(), new Event(event.getName(), event.getPreconditionEvents(), 
					event.getDefinition().getTimeParameters(), event.getDefinition().getLocationParameters()));
		}
		
		calculateFitness();
		
		ownBestResult = new Particle(this);
	}
	
	//stvara novu cesticu 
	public Particle(IPlan plan, Map<String, ISchedulingData> eventsSchedulingData) throws SchedulingException
	{
		List<IEvent> events = sortEvents(plan.getPlanEvents());
		boolean notFirst = false;
		
		planName = plan.getName();
		if(plan.isEqualStudentDistributionInEachEvent()) equalStudentDistribution = true;
		
		for(IEvent event : events)
		{
			List<ITimeParameter> time = event.getDefinition().getTimeParameters();
			List<ILocationParameter> location = event.getDefinition().getLocationParameters();
			List<TermRecord> eventTermList = null;
			eventsTermList.put(event.getId(), new ArrayList<TermRecord>());
			
			//slucajna distribucija
			if (event.getEventDistribution().getType() == Definition.RANDOM_DISTRIBUTION)
			{	
				List<TermRecord> eventRawTermList = prepareTermData(event.getId(), eventsSchedulingData.get(event.getId()).getTermData());
				syncWithPlan(eventRawTermList, event.getId());
				Map<String, List<String>> eventStudentList = prepareJmbagData(event.getId(),eventsSchedulingData.get(event.getId()).getPeopleData());
				
				if(plan.isEqualStudentDistributionInEachEvent() && notFirst)
				{
					if(event.getPreconditionEvents().size() == 0)
						eventData.put(event.getId(), new Event(event.getName(), null, time, location));
					else
						eventData.put(event.getId(), new Event(event.getName(), event.getPreconditionEvents(), time, location));
					
					for(String tempEvent : eventsTermList.keySet())
					{
						eventTermList = generateTerms(eventsTermList.get(tempEvent), eventRawTermList);
						syncPlanWith(eventTermList);
						break;
					}
				}
				else if (event.getPreconditionEvents().size() == 0)
				{
					eventData.put(event.getId(), new Event(event.getName(), null, time, location));
					//stvaranje termina
					int i = 0;
					while(true)
					{
						try
						{
							//stvara termine
							eventTermList = generateTerms(eventStudentList, eventRawTermList, null,
													event.getEventDistribution().getMaximumTermNumber(), 
													event.getEventDistribution().getMinimumTermNumber(),
													event.getTermDuration());
							
//							for(TermRecord term : eventTermList)
//								System.out.println(term.toString());
							
							syncPlanWith(eventTermList);
							notFirst = true;
							break;
						}
						catch(SchedulingException e)
						{
							resetStudents(eventStudentList.keySet(), event.getId());
							resetTerms(eventRawTermList, event.getId());
							if(++i > 50)
								{
								System.out.println("neuspjeh");	
								throw e;
								}
						}
					}
					
				}
				else
				{ 
					eventData.put(event.getId(), new Event(event.getName(), event.getPreconditionEvents(), time, location));
					List<TermRecord> eventPrecTerms = new ArrayList<TermRecord>();
					
					//postavlja preduvjete
					for(TermRecord term : eventRawTermList)
						term.setPreconditions(event.getPreconditionEvents());
					
					//stvara listu termina svih dogadjaja koji su preduvjeti i stvara listu vremenskih udaljenosti 
					//koje termini trebaju zadovoljiti
					for(IPrecondition precondition : event.getPreconditionEvents())
					{
						for(TermRecord term : eventsTermList.get(precondition.getEvent().getId()))
						{
							TermRecord temp = new TermRecord(term);
							preparePrecondition(temp, precondition.getTimeDistance());
							eventPrecTerms.add(temp);
						}
					}
									
					sort(eventPrecTerms);
					//iz slobodnih termina izbacuje sve prije kraja prvog preduvjeta
					subtractPrecondition(eventRawTermList, eventPrecTerms.get(0));
					
					//provjerava je li moguce za zadane preduvjete i vremensko razdoblje dodat ijedan termin
					isPossible(eventRawTermList, eventPrecTerms, event.getTermDuration());
					
					int i = 0;
					while(true)
					{
						try
						{	
							//stvara termine
							eventTermList = generateTerms(eventStudentList, eventRawTermList, eventPrecTerms,
													event.getEventDistribution().getMaximumTermNumber(), 
													event.getEventDistribution().getMinimumTermNumber(),
													event.getTermDuration());
							
//							for(TermRecord term : eventTermList)
//								System.out.println(term.toString());
							
							syncPlanWith(eventTermList);
							notFirst = true;
							break;
						}
						catch(SchedulingException e)
						{
							resetStudents(eventStudentList.keySet(), event.getId());
							resetTerms(eventRawTermList, event.getId());
							if(++i > 20) throw e;	
						}
					}				
				}
			}
			else if(event.getEventDistribution().getType() == Definition.GIVEN_DISTRIBUTION)
			{
				int type = 0;
				if(plan.getDefinition().getIndividuals().size() != 0 || event.getDefinition().getIndividuals().size() != 0)
					type = 1;

				if(plan.isEqualStudentDistributionInEachEvent() && notFirst)
				{
					if(event.getPreconditionEvents().size() == 0)
						eventData.put(event.getId(), new Event(event.getName(), null, time, location));
					else 
						eventData.put(event.getId(), new Event(event.getName(), event.getPreconditionEvents(), time, location));
					
					eventTermList = generateTerms(event.getId(), event.getTerms(), eventsSchedulingData);
				}				
				else if(event.getPreconditionEvents().size() == 0)
				{	
					eventData.put(event.getId(), new Event(event.getName(), null, time, location));
					eventTermList = generateTerms(event, null, type, eventsSchedulingData) ;
					notFirst = true;
				}
				else if(event.getPreconditionEvents().size() != 0)
				{
					eventData.put(event.getId(), new Event(event.getName(), event.getPreconditionEvents(), time, location));
					List<TermRecord> eventPrecTerms = new ArrayList<TermRecord>();
					
					for(IPrecondition precondition : event.getPreconditionEvents())
					{
						for(TermRecord term : eventsTermList.get(precondition.getEvent().getId()))
						{
							TermRecord temp = new TermRecord(term);
							preparePrecondition(temp, precondition.getTimeDistance());
							eventPrecTerms.add(temp);
						}
					}
					
					sort(eventPrecTerms);
					eventTermList = generateTerms(event, eventPrecTerms, type, eventsSchedulingData);
					notFirst = true;
				}
			}
			
			eventsTermList.get(event.getId()).addAll(eventTermList);
		}
		
		calculateFitness();
		ownBestResult = this;
	}

	//razmisliti kako poboljsati fitness funkciju
	public void calculateFitness()
	{
		int distanceComponent = 0, occupancyComponent = 0;
		int A = 10, B = 1;
		
		fitness = 0;
		for(String student : studentOccupancyList.keySet())
		{
			List<TermRecord> occupancyList = toTerm(studentOccupancyList.get(student));
			sort(occupancyList);
			for(int i = 0; i < occupancyList.size() - 1; i++)
			{
				TermRecord firstTerm = occupancyList.get(i);
				TermRecord secondTerm = occupancyList.get(i+1);
				
				int distance = absoluteDate(secondTerm.date) - absoluteDate(firstTerm.date);
				if(distance*720 > 10800) continue;
				if(distance == 0)
					distance = secondTerm.fromTime.getAbsoluteTime() - firstTerm.toTime.getAbsoluteTime();
				else
				{
					distance *= 720;
					distance -= 720 + secondTerm.fromTime.getAbsoluteTime() - firstTerm.toTime.getAbsoluteTime();
				}
				
				if (!firstTerm.room.equals("undef") && !secondTerm.room.equals("undef"))
					distanceComponent += (int) Math.exp(10.*(10800- distance)/10800.);
				else if((firstTerm.room.equals("undef") && !secondTerm.room.equals("undef")) ||
						(!firstTerm.room.equals("undef") && secondTerm.room.equals("undef")))
					distanceComponent += distance;
			}
		}
		
		for(String event : eventsTermList.keySet())
			for(TermRecord term : eventsTermList.get(event))
				occupancyComponent += (int) Math.exp(10.*term.students.size()/(float)term.roomCapacity);
		
		this.fitness = A * distanceComponent + B * occupancyComponent;
	}
	
	//zamjena studenata
	public void switchStudents(Particle globalBestResult)

	{
		if(equalStudentDistribution) return;
		switchStudentsWithSelf();
		switchStudentsWithBest(ownBestResult);
		switchStudentsWithBest(globalBestResult);
	}

	//zamjena termina
	public void switchTerms(Particle globalBestResult)
	{
		Set<String> usedEvents = new HashSet<String>();
		int moved = 0, globalSwitched = 0, ownSwitched = 0, size = eventsTermList.keySet().size();

		for(String event : eventsTermList.keySet())
		{
			if(moved < 2 && random.nextInt(size)%2 == 0 && moveTerm(event))
			{
					usedEvents.add(event);
					moved++;
					continue;
			}
			if(!equalStudentDistribution && globalSwitched < 2 && random.nextInt(size)%3 == 0 && 
					eventsTermList.get(event).size() == globalBestResult.eventsTermList.get(event).size() &&
					copyTermFromParticle(globalBestResult, event))
				{
						usedEvents.add(event);
						globalSwitched++;
						continue;
				}
			if(!equalStudentDistribution && ownSwitched < 2 && random.nextInt(size)%2 == 0 && 
				eventsTermList.get(event).size() == ownBestResult.eventsTermList.get(event).size() &&
				copyTermFromParticle(ownBestResult, event))
			{
					usedEvents.add(event);
					ownSwitched++;
					continue;
			}	
		}
		
//		System.out.println("Moved: " + moved + "\nOwn best switched: " + ownSwitched +
//				"\nGlobal best switched: " + globalSwitched);
	}
	
	//sprema rjesenje u ISchedulingResult
	public ISchedulingResult toResult()
	{
		ISchedulingResult result = new SchedulingResult();
		
		result.addPlan(planName);
		
		for(String eventId : eventData.keySet())
		{
			result.addEvent(eventData.get(eventId).name, eventId);
			for(int i = 0; i < eventsTermList.get(eventId).size(); i++)
			{
				TermRecord temp = eventsTermList.get(eventId).get(i);
				
				result.addTerm(eventData.get(eventId).name, temp.termName == null ? "Termin" + i + 1 : temp.termName,
						temp.room, temp.roomCapacity, temp.date, temp.fromTime.getAbsoluteTime(), temp.toTime.getAbsoluteTime());
				
				for(String jmbag : temp.students)
					result.addStudentToTerm(eventData.get(eventId).name, temp.termName == null ? "Termin" + i + 1 : temp.termName, jmbag);
			}
		}
		
		return result;
	}
	
	
	private List<IEvent> sortEvents(List<IEvent> planEvents)
	{
		List<IEvent> result = new ArrayList<IEvent>();
		
		for(IEvent event : planEvents)
			if(event.getPreconditionEvents().size() == 0)
				result.add(event);
		while(result.size() != planEvents.size())
		{
			boolean hasPrecondition = true;
			for(IEvent event : planEvents)
			{
				if(!result.contains(event))
				{
					for(IPrecondition precondition : event.getPreconditionEvents())
						if(!result.contains(precondition.getEvent())) hasPrecondition = false;
					if(hasPrecondition) result.add(event);
				}
			}
		}
		
		return result;
	}
	
	//priprema listu slobodnih termina za dogadjaj
	private List<TermRecord> prepareTermData(String eventId, Map<RoomData, Map<DateStamp, List<TimeSpan>>> termData)
	{
		List<TermRecord> termList = new ArrayList<TermRecord>();
		for (Map.Entry<RoomData, Map<DateStamp, List<TimeSpan>>> roomEntry : termData.entrySet())
		{
			RoomData room = roomEntry.getKey();
			for (Map.Entry<DateStamp, List<TimeSpan>> dateEntry : roomEntry.getValue().entrySet())
			{
				DateStamp date = dateEntry.getKey();
				for (TimeSpan timeEntry : dateEntry.getValue())
				{					
					TermRecord temp = new TermRecord(eventId, room.getId(), room.getCapacity(), date.getStamp(), 
												timeEntry.getStart(), timeEntry.getEnd(), new ArrayList<String>());
					termList.add(temp);
					addTermToList(temp);
				}
			}
		}
			
		return termList;
	}
	
	//dodaje termin u listu termina plana ako vec ne postoji
	private void addTermToList(TermRecord term)
	{		
		for(TermRecord temp : planTermList)
		{
//			if(temp == null) 
//				System.out.println("");
			if (temp.date.equals(term.date) && temp.room.equals(term.room) && 
				(temp.fromTime.equals(term.fromTime) || temp.toTime.equals(term.toTime))) return;
		}
		
		planTermList.add(new TermRecord(term));
	}
	
	//priprema listu studenta s njihovim zauzecima sinkroniziranu sa zauzecima u planu
	private Map<String, List<String>> prepareJmbagData(String eventId, Map<String, Map<DateStamp, List<TimeSpan>>> peopleData)
	{
		Map<String, List<String>> studentList = new HashMap<String, List<String>>();
		for (Map.Entry<String, Map<DateStamp, List<TimeSpan>>> studentEntry : peopleData.entrySet())
		{
			String jmbag = studentEntry.getKey();
			if(!studentOccupancyList.containsKey(jmbag))
			{
				studentOccupancyList.put(jmbag, new ArrayList<String>());
				for (Map.Entry<DateStamp, List<TimeSpan>> dateEntry : studentEntry.getValue().entrySet())
				{
					DateStamp date = dateEntry.getKey();
					for (TimeSpan timeEntry : dateEntry.getValue())
					{
						String temp = eventId + "|" + "undef" + "#" + date.getStamp() + "$" + timeEntry.getStart().toString() +
						 "$" + timeEntry.getEnd().toString();
						studentOccupancyList.get(jmbag).add(temp);
					}
				}
				
			}
			else
			{
				for (Map.Entry<DateStamp, List<TimeSpan>> dateEntry : studentEntry.getValue().entrySet())
				{
					DateStamp date = dateEntry.getKey();
					for (TimeSpan timeEntry : dateEntry.getValue())
					{
						
						String temp = eventId + "|" + "undef" + "#" + date.getStamp() + "$" + timeEntry.getStart().toString() +
						 "$" + timeEntry.getEnd().toString();
						if(!studentOccupancyList.get(jmbag).contains(temp))
							studentOccupancyList.get(jmbag).add(temp);
					}
				}
			}
			studentList.put(jmbag, studentOccupancyList.get(jmbag));
		}
		
		return studentList;
	}
	
	//sinkornizira listu slobodnih termina s planom
	private void syncWithPlan(List<TermRecord> terms, String eventId)
	{
		Set<String> eventTerms = new HashSet<String>();
		
		for(TermRecord eventTerm : terms)
			eventTerms.add(eventTerm.room + "&" + eventTerm.date);
		terms.clear();
		
		for(String eventTerm : eventTerms)
		{
			for(TermRecord planTerm : planTermList)
			{
				if(eventTerm.equals(planTerm.room + "&" + planTerm.date))
				{
					TermRecord newTerm = new TermRecord(planTerm);
					newTerm.setPreconditions(new ArrayList<String>());
					newTerm.setEventId(eventId);
					terms.add(newTerm);
				}
			}
		}
	}
	
	//sinkronizira listu slobodnih termina plana s zadanim terminima
	private void syncPlanWith(List<TermRecord> terms)
	{
		List<TermRecord> tempPlanTermList = new ArrayList<TermRecord>(planTermList);
		for (TermRecord eventTerm : terms)
		{
			if(eventTerm == null) continue;
			for(TermRecord planTerm : tempPlanTermList)
			{
//				if(planTerm == null)
//					System.out.println("");
				if(!eventTerm.date.equals(planTerm.date) || !eventTerm.room.equals(planTerm.room)) continue;
				if(	eventTerm.fromTime.getAbsoluteTime() > planTerm.fromTime.getAbsoluteTime() &&
					eventTerm.toTime.getAbsoluteTime() < planTerm.toTime.getAbsoluteTime())
				{
					planTermList.add(planTermList.indexOf(planTerm)+1, new TermRecord(planTerm.eventId, planTerm.room,
							planTerm.roomCapacity, planTerm.date, new TimeStamp(eventTerm.toTime.getAbsoluteTime()),
							planTerm.toTime, null));
					planTerm.setToTime(eventTerm.fromTime);
					break;
				}
				else if(eventTerm.equals(planTerm))
				{
					planTermList.remove(planTerm);
					break;
				}
				else if(eventTerm.fromTime.getAbsoluteTime() == planTerm.fromTime.getAbsoluteTime())
				{
					planTerm.setFromTime(eventTerm.toTime);
					break;
				}
				else if(eventTerm.toTime.getAbsoluteTime() == planTerm.toTime.getAbsoluteTime())
				{
					planTerm.setToTime(eventTerm.fromTime);
					break;
				}
			}
		}
	}
	
	//vraca listu slobodnih termina za dogadjaj na pocetno stanje
	private void resetTerms(List<TermRecord> rawTermList, String eventId)
	{
		Set<String> terms = new HashSet<String>();
		TimeStamp zero = new TimeStamp(0); 
		
		for(TermRecord rawTerm : rawTermList)
		{
			if(rawTerm == null) continue;
			
			rawTerm.setToTime(zero);
			rawTerm.setFromTime(zero);
			terms.add(rawTerm.toString());
		}
		
		rawTermList.clear();
		
		for(TermRecord planTerm : planTermList)
		{
			TermRecord temp = new TermRecord("",planTerm.room,0,planTerm.date,zero,zero,null);
			if(terms.contains(temp.toString()))
			{
				planTerm.eventId = eventId;
				rawTermList.add(new TermRecord(planTerm));
			}
		}
	}
	
	//vraca zauzece studenata na poetno stanje
	private void resetStudents(Set<String> jmbags, String eventId)
	{
		for(String jmbag : jmbags)
		{
			List<String> tempTermList = studentOccupancyList.get(jmbag);
			if (tempTermList.size() > 0)
			{
				String[] event = tempTermList.get(tempTermList.size()-1).split("\\|");
				if(event[0].equals(eventId) && !event[1].contains("undef"))
					tempTermList.remove(tempTermList.size()-1);
			}
		}
	}
	
	//odredjuje ukupno vremensko zauzece preduvjeta termin + odmak
	private void preparePrecondition(TermRecord precondition, String distance)
	{	
		if(distance.endsWith("d"))
		{
			StringBuilder sb = new StringBuilder();
			Calendar date = GregorianCalendar.getInstance();
			date.set(Integer.parseInt(precondition.date.substring(0,4)), 
					Integer.parseInt(precondition.date.substring(5,7)) - 1, 
					Integer.parseInt(precondition.date.substring(8,10)));
			date.add(Calendar.DAY_OF_MONTH, Integer.parseInt(distance.substring(0,distance.length()-1)));
			int day = date.get(Calendar.DAY_OF_MONTH);
			int month = date.get(Calendar.MONTH) + 1;
			int year = date.get(Calendar.YEAR);
			sb.append(precondition.date + "$");
			sb.append(year + "-");
			if(month < 10) sb.append("0");
			sb.append(month + "-");
			if(day < 10) sb.append("0");
			sb.append(day);
			precondition.setDate(sb.toString());
			precondition.setToTime(new TimeStamp(20, 0));
		}
		else if(distance.endsWith("h"))
		{
			int hour = precondition.toTime.getHour() + Integer.parseInt(distance.substring(0,distance.length()-1));
			if (hour > 20) hour = 20;
			
			precondition.setDate(precondition.date + "$" + precondition.date);
			precondition.setToTime(new TimeStamp(hour, precondition.toTime.getMinute()));
		}
		else if(distance.endsWith("m"))
		{
			int hour = precondition.toTime.getHour() + Integer.parseInt(distance.substring(0,distance.length()-1))/60;
			int min = precondition.toTime.getMinute() + Integer.parseInt(distance.substring(0,distance.length()-1))%60;
			if (hour > 20) hour = 20;
			
			precondition.setDate(precondition.date + "$" + precondition.date);
			precondition.setToTime(new TimeStamp(hour, min));
		}
	}
	
	//stvara termine za zadane studente, slobodne termine, preduvjete, broj termina
	private List<TermRecord> generateTerms(Map<String, List<String>> studentList, List<TermRecord> rawTermList, 
			List<TermRecord> precTerms, int maxTermNumber, int minTermNumber, int duration)	throws SchedulingException
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		Set<String> usedStudents = new HashSet<String>();
		TermRecord eventTerm;
		int step = 60, offset = 0, capacity = 0;
		
		sort(rawTermList);
		
		int index = rawTermList.size();
		for(int i = 0; i < maxTermNumber + 1; i++)
			rawTermList.add(null);
		
		for(TermRecord term : rawTermList)
		{
			if(term == null) 
				throw new SchedulingException("Nije uspjelo stvaranje plana! \n   Dogadaj: " 
					+ rawTermList.get(0).eventId + "\n   Razlog: prekratki vremenski raspon");
			
			offset = 0;
			do
			{
				TermRecord[] tempTerms = generateTerm(term, offset, duration);
				if (tempTerms == null) break;
				
				eventTerm = tempTerms[1];

				try
				{
					List<TermRecord> satisfiedPreconditions = null;
					if(precTerms != null)
						satisfiedPreconditions = satisfiedPreconditions(eventTerm, precTerms);
					
					connectTermAndStudents(studentList, eventTerm, satisfiedPreconditions, usedStudents);
					capacity += eventTerm.roomCapacity;
					result.add(eventTerm);
					rawTermList.set(index, tempTerms[0]);
					index++;
					sort(rawTermList);
					offset += duration;
				}
				catch(SchedulingException e)
				{
					term.setToTime(tempTerms[0].toTime);
					offset += step;
					continue;
				}
			
				if(usedStudents.size() == studentList.keySet().size()) break;
				if(capacity >= studentList.keySet().size()){
					if(result.size() < minTermNumber)
						capacity -= removeBiggestRoom(result, rawTermList, usedStudents);
					else
						capacity -= removeTerm(result, rawTermList, usedStudents);
						
					sort(rawTermList);
					index--;
					offset -= (duration - step);
				}	
				
			}while(capacity < studentList.keySet().size());
			
			if(usedStudents.size() == studentList.keySet().size()) break;	
		}
		
		List<TermRecord> tempTermList = new ArrayList<TermRecord>(rawTermList);
		for(TermRecord temp : tempTermList)
			if(temp == null || temp.fromTime.equals(temp.toTime)) rawTermList.remove(temp);
		
		if(result.size() < minTermNumber || result.size() > maxTermNumber || 
			usedStudents.size() != studentList.keySet().size())
			throw new SchedulingException();
		
		
		return result;
	}
	
	//stvara termine s istom raspodjelom studenata kao i previouEventTermList
	private List<TermRecord> generateTerms(List<TermRecord> previousEventTermList, List<TermRecord> rawTermList)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		TermRecord eventTerm;
		int offset, step = 60;
		
		sort(rawTermList);
		
		int index = rawTermList.size();
		for(int i = 0; i < previousEventTermList.size() + 1; i++)
			rawTermList.add(null);
		
		for(TermRecord previousTerm : previousEventTermList)
		{
			for(TermRecord term : rawTermList)
			{
				if(term == null) throw new SchedulingException("Nije uspjelo stvaranje plana! \n   Dogadaj: " 
						+ rawTermList.get(0).eventId + "\n   Razlog: prekratki vremenski raspon");
				
				offset = 0;
				boolean created = false;
				while(true)
				{
					TermRecord[] tempTerms = generateTerm(term, offset, previousTerm.getDuration());
					if (tempTerms == null) break;
					
					eventTerm = tempTerms[1];
					
					if(!conflictedStudents(previousTerm.students, eventTerm) && 
							preconditionsSatisfied(eventTerm, getPreconditionTerms(eventTerm.eventId, previousTerm.students)))
					{
						eventTerm.students.addAll(previousTerm.students);
						for(String student : eventTerm.students)
							studentOccupancyList.get(student).add(eventTerm.eventId + "|" + eventTerm.toString());
						
						result.add(eventTerm);
						rawTermList.set(index, tempTerms[0]);
						index++;
						sort(rawTermList);
						created = true;
						break;
					}
					else
					{
						term.setToTime(tempTerms[0].toTime);
						offset += step;
					}
				}
				if(created) break;
			}
		}
		
		List<TermRecord> tempTermList = new ArrayList<TermRecord>(rawTermList);
		for(TermRecord temp : tempTermList)
			if(temp == null || temp.fromTime.equals(temp.toTime)) rawTermList.remove(temp);
		
		return result;
	}
	
	//generira termine kad je zadana razdioba
	private List<TermRecord> generateTerms(IEvent event, List<TermRecord> precTerms, int type, Map<String, ISchedulingData> schedulingData) throws SchedulingException
	{
		List<ITerm> terms = event.getTerms();
		String eventId = event.getId();
		int duration = event.getTermDuration();
		List<TermRecord> result = new ArrayList<TermRecord>();
		Set<String> usedStudents = new HashSet<String>();
		TermRecord eventTerm;
		int step = 60, offset = 0, k = 0, i;
		
		for(i = 0; i < terms.size(); i++)
		{
			ITerm term = terms.get(i);
			
			List<TermRecord> rawTermList = schedulingData.get(eventId) != null ? 
					prepareTermData(eventId, schedulingData.get(eventId).getTermData()) :
					prepareTermData(eventId, schedulingData.get(term.getId()).getTermData());
			syncWithPlan(rawTermList, eventId);
			
			Map<String, List<String>> termStudentList = schedulingData.get(eventId) != null ? 
					prepareJmbagData(eventId, schedulingData.get(eventId).getPeopleData()):
					prepareJmbagData(eventId, schedulingData.get(term.getId()).getPeopleData());
			
			if(precTerms != null)
			{
				for(TermRecord tempTerm : rawTermList)
					tempTerm.setPreconditions(event.getPreconditionEvents());
				
				sort(rawTermList);
				subtractPrecondition(rawTermList, precTerms.get(0));
				isPossible(rawTermList, precTerms, duration);
			}

			rawTermList.add(null);
			sort(rawTermList);
			
			for(TermRecord termTime : rawTermList)
			{
				if(termTime == null)
				{
					for(String student: usedStudents)
						studentOccupancyList.get(student).remove(studentOccupancyList.get(student).size() - 1);
					
					throw new SchedulingException("Nije uspjelo stvaranje plana! \n   Dogadaj: " 
						+ rawTermList.get(0).eventId + "\n   Termin: " + term.getId() + " " + term.getName() + 
						"\n   Razlog: prekratki vremenski raspon");
				}
				
				offset = 0;
				boolean out = false;
				while(!out)
				{
					TermRecord[] tempTerms = generateTerm(termTime, offset, duration);
					if (tempTerms == null) break;
					
					eventTerm = tempTerms[1];

					try{
						List<TermRecord> satisfiedPreconditions = null;
						if(precTerms != null)
							satisfiedPreconditions = satisfiedPreconditions(eventTerm, precTerms);
							
						connectTermAndStudents(termStudentList, eventTerm, satisfiedPreconditions, usedStudents);
						
						System.out.println(eventTerm.toString() + " " + eventTerm.students.size());
						rawTermList.set(rawTermList.size()-1, tempTerms[0]);
						eventTerm.termId = term.getId();
						eventTerm.termName = term.getName();
						eventTerm.rawTerms = new ArrayList<TermRecord>(rawTermList);
						result.add(eventTerm);
						offset += duration;
					}
					catch(SchedulingException e)
					{
						termTime.setToTime(tempTerms[0].toTime);
						offset += step;
						continue;
					}
					
					if((type == 0 && eventTerm.students.size() != termStudentList.keySet().size()))
					{
						planTermList.remove(null);
						syncPlanWith(result);
						removeTerm(result, rawTermList, usedStudents);
						offset -= (duration - offset);
					}
					else if((type == 0 && eventTerm.students.size() == termStudentList.keySet().size()))
					{						
						sort(rawTermList);
						planTermList.remove(null);
						syncPlanWith(result);
						break;
					}
					else if((type == 1 && result.size() == terms.size() && 
							usedStudents.size() < termStudentList.keySet().size()))
					{
						int j;
						if(++k > 50) throw new SchedulingException();
						
						planTermList.remove(null);
						syncPlanWith(result);
						
						List<TermRecord> tempResult = new ArrayList<TermRecord>(result);
						removeTerm(result, planTermList, usedStudents);
						rawTermList.set(rawTermList.size()-1, null);
						
						planTermList.remove(null);
						
						for(j = 0; j < tempResult.size(); j++)
							if(tempResult.get(j).students.size() == 0)
								break;
								
//						System.out.println(j + " " + rawTermList.indexOf(null));
						if(i != j)
						{
							i = j - 1;
							out = true;
							break;
						}
						else
							offset -= (duration - offset);
						
					}
					else if(type == 1 && result.size() != terms.size())
					{
						sort(rawTermList);
						planTermList.remove(null);
						syncPlanWith(result);
						out = true;
					}
					else if(type == 1 && result.size() == terms.size() && 
							usedStudents.size() == termStudentList.keySet().size())
					{
						planTermList.remove(null);
						syncPlanWith(result);
						break;
					}
				}
				
				if(out || (type == 0 && result.get(result.size()-1).students.size() == termStudentList.keySet().size()) || 
					(type == 1 && result.size() == terms.size() && usedStudents.size() == termStudentList.keySet().size()))
					break;				
			}
		}
		
		return result;
	}
	
	//generira termine za danu distribuciju s jednolikom raspodjelom studenata 
	private List<TermRecord> generateTerms(String eventId, List<ITerm> terms, Map<String, ISchedulingData> schedulingData)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		Set<TermRecord> previousEventTerms = new HashSet<TermRecord>();
		TermRecord eventTerm, usedTerm = null;
		int step = 60, offset = 0;
		
		for(String event : eventsTermList.keySet())
			if(!event.equals(eventId)) previousEventTerms.addAll(eventsTermList.get(event)); 
		
		for(ITerm term : terms)
		{
			List<TermRecord> rawTermList = schedulingData.get(eventId) != null ? 
					prepareTermData(eventId, schedulingData.get(eventId).getTermData()) :
					prepareTermData(eventId, schedulingData.get(term.getId()).getTermData());
			syncWithPlan(rawTermList, eventId);
		
			sort(rawTermList);
			int index = rawTermList.size();
			rawTermList.add(null);
			rawTermList.add(null);
			
			for(TermRecord previousTerm : previousEventTerms) 
			{
				for(TermRecord termTime : rawTermList)
				{
					if(termTime == null)
						throw new SchedulingException("Nije uspjelo stvaranje plana! \n   Dogadaj: " 
								+ rawTermList.get(0).eventId + "\n   Termin: " + term.getId() + " " + term.getName() + 
								"\n   Razlog: prekratki vremenski raspon");
					
					offset = 0;
					boolean created = false;
					while(true)
					{
						TermRecord[] tempTerms = generateTerm(termTime, offset, previousTerm.getDuration());
						if (tempTerms == null) break;
						
						eventTerm = tempTerms[1];
						
						if(!conflictedStudents(previousTerm.students, eventTerm) && 
								preconditionsSatisfied(eventTerm, getPreconditionTerms(eventTerm.eventId, previousTerm.students)))
						{
							eventTerm.students.addAll(previousTerm.students);
							for(String student : eventTerm.students)
								studentOccupancyList.get(student).add(eventTerm.eventId + "|" + eventTerm.toString());
							rawTermList.set(index, tempTerms[0]);
							eventTerm.termId = term.getId();
							eventTerm.termName = term.getName();
							eventTerm.rawTerms = new ArrayList<TermRecord>(rawTermList);
							result.add(eventTerm);
							usedTerm = previousTerm;
							index++;
							created = true;
							break;
						}
						else
						{
							termTime.setToTime(tempTerms[0].toTime);
							offset += step;
							continue;
						}
					}
					
					if(created) break;
				}
				rawTermList.remove(null);
				List<TermRecord> tempTermList = new ArrayList<TermRecord>(rawTermList);
				for(TermRecord temp : tempTermList)
					if(temp == null || temp.fromTime.equals(temp.toTime)) rawTermList.remove(temp);
				syncWithPlan(rawTermList, eventId);
			}
			previousEventTerms.remove(usedTerm);
			
		}
		
		return result;
	}
	
	//stvara jedan termin iz danog slobodnog termina
	private TermRecord[] generateTerm(TermRecord term, int offset, int duration)
	{
		TermRecord[] result = new TermRecord[2];
		
		if (term.toTime.getAbsoluteTime() - term.fromTime.getAbsoluteTime() < duration) return null;
		else if (term.fromTime.getAbsoluteTime() + duration + offset > term.toTime.getAbsoluteTime()) return null;
		else{
		
			TermRecord tempTerm = new TermRecord(term.eventId, term.room, term.roomCapacity, term.date,
					new TimeStamp(term.fromTime.getAbsoluteTime()+offset),
					new TimeStamp(term.fromTime.getAbsoluteTime()+offset+duration), term.preconditions);
			result[0] = new TermRecord(term.eventId, term.room, term.roomCapacity, term.date,
					new TimeStamp(tempTerm.toTime.getAbsoluteTime()), term.toTime, term.preconditions);
			term.setToTime(new TimeStamp(tempTerm.fromTime.getAbsoluteTime()));
			
			result[1] = tempTerm;
		}
		
		return result;
	}
	
	//sortira listu termina po datumu(m-v), vrmenu pocetka(m-v), duljini(m-v), kapacitetu(v-m)
	private void sort(List<TermRecord> termList)
	{
		for(int i = 0; i < termList.size(); i++){
			boolean switched = false;
			for(int j = 0; j < termList.size() - 1; j++){
				boolean firstNull = termList.get(j) == null && termList.get(j+1) != null;
				if(firstNull){
					TermRecord temp = termList.get(j);
					termList.set(j, termList.get(j+1));
					termList.set(j+1, temp);
					switched = true;
					continue;
				}
				
				
				if(termList.get(j+1) != null){
					boolean after = termList.get(j).date.compareTo(termList.get(j+1).date) > 0 ||
									(termList.get(j).date.equals(termList.get(j+1).date) &&
									termList.get(j).fromTime.after(termList.get(j+1).fromTime));
					
					boolean longer = termList.get(j).date.equals(termList.get(j+1).date) &&
										termList.get(j).fromTime.equals(termList.get(j+1).fromTime) &&
										termList.get(j).toTime.compareTo(termList.get(j+1).toTime) > 0;
										
					boolean smaller = termList.get(j).date.equals(termList.get(j+1).date) &&
										termList.get(j).fromTime.equals(termList.get(j+1).fromTime) &&
										termList.get(j).toTime.equals(termList.get(j+1).toTime) &&
										termList.get(j).roomCapacity < termList.get(j+1).roomCapacity;
					if(after || longer || smaller)
					{
						TermRecord temp = termList.get(j);
						termList.set(j, termList.get(j+1));
						termList.set(j+1, temp);
						switched = true;
					}
				}
			}
			if(switched == false) return;
		}
		
	}
	
	//izbacuje najmanje popunjeni termin i obavlja potrebnu sinkronizaciju
	private int removeTerm(List<TermRecord> resultList, List<TermRecord> termList, Set<String> usedStudents)
	{
		float min = Float.MAX_VALUE;
		TermRecord worstTerm = null;
		
		if(new Random().nextInt(100) < 40)
			worstTerm = resultList.get(new Random().nextInt(resultList.size()));	
		else
			for(TermRecord term : resultList)
				if(term.students.size()/(float)term.roomCapacity < min)
				{
					min = term.students.size()/(float)term.roomCapacity;
					worstTerm = term;
				}
			
		
		for(String student : worstTerm.students)
		{
			usedStudents.remove(student);
			studentOccupancyList.get(student).remove(studentOccupancyList.get(student).size() - 1);
		}
		worstTerm.students.clear();
		returnTermToRawTerms(termList, worstTerm);
		
		resultList.remove(worstTerm);
		return worstTerm.roomCapacity;
	}
	
	private int removeBiggestRoom(List<TermRecord> resultList, List<TermRecord> termList, Set<String> usedStudents)
	{
		int max = 0;
		TermRecord biggestTerm = null;
		
		for(TermRecord term : resultList)
			if(term.roomCapacity > max)
			{
				max = term.roomCapacity;
				biggestTerm = term;
			}
	
		for(String student : biggestTerm.students)
		{
			usedStudents.remove(student);
			studentOccupancyList.get(student).remove(studentOccupancyList.get(student).size() - 1);
		}
		biggestTerm.students.clear();
		returnTermToRawTerms(termList, biggestTerm);
		
		resultList.remove(biggestTerm);
		return biggestTerm.roomCapacity;
	}
	
	//oslobadja termin
	private void returnTermToRawTerms(List<TermRecord> termList, TermRecord term)
	{
		sort(termList);
		TermRecord temp = null;
		int i = 0;
		boolean returned = false;
		for(TermRecord tempTerm : termList)
		{
			if(tempTerm != null && tempTerm.date.equals(term.date) && tempTerm.room.equals(term.room)
					&& tempTerm.toTime.equals(term.fromTime)){
				tempTerm.setToTime(term.toTime);
				temp = tempTerm;
				returned = true;
			}
			else if(tempTerm != null && tempTerm.date.equals(term.date) && tempTerm.room.equals(term.room)
					&& tempTerm.fromTime.equals(term.toTime)){
				if(temp != null)
				{
					temp.setToTime(tempTerm.toTime);
					termList.set(i, null);
				}
				else
					tempTerm.setFromTime(term.fromTime);
				returned = true;
			}
			i++;
		}
		if(!returned)
		{
			term.students.clear();
			termList.add(term);
		}
	}
	
	//zadani termin nastoji popuniti studentima pritom pazeci na zauzeca i preduvjete
	private int connectTermAndStudents(Map<String, List<String>> studentList, TermRecord term,
			List<TermRecord> precTerms, Set<String> usedJmbags) 
			throws SchedulingException
	{
		List<String> jmbags = new ArrayList<String>(studentList.keySet());
		jmbags.removeAll(usedJmbags);
		int added = 0;
		while(!jmbags.isEmpty() && term.roomCapacity != term.students.size())
		{
			String jmbag = jmbags.remove(random.nextInt(jmbags.size()));
			if (!conflictedStudent(studentList.get(jmbag),term) &&	term.roomCapacity > term.students.size())
			{
				if(precTerms != null)
					if(!hasPrecTerm(studentList.get(jmbag), term.preconditions, precTerms))
						continue;
				
				term.students.add(jmbag);
				studentList.get(jmbag).add(term.eventId + "|" + term.toString());
				usedJmbags.add(jmbag);
				added++;
			}
		}
		if(added == 0) throw new SchedulingException();
		
		return added;
	}
	
	//ispituje je li student slobodan za zadani termin
	private boolean conflictedStudent(List<String> occupancy, TermRecord termTime)
	{
		List<TermRecord> termOccupancy = toTerm(occupancy);
		for (TermRecord term : termOccupancy)
		{
			if (term.date.equals(termTime.date))
			{
				if(term.fromTime.afterOrAt(termTime.fromTime) && term.fromTime.before(termTime.toTime) ||
					term.fromTime.beforeOrAt(termTime.fromTime) && term.toTime.after(termTime.fromTime))
					return true;
			}
		}
		
		return false;
	}
	
	//ispituje jesu li studenti slobodni za dani termin
	private boolean conflictedStudents(List<String> students, TermRecord termTime)
	{
		for(String student : students)
			if(conflictedStudent(studentOccupancyList.get(student), termTime)) return true;
		
		return false;
	}
	
	//ispituje je li slobodan zadani temrin
	private boolean conflictedTerm(TermRecord term, List<TermRecord> rawTerms)
	{
		List<TermRecord> tempPlanTermList = rawTerms != null ? rawTerms : getEventRawTerms(term.eventId);
		for (TermRecord rawTerm : tempPlanTermList)
		{
			if (rawTerm.date.equals(term.date) && rawTerm.date.equals(term.date) &&
				rawTerm.fromTime.beforeOrAt(term.fromTime) && rawTerm.toTime.afterOrAt(term.toTime))
					return false;
		}
		
		return true;	
	}
	
	//ispituje je li student prisustvovao prijasnjim terminima
	private boolean hasPrecTerm(List<String> strStudentTerms, List<String> precEvents, List<TermRecord> precTerms)
	{
		int i = 0;
		for(String event : precEvents)
		{
			int differ = 0;
			boolean satisfied = false;
			List<TermRecord> studentTerms = toTerm(strStudentTerms);
			for(TermRecord studentTerm : studentTerms)
			{
				if(studentTerm.eventId.equals(event) && !studentTerm.room.equals("undef"))
				{
					for(TermRecord precTerm : precTerms)
						if(studentTerm.date.equals(precTerm. date.substring(0,10)) && studentTerm.room.equals(precTerm.room)
							&& studentTerm.fromTime.equals(precTerm.fromTime)) satisfied = true;
				}
				else if(!studentTerm.eventId.equals(event)) differ++;
			}
			if(differ == studentTerms.size()) satisfied = true;
			if(satisfied) i++;
		}
		
		if(i == precEvents.size()) return true;
		else return false;
	}
	
	//od slobodnog termina oduzima preduvjet, tako da izbriše sve slobodne termine do (kraj termina + odmak)
	private void subtractPrecondition(List<TermRecord> rawTerms, TermRecord precTerm)
	{		
		for(TermRecord term : rawTerms){
			if(term.date.compareTo(precTerm.date.substring(11,21)) < 0 ||
				(term.date.equals(precTerm.date.substring(11,21)) &&
				term.toTime.beforeOrAt(precTerm.toTime)))
				rawTerms.set(rawTerms.indexOf(term), null);
			else if(term.date.equals(
					precTerm.date.substring(11,21)) &&
				term.fromTime.before(precTerm.toTime) && term.toTime.after(precTerm.toTime))
				rawTerms.get(rawTerms.indexOf(term)).fromTime = new TimeStamp(precTerm.toTime.getAbsoluteTime());	
		}
		
		sort(rawTerms);
		for(int i = rawTerms.size()-1; i >= 0; i--)
			if(rawTerms.get(i) == null) rawTerms.remove(i);
			else break;
	}
	
	//ispituje zadovoljava li zadani termin zadani preduvjet
	private boolean preconditionSatisfied(TermRecord term, TermRecord precTerm)
	{
		if((precTerm.date.substring(11,21).compareTo(term.date) < 0) ||
			(precTerm.date.substring(11,21).equals(term.date) && precTerm.toTime.beforeOrAt(term.fromTime)))	
			return true;
		return false;
	}
	
	//ispituje zadovoljava li zadani termin zadane preduvjete
	private boolean preconditionsSatisfied(TermRecord term, List<TermRecord> precTerms)
	{
		if(precTerms.size() == 0 || precTerms.get(0) == null) return true;
		for(TermRecord precTerm : precTerms)
			if(!preconditionSatisfied(term, precTerm)) return false;
		
		return true;
	}
	
	//vraca listu preduvjeta koje zadovoljava dani termin
	private List<TermRecord> satisfiedPreconditions(TermRecord term, List<TermRecord> preconditions) throws SchedulingException
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		
		int i = 0;
		for(String precEvent : term.preconditions)
		{
			boolean satisfied = false;
			for(TermRecord precTerm : preconditions)
			{
				if(precTerm.eventId.equals(precEvent) && preconditionSatisfied(term, precTerm))
				{
					result.add(precTerm);
					satisfied = true;
				}
			}
			if(satisfied) i++;
		}
		
		if(i != term.preconditions.size()) throw new SchedulingException();
		
		return result;
	}
	
	//ispituje je li moguce za zadane slobodne termine i preduvjete stvoriti ijedan termin
	private void isPossible(List<TermRecord> rawTerms, List<TermRecord> precTerms, int duration)
	{
		TermRecord precTerm = precTerms.get(precTerms.size()-1);
		if(rawTerms.size() == 0)
			throw new SchedulingException("Nije uspjelo stvaranje plana!" + 
					"\n   Razlog: Nije moguce zadovoljiti preduvjete");
		TermRecord rawTerm = rawTerms.get(rawTerms.size()-1);
		if(precTerm.date.substring(11,21).compareTo(rawTerm.date) > 0 || 
				(precTerm.date.substring(11,21).equals(rawTerm.date) &&
				(precTerm.toTime.after(rawTerm.toTime) || 
				rawTerm.toTime.getAbsoluteTime()-precTerm.toTime.getAbsoluteTime() < duration
				))) throw new SchedulingException("Nije uspjelo stvaranje plana! \n   Dogadaj: " 
						+ rawTerms.get(0).eventId + "\n   Razlog: Nije moguce zadovoljiti preduvjete");	
	}
	
	//zamnjena studenata u svojim terminima
	private void switchStudentsWithSelf()
	{
		TermRecord firstTerm, secondTerm;
		int n = 5;
		
		for(String event : eventsTermList.keySet())
		{
			if(random.nextInt()%3 != 0) continue; 
			List<TermRecord> terms = eventsTermList.get(event);
			if(terms.size() >= 2)
			{
				do{
					firstTerm = terms.get(random.nextInt(terms.size()));
					secondTerm = terms.get(random.nextInt(terms.size()));
				}while(firstTerm.equals(secondTerm));
				
				if(secondTerm.students.size() < firstTerm.students.size())
				{
					TermRecord temp = secondTerm;
					secondTerm = firstTerm;
					firstTerm = temp;
				}
				
				Set<String> usedStudents = new HashSet<String>();
				int i = 0, j = 0;
				while(i < firstTerm.students.size()/n && j < firstTerm.students.size())
				{
					String firstStudent = firstTerm.students.get(j);
					String secondStudent = secondTerm.students.get(j);
					j++;
					
					if(!usedStudents.contains(firstStudent) && !usedStudents.contains(secondStudent))
					{
						usedStudents.add(firstStudent);
						usedStudents.add(secondStudent);
						
						if(switchTheseStudents(firstStudent, secondStudent, firstTerm, secondTerm)) i++;
					}
				}
			}
		}
	}
	
	//zamjena studenata sa jednom od najboljih cestica (svojom ili globalnom)
	private void switchStudentsWithBest(Particle bestParticle)
	{
		List<TermRecord> terms = getMatchingTerms(bestParticle);
		int n = 5;
		
		for(int i = 0; i < terms.size()/2; i++)
		{
			TermRecord firstTerm = terms.get(i*2);
			Set<String> thisTermStudents = new HashSet<String>(firstTerm.students);
			Set<String> bestTermStudents = new HashSet<String>(terms.get(i*2 + 1).students);
			bestTermStudents.removeAll(thisTermStudents);
			thisTermStudents.removeAll(bestTermStudents);
			if(thisTermStudents.size() == 0) continue;
			
			int j = 0;
			for(String secondStudent : bestTermStudents)
			{
				int broj = bestTermStudents.size() < 5 ? 1 : bestTermStudents.size()/n; 
				
				TermRecord secondTerm = findStudentTerm(secondStudent, firstTerm.eventId);
				String firstStudent = (String) thisTermStudents.toArray()[random.nextInt(thisTermStudents.size())];
				
				if(switchTheseStudents(firstStudent, secondStudent, firstTerm, secondTerm)) if(++j >= broj) break;
			}
		}
	}
	
	//zamjeni zadane studente u terminima
	private boolean switchTheseStudents(String firstStudent, String secondStudent, TermRecord firstTerm, TermRecord secondTerm)
	{
		List<String> firstStudentTerms = studentOccupancyList.get(firstStudent);
		List<String> secondStudentTerms = studentOccupancyList.get(secondStudent);
		boolean satisfiesPreconditions = true;
		if(eventData.get(firstTerm.eventId).preconditions != null)
		{
			List<TermRecord> firstStudentPreconditions = getPreconditionTerms(firstTerm.eventId, firstStudent);
//			if(secondTerm == null)
//				System.out.println("");
			List<TermRecord> secondStudentPreconditions = getPreconditionTerms(secondTerm.eventId, secondStudent);
			satisfiesPreconditions = preconditionsSatisfied(firstTerm, secondStudentPreconditions) && 
											preconditionsSatisfied(secondTerm, firstStudentPreconditions); 
		}
		
		if(!conflictedStudent(firstStudentTerms, secondTerm) && !conflictedStudent(secondStudentTerms, firstTerm) &&
			satisfiesPreconditions)
		{
			int index =firstStudentTerms.indexOf(firstTerm.eventId + "|" + firstTerm.toString());
//			if(index == -1)
//				System.out.println("");
			TermRecord tempTerm = toTerm(firstStudentTerms.remove(index));
			firstStudentTerms.add(secondTerm.eventId + "|" + secondTerm.toString());
			secondStudentTerms.remove(secondTerm.eventId + "|" + secondTerm.toString());
			secondStudentTerms.add(tempTerm.eventId + "|" + tempTerm.toString());
			
			String tempStudent = firstTerm.students.remove(firstTerm.students.indexOf(firstStudent));
			firstTerm.students.add(secondStudent);
			secondTerm.students.remove(secondStudent);
			secondTerm.students.add(tempStudent);
		
			return true;
		}
		
		return false;
	}
	
	//vraca listu parova svih identicnih termina
	private List<TermRecord> getMatchingTerms(Particle bestParticle)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		
		for(String thisEvent : this.eventsTermList.keySet())
		{
			for(TermRecord bestTerm : bestParticle.eventsTermList.get(thisEvent))
			{
				for(TermRecord thisTerm : this.eventsTermList.get(thisEvent))
				{
					if(thisTerm.date.equals(bestTerm.date) && thisTerm.fromTime.equals(bestTerm.fromTime))
					{
						result.add(thisTerm);
						result.add(bestTerm);
						break;
					}
					if(thisTerm.date.compareTo(bestTerm.date) > 0)
						break;
				}
			}
		}
		
		return result;
	}
	
	//vraca termin u kojem se nalazi student
	private TermRecord findStudentTerm(String jmbag, String eventId)
	{
		for(String strTerm : studentOccupancyList.get(jmbag))
		{
			TermRecord term = toTerm(strTerm);
			if(term.eventId.equals(eventId) && !term.room.equals("undef")) return term;
		}	
		return null;
	}
	
	//vraca termine preduvjeta studenta za dani dogadjaj 
	private List<TermRecord> getPreconditionTerms(String eventId, String student)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		
		if (!eventData.keySet().contains(eventId) || eventData.get(eventId).preconditions == null) 
			result.add(null);
		else
		{
			for(String precondition : eventData.get(eventId).preconditions.keySet())
				for(String strStudentTerm : studentOccupancyList.get(student))
				{
					TermRecord studentTerm = toTerm(strStudentTerm);
					if(studentTerm.eventId.equals(precondition) && !studentTerm.room.equals("undef"))
					{
						TermRecord temp = new TermRecord(studentTerm);
						preparePrecondition(temp, eventData.get(eventId).preconditions.get(precondition));
						result.add(temp);
						break;
					}
				}
		}
		
		return result;
	}
	
	//vraca termine preduvjeta svih studenata termina za dani dogadjaj
	private List<TermRecord> getPreconditionTerms(String eventId, List<String> students)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		
		for(String student : students)
		{
			List<TermRecord> temp = getPreconditionTerms(eventId, student); 
			if(temp.get(0) == null)
			{
				result.add(null);
				break;
			}
			else
				result.addAll(temp);
		}
			
			
		return result;
	}
	
	//vrijednost datuma godina * 12 * 31 + mjesec * 31 + dan
	private int absoluteDate(String date)
	{
		return Integer.parseInt(date.substring(0,4)) * 372 +
		Integer.parseInt(date.substring(5,7)) * 31 +
		Integer.parseInt(date.substring(8,10));
	}

	//pomice slucajno odabrani termin u dogadjaju
	private boolean moveTerm(String eventId)
	{
		List<TermRecord> tempTermList = eventsTermList.get(eventId);
		List<TermRecord> tempPlanTermList = getEventRawTerms(eventId);
		sort(tempTermList);
		sort(tempPlanTermList);
		int i, direction = random.nextInt() % 2 == 0 ? -1 : 1;
		int termNumber = random.nextInt(tempTermList.size());
		TermRecord tempTerm = tempTermList.get(termNumber), tempRawTerm;
		String date;
		
		if(tempTerm.rawTerms != null)
		{
			syncWithPlan(tempTerm.rawTerms, tempTerm.eventId);
			tempPlanTermList = tempTerm.rawTerms;
		}
		
		if(direction == -1)
		{
			if(tempPlanTermList.get(0).date.equals(tempTerm.date)) return false;
			else
			{
				i = 0;
				while(i < tempPlanTermList.size() - 2 && tempPlanTermList.get(i+1).date.compareTo(tempTerm.date) < 0) i++;
				date = tempPlanTermList.get(i).date;
			}
		}
		else
		{
			i = tempPlanTermList.size()-1;
			if(tempPlanTermList.get(i).date.equals(tempTerm.date)) return false;
			else
			{
				while(i > 0 && tempPlanTermList.get(i-1).date.compareTo(tempTerm.date) > 0) i--;
				date = tempPlanTermList.get(i).date;
				while(i < tempPlanTermList.size() - 2 && tempPlanTermList.get(i+1).date.equals(date)) i++;
			}
		}
		tempRawTerm = tempPlanTermList.get(i);
		
		int offset = 0, korak = 30;
		while(tempRawTerm.date.equals(date))
		{
			TermRecord[] temp = generateTerm(tempRawTerm, offset, tempTerm.getDuration());
			if(temp == null)
			{
				offset = 0;
				i--;
				if(i < 0) break;
				tempRawTerm = tempPlanTermList.get(i);
			}
			else
			{
				TermRecord newTerm = temp[1];
				if(newTerm.roomCapacity < tempTerm.students.size())
				{
					tempRawTerm.setToTime(temp[0].toTime);
					offset = 0;
					i--;
					if(i < 0) break;
					tempRawTerm = tempPlanTermList.get(i);
					continue;
				}
				newTerm.eventId = eventId;
				if(!conflictedStudents(tempTerm.students, newTerm) && preconditionsSatisfied(newTerm, getPreconditionTerms(eventId, tempTerm.students))
					&& precedes(newTerm, isPreconditionTo(newTerm.eventId)))
				{
					newTerm.students = new ArrayList<String>(tempTerm.students);
					newTerm.eventId = tempTerm.eventId;
					newTerm.preconditions = tempTerm.preconditions;
					
					for(String student : newTerm.students)
					{
						studentOccupancyList.get(student).remove(tempTerm.eventId + "|" + tempTerm.toString());
						studentOccupancyList.get(student).add(newTerm.eventId + "|" + newTerm.toString());
					}
					
					tempTermList.set(termNumber, newTerm);
					if(!temp[0].fromTime.equals(temp[0].toTime))
						planTermList.add(temp[0]);
					if(tempRawTerm.fromTime.equals(tempRawTerm.toTime))
						planTermList.remove(tempRawTerm);
					returnTermToRawTerms(planTermList, tempTerm);
					planTermList.remove(null);

					return true;
				}
				else
				{
					tempRawTerm.setToTime(temp[0].toTime);
					offset += korak;
				}
			}
		}
		
		return false;
	}	
	
	//u zadanom dogadjaju zamjeni jedan termin s terminom iz dogadjaja druge cestice
	private boolean copyTermFromParticle(Particle particle, String eventId) throws SchedulingException
	{
		List<TermRecord> myEventTermList = new ArrayList<TermRecord>(this.eventsTermList.get(eventId));
		List<TermRecord> particleEventTermList = new ArrayList<TermRecord>(particle.eventsTermList.get(eventId));
		TermRecord termToCopy, termToChange, resetTerm;
		int n;
		
		sort(myEventTermList);
		sort(particleEventTermList);
		
		do{
			n = random.nextInt(myEventTermList.size());
			termToCopy = particleEventTermList.remove(n);
			termToChange = myEventTermList.remove(n);
			if(termToChange.rawTerms != null)
				syncWithPlan(termToChange.rawTerms, termToChange.eventId);
			if(termToCopy.date.equals(termToChange.date) && termToCopy.fromTime.equals(termToChange.fromTime)
				&& termToCopy.room.equals(termToChange.room)) break;
			if(myEventTermList.size() == 0)
				return false;
		}while(conflictedTerm(termToCopy, termToChange.rawTerms) || conflictedStudents(termToCopy.students, termToCopy) ||
				!preconditionsSatisfied(termToCopy, getPreconditionTerms(eventId, termToCopy.students)) ||
				!precedes(termToCopy, isPreconditionTo(termToChange.eventId)));
		
		myEventTermList = this.eventsTermList.get(eventId);
		resetTerm = new TermRecord(termToChange);
		
		termToChange.setDate(termToCopy.date);
		termToChange.setFromTime(new TimeStamp(termToCopy.fromTime.getAbsoluteTime()));
		termToChange.setToTime(new TimeStamp(termToCopy.toTime.getAbsoluteTime()));
		termToChange.room = termToCopy.room;
		termToChange.roomCapacity = termToCopy.roomCapacity;
		termToChange.students = new ArrayList<String>(termToCopy.students);
		
		for(String student : resetTerm.students)
			studentOccupancyList.get(student).remove(resetTerm.eventId + "|" + resetTerm.toString());
		
		Map<String, TermRecord> reset = new HashMap<String, TermRecord>();
		Set<String> sharedStudents = new HashSet<String>(resetTerm.students);
		sharedStudents.retainAll(new HashSet<String>(termToCopy.students));
		resetTerm.students.removeAll(sharedStudents);
		
		for(String student : termToCopy.students)
		{
			if(!sharedStudents.contains(student))
			{
				TermRecord tempTerm = findStudentTerm(student, eventId);
				reset.put(student, tempTerm);
				tempTerm.students.remove(student);
				studentOccupancyList.get(student).remove(tempTerm.eventId + "|" + tempTerm.toString());
			}
			else
				reset.put(student, resetTerm);
			studentOccupancyList.get(student).add(termToChange.eventId + "|" + termToChange.toString());
		}
		
		while(!resetTerm.students.isEmpty())
		{
			String student = resetTerm.students.get(0);
			boolean added = false;
			for(TermRecord term : myEventTermList)
			{
				if(term.students.size() < term.roomCapacity	&& !conflictedStudent(studentOccupancyList.get(student), term)
					&& preconditionsSatisfied(term, getPreconditionTerms(eventId, student)))
				{
					term.students.add(student);
					studentOccupancyList.get(student).add(term.eventId + "|" + term.toString());
					reset.put(student, term);
					resetTerm.students.remove(0);
					added = true;
					break;
				}
			}
			if(!added)
			{
				for(String tempStudent : reset.keySet())
				{
					if(termToCopy.students.contains(tempStudent))
					{
						studentOccupancyList.get(tempStudent).remove(studentOccupancyList.get(tempStudent).size()-1);
						studentOccupancyList.get(tempStudent).add(reset.get(tempStudent).eventId + "|" + reset.get(tempStudent).toString());
						reset.get(tempStudent).students.add(tempStudent);
					}
					else
					{
						reset.get(tempStudent).students.remove(tempStudent);
						studentOccupancyList.get(tempStudent).remove(studentOccupancyList.get(tempStudent).size()-1);
						resetTerm.students.add(tempStudent);
					}
				}
				for(String tempStudent : resetTerm.students)
					if(!sharedStudents.contains(tempStudent))
						studentOccupancyList.get(tempStudent).add(resetTerm.eventId + "|" + resetTerm.toString());
				
				termToChange.setDate(resetTerm.date);
				termToChange.setFromTime(resetTerm.fromTime);
				termToChange.setToTime(resetTerm.toTime);
				termToChange.room = resetTerm.room;
				termToChange.roomCapacity = resetTerm.roomCapacity;
				termToChange.students = resetTerm.students;
				
				return false;
			}
		}
		
		List<TermRecord> tempPlanTermList = getEventRawTerms(eventId);
		for(TermRecord term : tempPlanTermList)
		{
			if(term.date.equals(termToChange.date) && term.fromTime.beforeOrAt(termToChange.fromTime) &&
				term.toTime.afterOrAt(termToChange.toTime))
			{
				if(!term.toTime.equals(termToChange.toTime))
					planTermList.add(new TermRecord(term.eventId, term.room, term.roomCapacity, term.date,
								new TimeStamp(termToChange.toTime.getAbsoluteTime()), term.toTime, term.preconditions));
				if(!term.fromTime.equals(termToChange.fromTime))
					term.setToTime(new TimeStamp(termToChange.fromTime.getAbsoluteTime()));
				else
					planTermList.remove(term);
				returnTermToRawTerms(planTermList, resetTerm);
				planTermList.remove(null);
				break;
			}
		}

		return true;
	}
	
	//iz planTermList-a izvlaci samo termine vezane uz zadani dogadjaj
	private List<TermRecord> getEventRawTerms(String eventId)
	{
		Event tempEventData = eventData.get(eventId);
		List<TermRecord> result = new ArrayList<TermRecord>();
		
		planTermList.remove(null);
		for(TermRecord term : planTermList)
		{
			for(ITimeParameter time : tempEventData.time)
			{
				if(term.date.compareTo(time.getFromDate().toString()) >= 0 &&
					term.date.compareTo(time.getToDate().toString()) <= 0)
				{
					result.add(term);
					break;
				}
			}
		}
		sort(result);
		
		return result;
	}	

	//danom terminu mjena sobu (ako je moguce)
	public void changeRoom(TermRecord term)
	{
		List<TermRecord> tempPlanTermList;
		
		try
		{
			tempPlanTermList = getEventRawTerms(term.eventId);
		}
		catch(Exception e)
		{
			return;
		}
		
		for(TermRecord tempTerm : tempPlanTermList)
			if(tempTerm.date.equals(term.date) && tempTerm.fromTime.beforeOrAt(term.fromTime)
					&& tempTerm.toTime.afterOrAt(term.toTime) && tempTerm.roomCapacity < term.roomCapacity &&
					tempTerm.roomCapacity >= term.students.size())
			{
				changeRoom(term, tempTerm);
				break;
			}
	}
	
	//termin term prebacuje u sobu termina newTerm
	private void changeRoom(TermRecord term, TermRecord newTerm)
	{
		if(!term.toTime.equals(newTerm.toTime))
			planTermList.add(new TermRecord(newTerm.eventId, newTerm.room, newTerm.roomCapacity, newTerm.date,
						new TimeStamp(term.toTime.getAbsoluteTime()), newTerm.toTime, term.preconditions));
		if(!term.fromTime.equals(newTerm.fromTime))
			newTerm.toTime = new TimeStamp(term.fromTime.getAbsoluteTime());
		else 
			planTermList.remove(newTerm);
		
		TermRecord oldTerm  = new TermRecord(term);
		returnTermToRawTerms(planTermList, oldTerm);
		planTermList.remove(null);
		term.room = newTerm.room;
		term.roomCapacity = newTerm.roomCapacity;
		for(String student : term.students)
		{
			studentOccupancyList.get(student).remove(oldTerm.eventId + "|" + oldTerm.toString());
			studentOccupancyList.get(student).add(term.eventId + "|" + term.toString());
		}
	}	
	
	private TermRecord toTerm(String strTerm)
	{
		String[] term = strTerm.split("\\|");
		String eventId = term[0];
		TermRecord temp = new TermRecord(term[0], term[1]);
		if(temp.room.equals("undef"))
			return temp;
		else
		{
			int index = eventsTermList.get(eventId).indexOf(temp);
			if(index == -1)
				System.out.println("");
			return eventsTermList.get(eventId).get(index);
		}
	}
	
	private List<TermRecord> toTerm(List<String> strTerms)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		for(String strTerm : strTerms)
			result.add(toTerm(strTerm));
		
		return result;
	}
	
	private List<TermRecord> isPreconditionTo(String eventId)
	{
		List<TermRecord> result = new ArrayList<TermRecord>();
		Set<String> events = new HashSet<String>();
		
		for(String event : eventData.keySet())
		{
			if(eventData.get(event).preconditions != null && eventData.get(event).preconditions.keySet().contains(eventId))
				events.add(event);
		}
		
		if(events.size() == 0) return null;
		
		for(String event : events)
			result.addAll(eventsTermList.get(event));
		
		return result;
	}
	
	private boolean precedes(TermRecord precTerm, List<TermRecord> terms)
	{
		if(terms == null) return true;
		
		TermRecord tempPrecTerm;
		for(TermRecord term : terms)
		{
			tempPrecTerm = new TermRecord(precTerm);
			preparePrecondition(tempPrecTerm, eventData.get(term.eventId).preconditions.get(precTerm.eventId));
			if(tempPrecTerm.date.substring(11,21).compareTo(term.date) > 0 || (tempPrecTerm.date.substring(11,21).equals(term.date) 
				&& tempPrecTerm.toTime.after(term.fromTime)))
				return false;
		}
		
		return true;
	}
	
	private class Event
	{
		String name;
		Map<String, String> preconditions = new HashMap<String, String>();
		List<ITimeParameter> time;
		List<ILocationParameter> location;
		
		public Event(String name, Set<IPrecondition> preconditions, List<ITimeParameter> time, List<ILocationParameter> location)
		{
			this.name = name;
			this.time = new ArrayList<ITimeParameter>(time);
			this.location = new ArrayList<ILocationParameter>(location);
			if(preconditions != null)
				for(IPrecondition precondition : preconditions)
					this.preconditions.put(precondition.getEvent().getId(), precondition.getTimeDistance());
			else
				this.preconditions = null;
		}
	
		public Event(Event event)
		{
			name = event.name;
			preconditions = event.preconditions;
			time = event.time;
			location = event.location;
		}
	}

}