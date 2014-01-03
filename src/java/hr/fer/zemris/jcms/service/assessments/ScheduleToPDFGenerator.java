package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.model.Assessment;
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
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

public class ScheduleToPDFGenerator {

	private PDFProvider p;

	public ScheduleToPDFGenerator(OutputStream os) throws IOException {
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
			ispisiRaspored(assessment, room, lista);
		}
	
		p.close();

	}

	private void ispisiRaspored(Assessment assessment, AssessmentRoom room, List<UserGroup> students) {
		p.nextPage();
		
        drawHeaderRaspored(assessment);

        double y = 16;
        double rh = 7;

        int rbr = 0;
        
        for(UserGroup student : students) {
			
	        y += rh;
	        if(y > p.ch-rh) {
	        	p.nextPage();
	        	drawHeaderRaspored(assessment);
	            y = 16 + rh;
	        }
	    
	        rbr++;
	        
	        p.pdf.newPath();
	        p.pdf.setlinewidth(1);
	        p.pdf.drawBox(0, y, p.cw, rh);
	        p.pdf.moveto(50, y); p.pdf.lineto(50, y+rh);
	        p.pdf.stroke();

	        p.pdf.begintext();
	        p.pdf.setfont(p.times,12);
	        p.pdf.showText(student.getUser().getJmbag(),PdfContentByte.ALIGN_LEFT,(float)(1),(float)(y+rh-2));
	        p.pdf.showText(room.getRoom().getName(),PdfContentByte.ALIGN_LEFT,(float)(51),(float)(y+rh-2));
	        p.pdf.endtext();

		}
	}

	private void drawHeaderRaspored(Assessment assessment) {
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
        p.pdf.showText("JMBAG",PdfContentByte.ALIGN_LEFT,(float)(1),(float)(16+6));
        p.pdf.showText("Dvorana",PdfContentByte.ALIGN_LEFT,(float)(51),(float)(16+6));
        p.pdf.endtext();
	}

	private String prepLine1(Assessment assessment) {
		if(assessment.getEvent()==null) {
			return assessment.getName();
		} else {
			return assessment.getName() + " (" + DateUtil.dateToString(assessment.getEvent().getStart())+")";
		}
	}
	
	private String prepLine2(Assessment assessment) {
		return "AG " + assessment.getCourseInstance().getYearSemester().getAcademicYear() + ", " + assessment.getCourseInstance().getYearSemester().getSemester()+" semestar";
	}	
}
