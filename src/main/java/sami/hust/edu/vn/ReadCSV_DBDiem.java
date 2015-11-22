package sami.hust.edu.vn;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import sami.hust.edu.vn.ProtoModels.DIEM_SV;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.DiemThi;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.DoiTuongUuTien;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.KhuVuc;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.UuTien;
import sami.hust.edu.vn.ProtoModels.MyKey;

public class ReadCSV_DBDiem {

	public static void main(String[] args) {

		ReadCSV_DBDiem obj = new ReadCSV_DBDiem();
		RedisClient.shareInstance().clearCache();
		obj.run();

	}

	public void run() {

		String csvFile = "E:/TSBachKhoa/TuyenSinh/data/DM/kqthithptqg-all.csv";
		Reader in;
		CSVParser records;

		try {
			in = new FileReader(csvFile);
			records = new CSVParser(in, CSVFormat.EXCEL.withHeader());

			for (CSVRecord record : records) {

				DiemThi mDiem = DiemThi.newBuilder()
						.setToan(castDiem(record.get("Toan")))
						.setVan(castDiem(record.get("Van")))
						.setLy(castDiem(record.get("Ly")))
						.setHoa(castDiem(record.get("Hoa")))
						.setSinh(castDiem(record.get("Sinh")))
						.setSu(castDiem(record.get("Su")))
						.setDia(castDiem(record.get("Dia")))
						.setAnh(castDiem(record.get("Anh")))
						.setNga(castDiem(record.get("Nga")))
						.setPhap(castDiem(record.get("Phap")))
						.setTrung(castDiem(record.get("Trung")))
						.setDuc(castDiem(record.get("Duc")))
						.setNhat(castDiem(record.get("Nhat"))).build();
				DIEM_SV model = DIEM_SV.newBuilder().setSBD(record.get("SBD"))
						.setKV(castKV(record.get("KV")))
						.setDTUT(castDT(record.get("DT")))
						.setUT(castUT(record.get("UT")))
						.setHoTen(record.get("HoTen"))
						.setNgaySinh(record.get("NgaySinh")).setDiem(mDiem)
						.build();

				MyKey key = MyKey.newBuilder().setKey(model.getSBD()).build();
				RedisClient.shareInstance().set(key.toByteArray(),
						model.toByteArray());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private double castDiem(String diem) {
		return Double.parseDouble(diem.replace("NA", "-1"));
	}

	private DoiTuongUuTien castDT(String dtut) {
		if (dtut.equals("NDT1"))
			return DoiTuongUuTien.NDT1;
		else if (dtut.equals("NDT1"))
			return DoiTuongUuTien.NDT2;
		return DoiTuongUuTien.KHONG;
	}

	private UuTien castUT(String data) {
		return data.equals("Khong") ? UuTien.KHONG_UT : UuTien.CO_UT;
	}

	private KhuVuc castKV(String khuvuc) {

		if (khuvuc.equals("KV1"))
			return KhuVuc.KV1;
		if (khuvuc.equals("KV2"))
			return KhuVuc.KV2;
		if (khuvuc.equals("KV2-NT"))
			return KhuVuc.KV2_NT;
		else
			return KhuVuc.KV3;
	}
}