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
import jxl.write.Number;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.NguyenVong;

import com.google.protobuf.InvalidProtocolBufferException;

public class WriteExellDSXetTuyen {

	private WritableCellFormat timesBoldUnderline;
	private WritableCellFormat times;
	private String inputFile;

	public static void main(String[] args) {

		new WriteExellDSXetTuyen().run();

	}

	public void run() {
		setOutputFile("d:/DATN/Result/DSXetTuyen.xls");
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
		MyKey key = MyKey.newBuilder().setKey("ALL_NVSV").build();
		try {
			ListKey mlist = ListKey.parseFrom(RedisClient.shareInstance().get(
					key.toByteArray()));
			List<String> allNVSV = mlist.getKeyList();
			int i = 1;
			for (String keysv : allNVSV) {
				key = MyKey.newBuilder().setKey(keysv).build();
				NguyenVongSV mNVSV = NguyenVongSV.parseFrom(RedisClient
						.shareInstance().get(key.toByteArray()));
				write(keysv.replace("NV_", ""), mNVSV, excelSheet, i);
				i++;
			}
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		workbook.write();
		workbook.close();
	}

	private void write(String sbd, NguyenVongSV mNVSV, WritableSheet sheet,
			int i) {
		NguyenVong nv1, nv2, nv3, nv4;
		nv1 = mNVSV.getNV1();
		nv2 = mNVSV.getNV2();
		nv3 = mNVSV.getNV3();
		nv4 = mNVSV.getNV4();
		try {
			addLabel(sheet, 0, i, sbd);
			addLabel(sheet, 1, i, nv1.getMaNganh() + "-"
					+ nv1.getToHopMon().toString());
			addNumberDouble(sheet, 2, i, nv1.getDiemXet());
			if (nv2.getDiemXet() != 0) {
				addLabel(sheet, 3, i, nv2.getMaNganh() + "-"
						+ nv2.getToHopMon().toString());
				addNumberDouble(sheet, 4, i, nv2.getDiemXet());
				if (nv3.getDiemXet() != 0) {
					addLabel(sheet, 5, i, nv3.getMaNganh() + "-"
							+ nv3.getToHopMon().toString());
					addNumberDouble(sheet, 6, i, nv3.getDiemXet());
					if (nv4.getDiemXet() != 0) {
						addLabel(sheet, 7, i, nv4.getMaNganh() + "-"
								+ nv4.getToHopMon().toString());
						addNumberDouble(sheet, 8, i, nv4.getDiemXet());

					}
				}
			}
		} catch (RowsExceededException e) {
			e.printStackTrace();
		} catch (WriteException e) {
			e.printStackTrace();
		}

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
		addCaption(sheet, 1, 0, "NV1");
		addCaption(sheet, 2, 0, "NV1_DX");
		addCaption(sheet, 3, 0, "NV2");
		addCaption(sheet, 4, 0, "NV2_DX");
		addCaption(sheet, 5, 0, "NV3");
		addCaption(sheet, 6, 0, "NV3_DX");
		addCaption(sheet, 7, 0, "NV4");
		addCaption(sheet, 8, 0, "NV4_DX");

	}

	private void addCaption(WritableSheet sheet, int column, int row, String s)
			throws RowsExceededException, WriteException {
		Label label;
		label = new Label(column, row, s, timesBoldUnderline);
		sheet.addCell(label);

	}

	private void addNumberDouble(WritableSheet sheet, int column, int row,
			Double db) throws WriteException, RowsExceededException {
		Number number;
		number = new Number(column, row, db, times);
		sheet.addCell(number);
	}

	private void addLabel(WritableSheet sheet, int column, int row, String s)
			throws WriteException, RowsExceededException {
		Label label;
		label = new Label(column, row, s, times);
		sheet.addCell(label);
	}

}
