package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentAssistantSchedule;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.pdf.PDFDocumentX;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ListsGenerator {

	private PDFProvider p;

	public ListsGenerator(OutputStream os) throws IOException {
		p = new PDFProvider(os);
	}
	
	static class PDFProvider {
		public PDFDocumentX pdf;
		public BaseFont times;
		public Document d;
		public double cw;
		public double ch;
		
		private int pageNo = 0;
		
		public PDFProvider(OutputStream os) throws IOException {
			resolveFontPaths();
			if(fontRoot==null) {
				throw new IOException("Fonts not found.");
			}
			try {
				times = BaseFont.createFont(new File(fontRoot,"times.ttf").toString(), "ISO-8859-2", BaseFont.EMBEDDED);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
	        d = new Document(PageSize.A4);

	        PdfWriter writer = null;
	        try {
	            writer = PdfWriter.getInstance(d, os);
	        } catch(Exception ex) {
	        }
	        d.open();
	        
	        pdf = PDFDocumentX.newInstance(d,writer);
			
	        cw = pdf.getClientWidth();
	        ch = pdf.getClientHeight();
		}

		private File fontRoot = null;
		
		private void resolveFontPaths() {
			URI uri;
			try {
				uri = this.getClass().getClassLoader().getResource("fonts/times.ttf").toURI();
			} catch (URISyntaxException e) {
				return;
			}
			fontRoot = new File(uri).getParentFile();
		}

		public void newList() {
			pageNo++;
			if(pageNo==1) return;
			if((pageNo%2) == 0) {
				// ubaci jednu praznu stranicu
	        	pdf.showpage();
	        	pdf.newPath();
	        	pdf.moveto(0, 0);
	        	pdf.lineto(cw, 0);
	        	pdf.stroke();
				pageNo++;
			}
			// odglumi nextPage()
        	pdf.showpage();
		}
		
		public void nextPage() {
			pageNo++;
        	pdf.showpage();
		}
		
		public void close() {
			d.close();
		}
	}

	public void close() {
		p.close();
	}
	
	public void generateLists(Assessment assessment) {
		List<AssessmentRoom> rooms = new ArrayList<AssessmentRoom>(assessment.getRooms());
		Iterator<AssessmentRoom> it = rooms.iterator();
		while(it.hasNext()) {
			AssessmentRoom room = it.next();
			if(!room.isTaken()) it.remove();
		}
		Collections.sort(rooms, StringUtil.ASSESSMENTROOM_COMPARATOR);
		for(AssessmentRoom room : rooms) {
			List<UserGroup> lista;
			if(room.getGroup()==null) {
				lista = new ArrayList<UserGroup>();
			} else {
				lista = new ArrayList<UserGroup>(room.getGroup().getUsers());
			}
			Collections.sort(lista, new Comparator<UserGroup>() {
				@Override
				public int compare(UserGroup o1, UserGroup o2) {
					return o1.getPosition() - o2.getPosition();
				}
			});
			ispisiDvoranuMulti(assessment, room, lista);
		}
	
		ispisiTablicuVracanjaMulti(assessment, rooms);
		
		ispisiTablicuAsistenataMulti(assessment);
		
		ispisiTablicuStudenataSumarnoMulti(assessment, rooms);

		p.close();

	}

	private void ispisiTablicuStudenataSumarnoMulti(Assessment assessment, List<AssessmentRoom> rooms) {
		p.newList();
		
        drawHeaderTSSMulti(assessment);

        double y = 16;
        double rh = 7;

        int rbr = 0;
        
        for(AssessmentRoom room : rooms) {
			
        	UserGroup firstUser = null;
        	UserGroup lastUser = null;
        	
        	if(room.getGroup()!=null) {
        		int gs = room.getGroup().getUsers().size();
        		if(gs>0) {
        			List<UserGroup> list = new ArrayList<UserGroup>(room.getGroup().getUsers());
        			firstUser = Collections.min(list, StringUtil.USER_GROUP_COMPARATOR1);
        			lastUser = Collections.max(list, StringUtil.USER_GROUP_COMPARATOR1);
        		}
        	}
        	
	        y += rh;
	        if(y > p.ch-rh) {
	        	p.nextPage();
	            drawHeaderTSSMulti(assessment);
	            y = 16 + rh;
	        }
	    
	        rbr++;
	        
	        p.pdf.newPath();
	        p.pdf.setlinewidth(1);
	        p.pdf.drawBox(0, y, p.cw, rh);
	        p.pdf.moveto(10, y); p.pdf.lineto(10, y+rh);
	        p.pdf.moveto(37, y); p.pdf.lineto(37, y+rh);
	        //p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);
	        p.pdf.moveto(95, y); p.pdf.lineto(95, y+rh);
	        p.pdf.moveto(160, y); p.pdf.lineto(160, y+rh);
	        p.pdf.stroke();

	        p.pdf.begintext();
	        p.pdf.setfont(p.times,12);
	        p.pdf.showText(""+rbr+".",PdfContentByte.ALIGN_RIGHT,(float)(8),(float)(y+rh-2));
	        p.pdf.showText(room.getRoom().getName(),PdfContentByte.ALIGN_RIGHT,(float)(35),(float)(y+rh-2));
	        String text1 = firstUser==null ? "-" : (firstUser.getUser().getLastName()+", "+firstUser.getUser().getFirstName());
	        p.pdf.showText(text1,PdfContentByte.ALIGN_LEFT,(float)(40),(float)(y+rh-2));
	        String text2 = lastUser==null ? "-" : (lastUser.getUser().getLastName()+", "+lastUser.getUser().getFirstName());
	        //p.pdf.showText(text2,PdfContentByte.ALIGN_LEFT,(float)(105),(float)(y+rh-2));
	        p.pdf.showText(text2,PdfContentByte.ALIGN_LEFT,(float)(100),(float)(y+rh-2));
	        p.pdf.showText(String.valueOf(room.getGroup()==null || room.getGroup().getUsers()==null ? 0 : room.getGroup().getUsers().size()),PdfContentByte.ALIGN_LEFT,(float)(162),(float)(y+rh-2));
	        p.pdf.endtext();

		}
	}
	
	private void drawHeaderTSSMulti(Assessment assessment) {
		String line1 = prepLine1(assessment);
		String line2 = prepLine2(assessment);

		p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 0, p.cw, 14);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,14);
        p.pdf.showText(line1,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(5));
        p.pdf.showText(line2,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(12));
        p.pdf.endtext();

        p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 16, p.cw, 8);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,12);
        p.pdf.showText("RBr.",PdfContentByte.ALIGN_CENTER,(float)(5),(float)(16+6));
        p.pdf.showText("Dvorana",PdfContentByte.ALIGN_CENTER,(float)(23),(float)(16+6));
        p.pdf.showText("Od studenta",PdfContentByte.ALIGN_LEFT,(float)(40),(float)(16+6));
        //p.pdf.showText("Do studenta",PdfContentByte.ALIGN_LEFT,(float)(105),(float)(16+6));
        p.pdf.showText("Do studenta",PdfContentByte.ALIGN_LEFT,(float)(100),(float)(16+6));
        p.pdf.showText("Broj",PdfContentByte.ALIGN_LEFT,(float)(162),(float)(16+6));
        p.pdf.endtext();
	}
	
	private void ispisiTablicuVracanjaMulti(Assessment assessment, List<AssessmentRoom> rooms) {
		p.newList();
		
        drawHeaderTVMulti(assessment);

        double y = 16;
        double rh = 7;

        int rbr = 0;
        
        Map<AssessmentRoom,int[]> brojAsistenata = new HashMap<AssessmentRoom, int[]>(rooms.size());
        
        for(AssessmentAssistantSchedule s : assessment.getAssistantSchedule()) {
        	if(s.getRoom()==null) continue;
        	int[] broj = brojAsistenata.get(s.getRoom());
        	if(broj==null) {
        		broj = new int[] {0};
        		brojAsistenata.put(s.getRoom(), broj);
        	}
        	broj[0]++;
        }
        for(AssessmentRoom room : rooms) {
			//System.out.println("TV: "+room.getName());
			//List<Student> lista = exam.getSchedule().get(room).getStudents();
			
	        y += rh;
	        if(y > p.ch-rh) {
	        	p.nextPage();
	            drawHeaderTVMulti(assessment);
	            y = 16 + rh;
	        }
	    
	        rbr++;
	        
	        p.pdf.newPath();
	        p.pdf.setlinewidth(1);
	        p.pdf.drawBox(0, y, p.cw, rh);
	        p.pdf.moveto(10, y); p.pdf.lineto(10, y+rh);
	        p.pdf.moveto(37, y); p.pdf.lineto(37, y+rh);
	        //p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);

	        p.pdf.moveto(70, y); p.pdf.lineto(70, y+rh);
	        p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);
	        p.pdf.moveto(141, y); p.pdf.lineto(141, y+rh);
	        p.pdf.stroke();

	        p.pdf.begintext();
	        p.pdf.setfont(p.times,12);
	        p.pdf.showText(""+rbr+".",PdfContentByte.ALIGN_RIGHT,(float)(8),(float)(y+rh-2));
	        p.pdf.showText(room.getRoom().getName(),PdfContentByte.ALIGN_RIGHT,(float)(35),(float)(y+rh-2));
	        p.pdf.showText(""+(room.getGroup()==null ? 0 : room.getGroup().getUsers().size()),PdfContentByte.ALIGN_LEFT,(float)(50),(float)(y+rh-2));
	        p.pdf.showText(brojAsistenata.get(room)==null ? "0" : String.valueOf(brojAsistenata.get(room)[0]),PdfContentByte.ALIGN_RIGHT,(float)(87),(float)(y+rh-2));
	        p.pdf.endtext();

		}
	}

	private void drawHeaderTVMulti(Assessment assessment) {
		String line1 = prepLine1(assessment);
		String line2 = prepLine2(assessment);

		p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 0, p.cw, 14);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,14);
        p.pdf.showText(line1,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(5));
        p.pdf.showText(line2,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(12));
        p.pdf.endtext();

        p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 16, p.cw, 8);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,12);
        p.pdf.showText("RBr.",PdfContentByte.ALIGN_CENTER,(float)(5),(float)(16+6));
        p.pdf.showText("Dvorana",PdfContentByte.ALIGN_CENTER,(float)(23),(float)(16+6));
        p.pdf.showText("Broj studenata",PdfContentByte.ALIGN_LEFT,(float)(40),(float)(16+6));
        p.pdf.showText("Broj čuvara",PdfContentByte.ALIGN_LEFT,(float)(75),(float)(16+6));
        p.pdf.showText("Ispunjeno obrazaca",PdfContentByte.ALIGN_LEFT,(float)(105),(float)(16+6));
        p.pdf.showText("Broj prijava",PdfContentByte.ALIGN_CENTER,(float)(135+(p.cw-135)/2),(float)(16+6));
        p.pdf.endtext();
	}

	private void ispisiDvoranuMulti(Assessment assessment, AssessmentRoom room, List<UserGroup> lista) {
		p.newList();
		
        drawHeaderMulti(assessment, room);

        double y = 16;
        double rh = 7;

        int redniBroj = 0;
        for(UserGroup student : lista) {
			//System.out.println(""+room.getName()+" "+student);
			
	        y += rh;
	        if(y > p.ch-rh) {
	        	p.nextPage();
	            drawHeaderMulti(assessment, room);
	            y = 16 + rh;
	        }
	        
	        redniBroj++;
	        p.pdf.newPath();
	        p.pdf.setlinewidth(1);
	        p.pdf.drawBox(0, y, p.cw, rh);
	        p.pdf.moveto(10, y); p.pdf.lineto(10, y+rh);
	        p.pdf.moveto(37, y); p.pdf.lineto(37, y+rh);
	        //p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);

	        p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);
	        p.pdf.moveto(120, y); p.pdf.lineto(120, y+rh);
	        p.pdf.stroke();

	        p.pdf.begintext();
	        p.pdf.setfont(p.times,12);
	        p.pdf.showText(String.valueOf(redniBroj)+".",PdfContentByte.ALIGN_RIGHT,(float)(8),(float)(y+rh-2));
	        p.pdf.showText(student.getUser().getJmbag(),PdfContentByte.ALIGN_RIGHT,(float)(35),(float)(y+rh-2));
	        p.pdf.showText(student.getUser().getLastName()+", "+student.getUser().getFirstName(),PdfContentByte.ALIGN_LEFT,(float)(40),(float)(y+rh-2));
	        p.pdf.endtext();

		}
	}

	private void drawHeaderMulti(Assessment assessment, AssessmentRoom room) {

		String line1 = prepLine1(assessment);
		String line2 = prepLine2(assessment);

		p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 0, p.cw-30, 14);
        p.pdf.drawBox(p.cw-30, 0, 30, 14);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,14);
        p.pdf.showText(line1,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth()-30)/2),(float)(5));
        p.pdf.showText(line2,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth()-30)/2),(float)(12));
        p.pdf.showText("Dvorana",PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth()-30) + 30/2),(float)(5));
        p.pdf.showText(room.getRoom().getName(),PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth()-30) + 30/2),(float)(12));
        p.pdf.endtext();

        p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 16, p.cw, 8);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,12);
        p.pdf.showText("RBr.",PdfContentByte.ALIGN_CENTER,(float)(5),(float)(16+6));
        p.pdf.showText("JMBAG",PdfContentByte.ALIGN_CENTER,(float)(23),(float)(16+6));
        p.pdf.showText("Prezime, Ime",PdfContentByte.ALIGN_LEFT,(float)(40),(float)(16+6));
        p.pdf.showText("Prisutan",PdfContentByte.ALIGN_LEFT,(float)(102),(float)(16+6));
        p.pdf.showText("Komentar",PdfContentByte.ALIGN_LEFT,(float)(122),(float)(16+6));
        p.pdf.endtext();
	}

	private void ispisiTablicuAsistenataMulti(Assessment assessment) {
		p.newList();
		
        drawHeaderTAMulti(assessment);
        List<AssessmentAssistantSchedule> asistenti = new ArrayList<AssessmentAssistantSchedule>(assessment.getAssistantSchedule());
        Collections.sort(asistenti, new Comparator<AssessmentAssistantSchedule>() {
			@Override
			public int compare(AssessmentAssistantSchedule o1,
					AssessmentAssistantSchedule o2) {
				return StringUtil.USER_COMPARATOR.compare(o1.getUser(), o2.getUser());
			}
		});
        double y = 16;
        double rh = 7;

        int rbr = 0;

        for(AssessmentAssistantSchedule asistent : asistenti) {
			
	        y += rh;
	        if(y > p.ch-rh) {
	        	p.nextPage();
	            drawHeaderTAMulti(assessment);
	            y = 16 + rh;
	        }
	    
	        rbr++;
	        
	        p.pdf.newPath();
	        p.pdf.setlinewidth(1);
	        p.pdf.drawBox(0, y, p.cw, rh);
	        p.pdf.moveto(10, y); p.pdf.lineto(10, y+rh);
	        p.pdf.moveto(53, y); p.pdf.lineto(53, y+rh);
	        //p.pdf.moveto(100, y); p.pdf.lineto(100, y+rh);

	        p.pdf.moveto(83, y); p.pdf.lineto(83, y+rh);
	        p.pdf.moveto(118, y); p.pdf.lineto(118, y+rh);
	        p.pdf.stroke();

	        p.pdf.begintext();
	        p.pdf.setfont(p.times,12);
	        p.pdf.showText(""+rbr+".",PdfContentByte.ALIGN_RIGHT,(float)(8),(float)(y+rh-2));
	        p.pdf.showText(asistent.getUser().getLastName()+", "+asistent.getUser().getFirstName(),PdfContentByte.ALIGN_LEFT,(float)(12),(float)(y+rh-2));
	        AssessmentRoom assistantRoom = asistent.getRoom();
	        if(assistantRoom!=null) {
		        p.pdf.showText(assistantRoom.getRoom().getName(),PdfContentByte.ALIGN_CENTER,(float)((53+83)/2.0),(float)(y+rh-2));
	        }
	        p.pdf.endtext();

		}
	}

	private void drawHeaderTAMulti(Assessment assessment) {
		String line1 = prepLine1(assessment);
		String line2 = prepLine2(assessment);

		p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 0, p.cw, 14);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,14);
        p.pdf.showText(line1,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(5));
        p.pdf.showText(line2,PdfContentByte.ALIGN_CENTER,(float)((p.pdf.getClientWidth())/2),(float)(12));
        p.pdf.endtext();

        p.pdf.newPath();
        p.pdf.setlinewidth(1);
        p.pdf.drawBox(0, 16, p.cw, 8);
        p.pdf.stroke();

        p.pdf.begintext();
        p.pdf.setfont(p.times,12);
        p.pdf.showText("RBr.",PdfContentByte.ALIGN_CENTER,(float)(5),(float)(16+6));
        p.pdf.showText("Čuvar",PdfContentByte.ALIGN_CENTER,(float)((12+53)/2.0),(float)(16+6));  // 12
        p.pdf.showText("Dvorana",PdfContentByte.ALIGN_CENTER,(float)((53+83)/2.0),(float)(16+6)); // 50
        p.pdf.showText("Potpis",PdfContentByte.ALIGN_CENTER,(float)((83+118)/2.0),(float)(16+6)); // 80
        p.pdf.showText("Zabilješka",PdfContentByte.ALIGN_CENTER,(float)((118+p.cw)/2.0),(float)(16+6)); // 110
        p.pdf.endtext();
	}

	private String prepLine1(Assessment assessment) {
		return assessment.getCourseInstance().getCourse().getName()+" ("+assessment.getCourseInstance().getCourse().getIsvuCode()+")";
		//if(assessment.getEvent()==null) {
		//	return assessment.getName();
		//} else {
		//	return assessment.getName() + " (" + DateUtil.dateToString(assessment.getEvent().getStart())+")";
		//}
	}
	
	private String prepLine2(Assessment assessment) {
		String firstPart = assessment.getName();
		if(assessment.getEvent()!=null) {
			firstPart = assessment.getName() + " (" + DateUtil.dateToString(assessment.getEvent().getStart())+")";
		}
		return firstPart+", AG " + assessment.getCourseInstance().getYearSemester().getAcademicYear() + ", " + assessment.getCourseInstance().getYearSemester().getSemester()+" semestar";
	}	
}
