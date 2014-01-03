package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.BarcodeStickersBean;

import hr.fer.zemris.jcms.beans.barcodes.IOpisnik;
import hr.fer.zemris.jcms.beans.barcodes.IStudent;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.UserUtil;
import hr.fer.zemris.jcms.web.actions.data.BarcodeStickersData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

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

public class BarCodePDFCreator {

	public static void getBarcodeStickersData(
			final BarcodeStickersData data, final BarcodeStickersBean bean, final Long userID, final String courseInstanceID, final DeleteOnCloseFileInputStream[] reference, final String task) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canPerformCourseAdministration(data.getCourseInstance());
				if(!canManage) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				if(task.equals("input")) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				int psIndex = -1;
				for(int i = 0; i < bean.getPageSizeConsts().length; i++) {
					if(bean.getPageSizeConsts()[i].equals(bean.getPageSize())) {
						psIndex = i;
						break;
					}
				}
				if(psIndex==-1) {
					// Nepoznata velicina stranice!
					data.getMessageLogger().addErrorMessage("Veličina stranice nije poznata!");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				boolean continuEditing = false;
				if(bean.getColumnsCount()<1) {
					data.getMessageLogger().addErrorMessage("Broj stupaca mora biti pozitivan!");
					continuEditing = true;
				}
				if(bean.getRowsCount()<1) {
					data.getMessageLogger().addErrorMessage("Broj redaka mora biti pozitivan!");
					continuEditing = true;
				}
				if(bean.getMarginBottom()<0) {
					data.getMessageLogger().addErrorMessage("Donja margina mora biti nenegativna!");
					continuEditing = true;
				}
				if(bean.getMarginTop()<0) {
					data.getMessageLogger().addErrorMessage("Gornja margina mora biti nenegativna!");
					continuEditing = true;
				}
				if(bean.getMarginLeft()<0) {
					data.getMessageLogger().addErrorMessage("Lijeva margina mora biti nenegativna!");
					continuEditing = true;
				}
				if(bean.getMarginRight()<0) {
					data.getMessageLogger().addErrorMessage("Desna margina mora biti nenegativna!");
					continuEditing = true;
				}
				if(bean.getNumberOfBarcodesPerStudent()<1) {
					data.getMessageLogger().addErrorMessage("Broj naljepnica po studentu mora biti pozitivan!");
					continuEditing = true;
				}
				if(bean.getNumberOfBarcodesPerStudent() % bean.getColumnsCount() != 0) {
					data.getMessageLogger().addErrorMessage("Broj naljepnica po studentu mora biti djeljiv s brojem stupaca!");
					continuEditing = true;
				}
				if(continuEditing) {
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				BufferedReader br = new BufferedReader(new StringReader(bean.getJmbags()==null ? "" : bean.getJmbags()));
				List<String> jmbags = new ArrayList<String>(1000);
				try {
					while(true) {
						String line = br.readLine();
						if(line==null) break;
						line = line.trim();
						if(line.equals("")) continue;
						jmbags.add(line);
					}
				} catch(Exception ex) {
					data.getMessageLogger().addErrorMessage("Pogreška prilikom obrade JMBAG-ova.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return null;
				}
				br = null;
				List<User> allCourseUsers = dh.getCourseInstanceDAO().findCourseUsers(em, courseInstanceID);
				List<User> barcodeUsers = null;
				if(jmbags.isEmpty()) {
					// Ako nije unio jmbagove, tada ih želi SVE
					barcodeUsers = allCourseUsers;
				} else {
					Map<String,User> userJmbagMap = UserUtil.mapUserByJmbag(allCourseUsers);
					barcodeUsers = new ArrayList<User>(allCourseUsers.size());
					boolean err = false;
					for(String jmbag : jmbags) {
						User user = userJmbagMap.get(jmbag);
						if(user==null) {
							err = true;
							data.getMessageLogger().addErrorMessage("Student "+jmbag+" nije na kolegiju!");
							continue;
						}
						barcodeUsers.add(user);
					}
					if(err) {
						data.getMessageLogger().addErrorMessage("Barem jedan student nije pronađen. Popis neće biti generiran!");
						data.setResult(AbstractActionData.RESULT_INPUT);
						return null;
					}
				}
				allCourseUsers = null;

				Collections.sort(barcodeUsers, StringUtil.USER_COMPARATOR);

				if(bean.isAltSort()) {
					if(bean.getNumberOfBarcodesPerStudent()>=bean.getColumnsCount() && (bean.getNumberOfBarcodesPerStudent()%bean.getColumnsCount())==0) {
						int brojRedakaZaJednogStudenta = bean.getNumberOfBarcodesPerStudent()/bean.getColumnsCount();
						if((bean.getRowsCount()%brojRedakaZaJednogStudenta)==0) {
							int brojPodrucja = bean.getRowsCount()/brojRedakaZaJednogStudenta;
							List<List<User>> liste = new ArrayList<List<User>>();
							for(int i = 0; i < brojPodrucja; i++) {
								liste.add(new ArrayList<User>());
							}
							int brojStudenataPoListi = barcodeUsers.size() / brojPodrucja;
							if(barcodeUsers.size() % brojPodrucja != 0) brojStudenataPoListi++;
							for(int i = 0; i < brojPodrucja; i++) {
								int odIndex = i * brojStudenataPoListi;
								int doIndex = odIndex + brojStudenataPoListi;
								if(odIndex>=barcodeUsers.size()) continue;
								if(doIndex>barcodeUsers.size()) doIndex = barcodeUsers.size();
								liste.get(i).addAll(barcodeUsers.subList(odIndex, doIndex));
							}
							List<User> newList = new ArrayList<User>(barcodeUsers.size());
							for(int j = 0; j < brojStudenataPoListi; j++) {
								for(int i = 0; i < brojPodrucja; i++) {
									List<User> l = liste.get(i);
									if(j < l.size()) {
										newList.add(l.get(j));
									}
								}
							}
							barcodeUsers = newList;
						}
					}
				}
				Map<Long,String> studentGroups = null;
				if(bean.isShowGroups()) {
					List<UserGroup> grupe = DAOHelperFactory.getDAOHelper().getGroupDAO().listUserGroupsInGroupTree(em, data.getCourseInstance().getId(), "0");
					studentGroups = new HashMap<Long, String>(grupe.size()*2);
					for(UserGroup ug : grupe) {
						studentGroups.put(ug.getUser().getId(), ug.getGroup().getName());
					}
				}
				List<IStudent> barcodeStudents = new ArrayList<IStudent>(barcodeUsers.size());
				for(User u : barcodeUsers) {
					final String lectureGroup = studentGroups==null ? null : studentGroups.get(u.getId());
					final User student = u;
					barcodeStudents.add(new IStudent() {
						@Override
						public String getPrezime() {
							return student.getLastName();
						}
					
						@Override
						public String getJMBAG() {
							return student.getJmbag();
						}
					
						@Override
						public String getIme() {
							return student.getFirstName();
						}
						@Override
						public String getLectureGroup() {
							return lectureGroup;
						}
					});
				}
				barcodeUsers = null;

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".pdf");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				bean.setStudents(barcodeStudents);
				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					createPDF(bean, os);
					os.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName("barcode.pdf");
				stream.setMimeType("application/pdf");
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}	
	
	private static boolean createPDF(IOpisnik podaci, OutputStream stream) {	
		Rectangle pageSize;

		String p=podaci.getPageSize();
		if(p.equals("A0")){
			pageSize=PageSize.A0;
		}else if(p.equals("A1")){
			pageSize=PageSize.A1;
		}else if(p.equals("A2")){
			pageSize=PageSize.A2;
		}else if(p.equals("A3")){
			pageSize=PageSize.A3;
		}else if(p.equals("A4")){
			pageSize=PageSize.A4;
		}else if(p.equals("A5")){
			pageSize=PageSize.A5;
		}else if(p.equals("A6")){
			pageSize=PageSize.A6;
		}else if(p.equals("A7")){
			pageSize=PageSize.A7;
		}else if(p.equals("A8")){
			pageSize=PageSize.A8;
		}else if(p.equals("A9")){
			pageSize=PageSize.A9;
		}else if(p.equals("A10")){
			pageSize=PageSize.A10;
		}else {
			return false;
		}
		System.out.println("Generiram bar kod 39...");	  
		if(podaci.isLandscape()){
			pageSize=pageSize.rotate();
		}		

		System.out.println("dimenzije papira: "+pageSize.getWidth()+" x "+pageSize.getHeight());	

		Document document = new Document(pageSize);	
		try {

			java.util.List<IStudent> lista = podaci.getStudents();
			PdfWriter writer = PdfWriter.getInstance(document,stream);
			document.open();
			PdfContentByte cb = writer.getDirectContent();			

			int wef = (int)((pageSize.getWidth()-podaci.getMarginRight()*2.83- podaci.getMarginLeft()*2.83));
			int hef = (int)((pageSize.getHeight()-podaci.getMarginTop()*2.83- podaci.getMarginBottom()*2.83));
			int columnsCount = podaci.getColumnsCount();


			float tempw = (int)((double)(wef)/columnsCount);
			int temph = hef/podaci.getRowsCount();
			System.out.println("dimenzije templatea: : "+tempw+" x "+temph);

			float pomakStudent = 0;
			int brojIspisanihRedova = 0;
			BaseFont bf = BaseFont.createFont(BaseFont.TIMES_ROMAN,
					BaseFont.CP1250, BaseFont.EMBEDDED);
			for(IStudent student : lista){
				Barcode39 code39 = new Barcode39();
				code39.setCode(student.getJMBAG());
				code39.setBaseline(15);
				Image im = code39.createImageWithBarcode(cb, null,null);	

				String text = student.getPrezime()+" "+student.getIme();
				if(student.getLectureGroup()!=null) {
					text = text + " ("+student.getLectureGroup()+")";
				}
				PdfTemplate tp1 = cb.createTemplate(tempw,temph);

				tp1.beginText();		
				float fontSize=temph/5.0f/1.55f;
				if(temph>0.8*tempw) {
					fontSize*=0.7;
				}
				if(fontSize>12) fontSize = 12;
				if(fontSize<6) fontSize = 6;				
				tp1.setFontAndSize(bf, fontSize);								
				tp1.showTextAligned(PdfTemplate.ALIGN_CENTER, text,	tempw/2, 2*temph/15, 0);
				tp1.endText();

				float leftBarMargin = 10 + tempw/20;
				// Bio original
				// float barW = tempw-20;
				// float barH = barW/3;
				// tp1.addImage(im, barW, 0, 0, barH, 10,temph-10-barH);				
				float barW = tempw-2*leftBarMargin;
				float barH = barW/3;
				tp1.addImage(im, barW, 0, 0, barH, leftBarMargin,temph-10-barH);				

				float pomakX=0;
				float pomakY=0;	
				for(int i=0; i<podaci.getNumberOfBarcodesPerStudent()/podaci.getColumnsCount(); i++){
					pomakX=0;
					for(int j=0; j<podaci.getColumnsCount(); j++){
						cb.addTemplate(tp1, podaci.getMarginLeft()*2.83f+pomakX,
								pageSize.getHeight()-temph-podaci.getMarginTop()-pomakY-pomakStudent);						
						pomakX+=tempw;						
					}
					pomakY+=temph;
					brojIspisanihRedova++;
					if (pomakStudent+pomakY>=
						pageSize.getHeight()-podaci.getMarginBottom()*2.83f-podaci.getMarginTop()*2.83f-10){
						document.newPage();
						pomakStudent=0;
						pomakY=0;
						brojIspisanihRedova=0;
					}						
				}
				pomakStudent=brojIspisanihRedova*temph;				
			}			

			System.out.println("Bar kod generiran");
			return true;
		} catch (DocumentException de) {
			System.err.println(de.getMessage());			
			return false;
		} catch (Exception ioe) {
			System.err.println(ioe.getMessage());
			return false;
		} finally {
			try { document.close(); } catch(Exception ignorable) {}
		}
	}

}
