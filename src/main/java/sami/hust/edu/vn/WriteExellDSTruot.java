package sami.hust.edu.vn;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.UnderlineStyle;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;

import com.google.protobuf.InvalidProtocolBufferException;

public class WriteExellDSTruot {

	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private String inputFile;

	public static void main(String[] args) {

		new WriteExellDSTruot().run();

	}

	public void run() {
		setOutputFile("d:/DATN/Result/DSTruot.xls");
		try {
			write();
		} catch (WriteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setOutputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public void write() throws IOException, WriteException {
		File file = new File(inputFile);
		WorkbookSettings wbSettings = new WorkbookSettings();

		wbSettings.setLocale(new Locale("en", "EN"));

		WritableWorkbook workbook = Workbook.createWorkbook(file, wbSettings);
		workbook.createSheet("Report", 0);
		WritableSheet excelSheet = workbook.getSheet(0);
		createLabel(excelSheet);
		MyKey key = MyKey.newBuilder().setKey("DSSVTruot").build();
		try {
			ListKey mlist = ListKey.parseFrom(RedisClient.shareInstance().get(
					key.toByteArray()));
			List<String> dstruot = mlist.getKeyList();
			int i = 1;
			for (String sbd : dstruot) {
				addLabel(excelSheet, 0, i, sbd);
				i++;
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		workbook.write();
		workbook.close();
	}

	private void createLabel(WritableSheet sheet) throws WriteException {
		// Lets create a times font
		WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
		// Define the cell format
		times = new WritableCellFormat(times10pt);
		// Lets automatically wrap the cells
		times.setWrap(false);
		// create create a bold font with unterlines
		WritableFont times10ptBoldUnderline = new WritableFont(
				WritableFont.TIMES, 10, WritableFont.BOLD, false,
				UnderlineStyle.SINGLE);
		timesBoldUnderline = new WritableCellFormat(times10ptBoldUnderline);
		// Lets automatically wrap the cells
		timesBoldUnderline.setWrap(false);
		CellView cv = new CellView();
		cv.setFormat(times);
		cv.setFormat(timesBoldUnderline);
		cv.setAutosize(true);

		// Write a few headers
		// Write a few headers
		addCaption(sheet, 0, 0, "SBD");

	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);

	}
//
//	private void addNumberDouble(WritableSheet sheet, int column, int row,
//			Double db) throws WriteException, RowsExceededException {
//		Number number;
//		number = new Number(column, row, db, times);
	// sheet.addCell(number);
//	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

}
