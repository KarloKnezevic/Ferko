package hr.fer.zemris.jcms.beans;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.barcodes.IOpisnik;
import hr.fer.zemris.jcms.beans.barcodes.IStudent;

public class BarcodeStickersBean implements IOpisnik {

	private int columnsCount = 3;
	private int rowsCount = 10;
	private float marginBottom = 20;
	private float marginTop = 20;
	private float marginLeft = 20;
	private float marginRight = 20;
	private int numberOfBarcodesPerStudent = 3;
	private String pageSize = "A4";
	private boolean landscape = false;
	private boolean altSort = false;
	private boolean showGroups = false;
	private String jmbags;
	
	private final String[] pageSizeConsts = new String[] {"A0","A1","A2","A3","A4","A5","A6","A7","A8","A9"};
	private List<StringNameStringValue> pageSizes;
	private List<IStudent> students;
	
	public BarcodeStickersBean() {
		pageSizes = new ArrayList<StringNameStringValue>();
		for(String ps : pageSizeConsts) {
			pageSizes.add(new StringNameStringValue(ps,ps));
		}
	}
	
	public boolean isShowGroups() {
		return showGroups;
	}
	public void setShowGroups(boolean showGroups) {
		this.showGroups = showGroups;
	}
	
	public String[] getPageSizeConsts() {
		return pageSizeConsts;
	}
	
	public List<StringNameStringValue> getPageSizes() {
		return pageSizes;
	}
	
	@Override
	public int getColumnsCount() {
		return columnsCount;
	}

	@Override
	public float getMarginBottom() {
		return marginBottom;
	}

	@Override
	public float getMarginLeft() {
		return marginLeft;
	}

	@Override
	public float getMarginRight() {
		return marginRight;
	}

	@Override
	public float getMarginTop() {
		return marginTop;
	}

	@Override
	public int getNumberOfBarcodesPerStudent() {
		return numberOfBarcodesPerStudent;
	}

	@Override
	public String getPageSize() {
		return pageSize;
	}

	@Override
	public int getRowsCount() {
		return rowsCount;
	}

	@Override
	public List<IStudent> getStudents() {
		return students;
	}

	@Override
	public boolean isLandscape() {
		return landscape;
	}

	public boolean isAltSort() {
		return altSort;
	}
	public void setAltSort(boolean altSort) {
		this.altSort = altSort;
	}
	
	public String getJmbags() {
		return jmbags;
	}
	
	public void setJmbags(String jmbags) {
		this.jmbags = jmbags;
	}
	
	public void setStudents(List<IStudent> students) {
		this.students = students;
	}
	
	public void setColumnsCount(int columnsCount) {
		this.columnsCount = columnsCount;
	}

	public void setRowsCount(int rowsCount) {
		this.rowsCount = rowsCount;
	}

	public void setMarginBottom(float marginBottom) {
		this.marginBottom = marginBottom;
	}

	public void setMarginTop(float marginTop) {
		this.marginTop = marginTop;
	}

	public void setMarginLeft(float marginLeft) {
		this.marginLeft = marginLeft;
	}

	public void setMarginRight(float marginRight) {
		this.marginRight = marginRight;
	}

	public void setNumberOfBarcodesPerStudent(int numberOfBarcodesPerStudent) {
		this.numberOfBarcodesPerStudent = numberOfBarcodesPerStudent;
	}

	public void setPageSize(String pageSize) {
		this.pageSize = pageSize;
	}

	public void setLandscape(boolean landscape) {
		this.landscape = landscape;
	}


}
