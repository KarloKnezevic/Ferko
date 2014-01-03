package hr.fer.zemris.jcms.service.assessments;

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

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.pdf.PDFDocumentX;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

public class FormGenerator {
	private PDFDocumentX pdf;
	private PdfContentByte cb;
	private BaseFont times;
	private BaseFont timesbd;
	private Document d;
	private double cw;
	private double ch;
	private int pageNo = 0;

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

	public FormGenerator(OutputStream os) throws IOException {
		resolveFontPaths();
		if(fontRoot==null) {
			throw new IOException("Fonts not found.");
		}
		try {
			times = BaseFont.createFont(new File(fontRoot,"times.ttf").toString(), "ISO-8859-2", BaseFont.EMBEDDED);
			timesbd = BaseFont.createFont(new File(fontRoot,"timesbd.ttf").toString(), "ISO-8859-2", BaseFont.EMBEDDED);
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
        cb = writer.getDirectContent();
	}

	public void close() {
		d.close();
	}

	private void nextPage() {
		pageNo++;
		if(pageNo==1) return;
		pdf.showpage();
	}
	
	public void generateForms(Assessment assessment, Assessment scheduleAssessment) {
		
		generateForm(assessment, scheduleAssessment, null, null, -1);
		List<AssessmentRoom> rooms = new ArrayList<AssessmentRoom>(scheduleAssessment.getRooms());
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
			int redni_broj = 0;
			for(UserGroup ug : lista) {
				redni_broj++;
				generateForm(assessment, scheduleAssessment, room, ug.getUser(), redni_broj);
			}
		}
	}

	private String prepLine1(Assessment assessment) {
		return assessment.getCourseInstance().getCourse().getName()+" ("+assessment.getCourseInstance().getCourse().getIsvuCode()+")";
	}
	private String prepLine2(Assessment assessment) {
		String firstPart = assessment.getName();
		if(assessment.getEvent()!=null) {
			firstPart = assessment.getName() + " (" + DateUtil.dateToString(assessment.getEvent().getStart())+")";
		}
		return firstPart+", AG " + assessment.getCourseInstance().getYearSemester().getAcademicYear() + ", " + assessment.getCourseInstance().getYearSemester().getSemester()+" semestar";
	}	

	private void generateForm(Assessment assessment, Assessment scheduleAssessment, AssessmentRoom room, User student, int redni_broj) {
		if(!(assessment.getAssessmentConfiguration() instanceof AssessmentConfChoice)) {
			// Ovo se ne smije dogoditi!
			return;
		}
		AssessmentConfChoice aconf = (AssessmentConfChoice)assessment.getAssessmentConfiguration();
		int nop = aconf.getProblemsNum();
		double circleRadius = nop>20 ? 1.6 : 1.8;
		
		nextPage();

		pdf.newPath();
		pdf.rectangle(-15,0,3,3);
		pdf.fill();
		pdf.stroke();
		
		pdf.newPath();
		pdf.rectangle(cw-3,0,3,3);
		pdf.fill();
		pdf.stroke();
		
		pdf.newPath();
		pdf.rectangle(-15,ch,3,3);
		pdf.fill();
		pdf.stroke();

		pdf.newPath();
		pdf.rectangle(cw-3,ch,3,3);
		pdf.fill();
		pdf.stroke();

		String line1 = prepLine1(scheduleAssessment);
		String line2 = prepLine2(scheduleAssessment);
		
		int cy = 0;
		
        pdf.begintext();
        pdf.setfont(timesbd,14);
        pdf.showText(line1,PdfContentByte.ALIGN_CENTER,(float)(cw/2.0),(float)(cy+7));
        pdf.showText(line2,PdfContentByte.ALIGN_CENTER,(float)(cw/2.0),(float)(cy+14));
        pdf.endtext();

        cy += 15;
        
        pdf.begintext();
        pdf.setfont(timesbd,12);
        pdf.showText("Prezime, ime:",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+7));
        pdf.showText("Dvorana:",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+14));
        pdf.endtext();
        if(student!=null) {
	        pdf.begintext();
	        pdf.setfont(times,12);
	        pdf.showText(student.getLastName()+", "+student.getFirstName()+"  ("+student.getJmbag()+")",PdfContentByte.ALIGN_LEFT,(float)(27),(float)(cy+7));
	        String roomName = room.getRoom()==null ? "?" : room.getRoom().getName();
	        pdf.showText(roomName+"  ("+String.valueOf(redni_broj)+")",PdfContentByte.ALIGN_LEFT,(float)19,(float)(cy+14));
	        pdf.endtext();
        }
        
        cy += 15;

        pdf.begintext();
        pdf.setfont(timesbd,12);
        pdf.showText("Grupa",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+(nop>20?15:20)));
        pdf.endtext();
        pdf.begintext();
        int i = 0;
        String[] groupLabels = aconf.getGroupsLabels()==null ? new String[0] : aconf.getGroupsLabels().split("\t");
        for(String l : groupLabels) {
            pdf.showText(l,PdfContentByte.ALIGN_CENTER,(float)(30+i*10),(float)(cy+(nop>20? 7 : 10)));
            i++;
        }
        pdf.endtext();

        pdf.newPath();
        pdf.setlinewidth(1);
        for(i=0; i<groupLabels.length; i++) {
        	pdf.circle(30+i*10,cy+(nop>20?15:18),circleRadius);
        }
        pdf.stroke();

        if(student!=null) {
        	drawBarCodeStandard(cb, student.getJmbag(), (int)(cw*0.43+0.5), cy+20);
        }
        
        cy += nop>20 ? 20 : 25;
        
        pdf.begintext();
        pdf.setfont(timesbd,12);
        pdf.showText("Zadaci",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+7));
        pdf.endtext();

        cy += 10;

        // Crtanje zaglavlja zadataka

        double answerDist = 8;
        double problemHeight = nop>20 ? 7 : 8;
        double problemLeft = 20;
        pdf.begintext();
        pdf.setfont(times,12);
        pdf.showText("Broj",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+5));
        i=0;
        String[] answerLabels = new String[aconf.getAnswersNumber()];
        for(int j=0; j<answerLabels.length; j++) {
        	answerLabels[j] = new StringBuilder().append((char)('A'+j)).toString();
        }
        for(String l : answerLabels) {
            pdf.showText(l,PdfContentByte.ALIGN_CENTER,(float)(problemLeft+i*answerDist),(float)(cy+5));
            i++;
        }
        if(aconf.getErrorColumn()) {
        	String errorLabel = aconf.getErrorColumnText();  
        	if (errorLabel == null) {
        		errorLabel = "Greška";
        	}
            pdf.showText(errorLabel,PdfContentByte.ALIGN_CENTER,(float)(problemLeft+(i+0.5)*answerDist),(float)(cy+5));
        }
        pdf.endtext();

        cy += nop>20 ? 10 : 20;

        pdf.begintext();
        pdf.setfont(times,12);
        i = 0;
        String[] problemLabels = aconf.getProblemsLabels()==null ? new String[0] : aconf.getProblemsLabels().split("\t");
        for(String l : problemLabels) {
            pdf.showText(l,PdfContentByte.ALIGN_CENTER,(float)(3),(float)(cy+5+i*problemHeight));
            i++;
        }
        pdf.endtext();
        
        pdf.newPath();
        pdf.setlinewidth(1);
        for(i=0; i<problemLabels.length; i++) {
        	int j = 0;
            for(j=0; j<aconf.getAnswersNumber(); j++) {
            	pdf.circle(problemLeft+j*answerDist,cy+5+i*problemHeight-1.5,circleRadius);
            }
            if(aconf.getErrorColumn()) {
            	pdf.circle(problemLeft+(j+0.5)*answerDist,cy+5+i*problemHeight-1.5,circleRadius);
            }
        }
        pdf.stroke();
        
        pdf.newPath();
        pdf.setlinewidth(1);
        int kraj = aconf.getAnswersNumber();
        double rightEnd = problemLeft+kraj*answerDist;
        if(aconf.getErrorColumn()) {
        	rightEnd += 1.5*answerDist;
        }        
        rightEnd += 10;
        
        for(int h=0; h<=problemLabels.length; h++) {
        	pdf.moveto(rightEnd, cy+h*problemHeight-0.5);
        	pdf.lineto(rightEnd+3*8, cy+h*problemHeight-0.5);
        }

        for(int v=0; v<=3; v++) {
        	pdf.moveto(rightEnd+v*8, cy-0.5);
        	pdf.lineto(rightEnd+v*8, cy+problemLabels.length*problemHeight-0.5);
        }
        pdf.stroke();

        cy += problemLabels.length*problemHeight + 10;
        
        if(nop>=20) {
	        pdf.begintext();
	        pdf.setfont(times,12);
	        pdf.showText("Broj",PdfContentByte.ALIGN_LEFT,(float)(0),(float)(cy+5));
	        i=0;
	        for(String l : answerLabels) {
	            pdf.showText(l,PdfContentByte.ALIGN_CENTER,(float)(problemLeft+i*answerDist),(float)(cy+5));
	            i++;
	        }
	        if(aconf.getErrorColumn()) {
	        	String errorLabel = aconf.getErrorColumnText();  
	        	if (errorLabel == null) {
	        		errorLabel = "Greška";
	        	}
	            pdf.showText(errorLabel,PdfContentByte.ALIGN_CENTER,(float)(problemLeft+(i+0.5)*answerDist),(float)(cy+5));
	        }
	        pdf.endtext();
        }
	}

	private void drawBarCodeStandard(PdfContentByte cb2, String jmbag, int x, int y) {
		Rectangle pageSize = PageSize.A4;
		float tempw = pageSize.getWidth()*0.4f;
		int temph = (int)(tempw*0.20f+0.5);

		Barcode39 code39 = new Barcode39();
		code39.setCode(jmbag);
		code39.setBaseline(15);
		code39.setBarHeight(temph);
		code39.setAltText("");
		Image im = code39.createImageWithBarcode(cb, null,null);
		PdfTemplate tp1 = cb.createTemplate(tempw,temph);
		try {
			tp1.addImage(im, tempw, 0, 0, temph, 0,0);
			cb.addTemplate(tp1, 0.5f*pageSize.getWidth(), pageSize.getHeight()-temph*1.2f-pageSize.getHeight()*0.15f);
		} catch (DocumentException e) {
			e.printStackTrace();
		}				
	}

}
