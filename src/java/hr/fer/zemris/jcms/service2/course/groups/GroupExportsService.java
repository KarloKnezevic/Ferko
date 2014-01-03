package hr.fer.zemris.jcms.service2.course.groups;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.ExportGroupMembershipTreeData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class GroupExportsService {

	public static void exportGroupMembers(EntityManager em, ExportGroupMembershipTreeData data) {
		
		// Dohvati podatke
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		CourseInstance ci = data.getCourseInstance();
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(ci, data.getGroup());
		boolean canView = gPerm.getCanView();
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getFormat()==null || (!data.getFormat().equals("xls") && !data.getFormat().equals("mm") && !data.getFormat().equals("csv"))) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		String ext = ".xls";
		if(data.getFormat().equals("mm")) ext = ".txt";
		if(data.getFormat().equals("csv")) ext = ".csv";
		
		File f = null;
		try {
			f = File.createTempFile("JCMS_", ext);
		} catch (IOException e) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		OutputStream os = null;
		try {
			os = new BufferedOutputStream(new FileOutputStream(f));
			if(data.getFormat().equals("xls")) {
				if(!exportGroupMembersTreeToXLS(data.getMessageLogger(), data.getCourseInstance(), data.getGroup(), os)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
			} else if(data.getFormat().equals("csv")) {
				if(!exportGroupMembersTreeToCSV(data.getMessageLogger(), data.getCourseInstance(), data.getGroup(), os)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
			} else {
				if(!exportGroupMembersTreeToMM(data.getMessageLogger(), data.getCourseInstance(), data.getGroup(), os)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
			}
		} catch (IOException e) {
			try { if(os!=null) os.close(); } catch(Exception ignorable) {}
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		try { if(os!=null) os.close(); } catch(Exception ignorable) {}
		DeleteOnCloseFileInputStream stream = null;
		try {
			stream = new DeleteOnCloseFileInputStream(f);
		} catch (IOException e) {
			f.delete();
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGenerateFile"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		stream.setFileName("popis"+ext);
		if(data.getFormat().equals("csv")) {
			stream.setMimeType("text/csv; charset=utf-8");
		} else if(data.getFormat().equals("xls")) {
			stream.setMimeType("application/vnd.ms-excel");
		} else if(data.getFormat().equals("mm")) {
			stream.setMimeType("text/plain; charset=windows-1250");
		} else {
			stream.setMimeType("application/octet-stream");
		}
		data.setStream(stream);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	protected static boolean exportGroupMembersTreeToXLS(IMessageLogger messageLogger, CourseInstance ci, Group group, OutputStream bos) {
		try {
			HSSFWorkbook wb = new HSSFWorkbook();
			HSSFCellStyle cellStyle = wb.createCellStyle();
			// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
			cellStyle.setDataFormat((short)0x31);

			HSSFFont yellowFont = wb.createFont();
			yellowFont.setFontHeightInPoints((short)10);
			yellowFont.setColor( (short)0x5 ); //make it yellow
			
			HSSFCellStyle blueCell = wb.createCellStyle(); 
			blueCell.setFillPattern((short) HSSFCellStyle.SOLID_FOREGROUND);
			blueCell.setFillForegroundColor((short)0x4); // blue background
			blueCell.setFont(yellowFont);
			blueCell.setBorderTop((short)1);
			blueCell.setBorderBottom((short)1); 
			
			exportGroupMembersTreeToXLSRecursive(messageLogger, ci, group, wb, cellStyle, blueCell);
			wb.write(bos);
			bos.flush();
			bos.close();
		} catch(Exception ex) {
			messageLogger.addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	private static void exportGroupMembersTreeToXLSRecursive(IMessageLogger messageLogger, CourseInstance ci, Group group, HSSFWorkbook wb, HSSFCellStyle cellStyle, HSSFCellStyle blueCell) {
		// Dohvati dozvole za ovu grupu
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(ci, group);
		// Ako ovu grupu uopće ne mogu vidjeti, van!
		if(!gPerm.getCanView()) return;
		// Ako u ovoj grupi ima korisnika i ja imam dozvolu vidjeti ih, ispiši ih
		if(!group.getUsers().isEmpty() && gPerm.getCanViewUsers()) {
			// ispisi
			HSSFSheet sheet = wb.createSheet(fixGroupName(group.getName()));
			sheet.setDefaultColumnWidth((short)20);
			int rowIndex = 0;
			HSSFRow row = sheet.createRow((short)rowIndex);
			HSSFCell cell = row.createCell((short)0);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString("Grupa:"));
			cell = row.createCell((short)1);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(group.getName()));
			rowIndex++;
			row = sheet.createRow((short)rowIndex);
			rowIndex++;
			row = sheet.createRow((short)rowIndex);
			cell = row.createCell((short)0);
			cell.setCellStyle(blueCell);
			cell.setCellValue(new HSSFRichTextString("Br."));
			cell = row.createCell((short)1);
			cell.setCellStyle(blueCell);
			cell.setCellValue(new HSSFRichTextString("JMBAG"));
			cell = row.createCell((short)2);
			cell.setCellStyle(blueCell);
			cell.setCellValue(new HSSFRichTextString("Prezime"));
			cell = row.createCell((short)3);
			cell.setCellStyle(blueCell);
			cell.setCellValue(new HSSFRichTextString("Ime"));
			cell = row.createCell((short)4);
			cell.setCellStyle(blueCell);
			cell.setCellValue(new HSSFRichTextString("Oznaka"));
			List<UserGroup> users = new ArrayList<UserGroup>(group.getUsers());
			Collections.sort(users, StringUtil.USER_GROUP_COMPARATOR1);
			int redniBroj = 0;
			for(UserGroup ug : users) {
				rowIndex++; redniBroj++;
				int columnIndex = 0;
				row = sheet.createRow((short)rowIndex);
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(Integer.toString(redniBroj)+"."));
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getUser().getJmbag()));
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getUser().getLastName()));
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getUser().getFirstName()));
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(ug.getTag() == null ? "" : ug.getTag()));
			}
		}
		List<Group> children = new ArrayList<Group>(group.getSubgroups());
		Collections.sort(children, StringUtil.GROUP_COMPARATOR);
		for(Group child : children) {
			exportGroupMembersTreeToXLSRecursive(messageLogger, ci, child, wb, cellStyle, blueCell);
		}
	}

	protected static boolean exportGroupMembersTreeToMM(IMessageLogger messageLogger, CourseInstance ci, Group group, OutputStream bos) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, "windows-1250"));
			bw.write("IDProvjere#SifDvorane#PrezimeIme#JMBAG#Rbr\r\n");
			bw.write("####\r\n");
			exportGroupMembersTreeToMMRecursive(messageLogger, ci, group, bw);
			bw.flush();
			bw.close();
		} catch(Exception ex) {
			messageLogger.addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	private static void exportGroupMembersTreeToMMRecursive(IMessageLogger messageLogger, CourseInstance ci, Group group, BufferedWriter bw) throws IOException {
		// Dohvati dozvole za ovu grupu
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(ci, group);
		// Ako ovu grupu uopće ne mogu vidjeti, van!
		if(!gPerm.getCanView()) return;
		// Ako u ovoj grupi ima korisnika i ja imam dozvolu vidjeti ih, ispiši ih
		if(!group.getUsers().isEmpty() && gPerm.getCanViewUsers()) {
			List<GroupWideEvent> gweList = new ArrayList<GroupWideEvent>(group.getEvents());
			String prostorija = "Nije definirana";
			if(!gweList.isEmpty()) {
				GroupWideEvent gwe = gweList.get(0);
				prostorija = gwe.getRoom().getName();
			}
			// ispisi
			List<UserGroup> users = new ArrayList<UserGroup>(group.getUsers());
			Collections.sort(users, StringUtil.USER_GROUP_COMPARATOR1);
			int redniBroj = 0;
			for(UserGroup ug : users) {
				redniBroj++;
				bw.write("*"+ci.getCourse().getIsvuCode()+"*#"+prostorija+"#"+ug.getUser().getLastName()+", "+ug.getUser().getFirstName()+" ("+ug.getUser().getJmbag()+")#*"+ug.getUser().getJmbag()+"*#"+redniBroj+"\r\n");
			}
		}
		List<Group> children = new ArrayList<Group>(group.getSubgroups());
		Collections.sort(children, StringUtil.GROUP_COMPARATOR);
		for(Group child : children) {
			exportGroupMembersTreeToMMRecursive(messageLogger, ci, child, bw);
		}
	}

	protected static boolean exportGroupMembersTreeToCSV(IMessageLogger messageLogger, CourseInstance ci, Group group, OutputStream bos) {
		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, "utf-8"));
			exportGroupMembersTreeToCSVRecursive(messageLogger, ci, group, bw);
			bw.flush();
			bw.close();
		} catch(Exception ex) {
			messageLogger.addErrorMessage("Dogodila se je greška prilikom generiranja izlaza.");
			return false;
		}
		return true;
	}
	
	private static void exportGroupMembersTreeToCSVRecursive(IMessageLogger messageLogger, CourseInstance ci, Group group, BufferedWriter bw) throws IOException {
		// Dohvati dozvole za ovu grupu
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(ci, group);
		// Ako ovu grupu uopće ne mogu vidjeti, van!
		if(!gPerm.getCanView()) return;
		// Ako u ovoj grupi ima korisnika i ja imam dozvolu vidjeti ih, ispiši ih
		if(!group.getUsers().isEmpty() && gPerm.getCanViewUsers()) {
			// ispisi
			List<UserGroup> users = new ArrayList<UserGroup>(group.getUsers());
			Collections.sort(users, StringUtil.USER_GROUP_COMPARATOR1);
			int redniBroj = 0;
			for(UserGroup ug : users) {
				redniBroj++;
				bw.write(group.getName()+"\t"+redniBroj+".\t"+ug.getUser().getJmbag()+"\t"+ug.getUser().getLastName()+"\t"+ug.getUser().getFirstName()+"\t"+(ug.getTag() == null ? "" : ug.getTag())+"\r\n");
			}
		}
		List<Group> children = new ArrayList<Group>(group.getSubgroups());
		Collections.sort(children, StringUtil.GROUP_COMPARATOR);
		for(Group child : children) {
			exportGroupMembersTreeToCSVRecursive(messageLogger, ci, child, bw);
		}
	}

	private static String fixGroupName(String name) {
		if(name==null || name.length()==0) {
			return "Bez_imena";
		}
		StringBuilder sb = new StringBuilder(name.length());
		char[] c = name.toCharArray();
		for(int i = 0; i < c.length; i++) {
			switch(c[i]) {
			case '/': sb.append('_'); break;
			case '\\': sb.append('_'); break;
			case '*': sb.append('_'); break;
			case '?': sb.append('_'); break;
			case '[': sb.append('_'); break;
			case ']': sb.append('_'); break;
			case '.': sb.append('_'); break;
			case '!': sb.append('_'); break;
			case ':': sb.append('_'); break;
			case ' ': sb.append('_'); break;
			default: sb.append(c[i]); break;
			}
		}
		if(sb.length()>31) sb.setLength(31);
		return sb.toString();
	}

}
