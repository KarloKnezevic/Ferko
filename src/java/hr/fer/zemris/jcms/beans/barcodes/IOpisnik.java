package hr.fer.zemris.jcms.beans.barcodes;
import java.util.*;


public interface IOpisnik {
	public float getMarginTop();
	public float getMarginBottom();
	public float getMarginLeft();
	public float getMarginRight();	
	
	public int getColumnsCount();
	public int getRowsCount();
	public int getNumberOfBarcodesPerStudent();
	
	public String getPageSize(); //"recimo "A4", prima "Ax", x={0..10}
	public boolean isLandscape();	
	public List<IStudent> getStudents();	
}
