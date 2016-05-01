package ua.kpi.nc.report.poi;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ua.kpi.nc.report.Line;
import ua.kpi.nc.report.Report;
import ua.kpi.nc.report.renderer.ReportRenderer;

/**
 * Created by Алексей on 28.04.2016.
 */
public abstract class AbsRender implements ReportRenderer {
	protected BufferedOutputStream bos;
	protected Workbook wb;
	protected String filename;
	protected ArrayList<Object> rows;
	protected ArrayList<Line> cells;
	protected static Logger log = LoggerFactory.getLogger(AbsRender.class.getName());

	@Override
	public void render(Report report, OutputStream out) {
		rows = (ArrayList<Object>) report.getHeader().getCells();
		cells = (ArrayList<Line>) report.getLines();
		write(rows, cells, out);
	}

	protected void write(List<Object> objects, List<Line> lines, OutputStream out) {
		ArrayList<Object> rows = (ArrayList<Object>) objects;
		ArrayList<Line> cells = (ArrayList<Line>) lines;
		log.trace("rows and cells initialization was successful");
		Sheet sheet = wb.createSheet();
		Row row = sheet.createRow(0);
		for (int i = 0; i < rows.size(); i++) {
			Cell cell = row.createCell(i);
			cell.setCellValue(rows.get(i) + "");
			sheet.autoSizeColumn(i);
		}
		for (int i = 1; i <= cells.size(); i++) {
			Row roww = sheet.createRow(i);
			for (int j = 0; j < rows.size(); j++) {
				Cell cell = roww.createCell(j);
				cell.setCellValue(cells.get(i - 1).getCells().get(j) + "");
			}
		}
		try {
			wb.write(out);
			log.trace("Write data into Workbook was successful");
		} catch (IOException e) {
			log.error("Cannot write data into Workbook", e);
		}
	}

}