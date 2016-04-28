package ua.kpi.nc.reports;

import java.util.ArrayList;
import java.util.List;

public class Row {

	private List<Object> cells;

	public Row() {
		cells = new ArrayList<>();
	}

	public List<Object> getCells() {
		return cells;
	}

	public void setCells(List<Object> cells) {
		this.cells = cells;
	}

	public boolean addCell(Object arg0) {
		return cells.add(arg0);
	}

}
