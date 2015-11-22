package sami.hust.edu.vn;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa.DiemXet;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;

public class ReadCSV_DBKhoa {

	public static void main(String[] args) {

		ReadCSV_DBKhoa obj = new ReadCSV_DBKhoa();
		obj.run();

	}

	public void run() {

		String csvFile = "E:/TSBachKhoa/TuyenSinh/data/ChiTieu.csv";
		Reader in;
		CSVParser records;

		try {
			in = new FileReader(csvFile);
			records = new CSVParser(in, CSVFormat.EXCEL.withHeader());
			ArrayList<String> mList = new ArrayList<String>();
			for (CSVRecord record : records) {
				ArrayList<DiemXet> dx = new ArrayList<DiemXet>();

				ChiTieuKhoa model = ChiTieuKhoa.newBuilder()
						.setMaKhoa(record.get("MaNN")).setDiemChuan(0)
						.setChiTieu(Integer.valueOf(record.get("ChiTieu")))
						.addAllDSDiemXet(dx).setSLHienTai(0)
						.setMucDo(Double.parseDouble(record.get("MucDo")))
						.build();
				MyKey key = MyKey.newBuilder().setKey(model.getMaKhoa())
						.build();
				RedisClient.shareInstance().set(key.toByteArray(),
						model.toByteArray());
				mList.add(model.getMaKhoa());
			}
			MyKey key = MyKey.newBuilder().setKey("ALL_KHOA").build();
			ListKey model = ListKey.newBuilder().addAllKey(mList).build();
			RedisClient.shareInstance().set(key.toByteArray(),
					model.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}