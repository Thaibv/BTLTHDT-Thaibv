package sami.hust.edu.vn;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import sami.hust.edu.vn.ProtoModels.DIEM_SV;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.DiemThi;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.DoiTuongUuTien;
import sami.hust.edu.vn.ProtoModels.DIEM_SV.UuTien;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.NguyenVong;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.NguyenVong.TrangThai;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.ToHopXet;

public class ReadCSV_DBDangKyNV {

	private CSVParser records;

	public static void main(String[] args) {

		ReadCSV_DBDangKyNV obj = new ReadCSV_DBDangKyNV();
		obj.run();

	}

	public void run() {
		String csvFile = "E:/TSBachKhoa/TuyenSinh/data/dangkynv-bk.csv";
		Reader in;
		try {
			in = new FileReader(csvFile);
			records = new CSVParser(in, CSVFormat.EXCEL.withHeader());
			ArrayList<String> list = new ArrayList<String>();
			for (CSVRecord record : records) {
				NguyenVong nv1, nv2 = null, nv3 = null, nv4 = null;
				NguyenVongSV model;
				String sbd = record.get("SBD");
				MyKey key = MyKey.newBuilder().setKey(sbd).build();

				DIEM_SV mDiem = DIEM_SV.parseFrom(RedisClient.shareInstance()
						.get(key.toByteArray()));

				nv1 = NguyenVong.newBuilder().setMaNganh(record.get("NV1.N"))
						.setTT(TrangThai.CHUA_XET)
						.setToHopMon(castTHM(record.get("NV1.TH")))
						.setDiemXet(Cal(mDiem, castTHM(record.get("NV1.TH"))))
						.build();

				if (!record.get("NV2.N").toUpperCase().equals("NA")) {

					nv2 = NguyenVong
							.newBuilder()
							.setMaNganh(record.get("NV2.N"))
							.setTT(TrangThai.CHUA_XET)
							.setToHopMon(castTHM(record.get("NV2.TH")))
							.setDiemXet(
									Cal(mDiem, castTHM(record.get("NV2.TH"))))
							.build();
				}

				if (!record.get("NV3.N").toUpperCase().equals("NA"))
					nv3 = NguyenVong
							.newBuilder()
							.setMaNganh(record.get("NV3.N"))
							.setTT(TrangThai.CHUA_XET)
							.setToHopMon(castTHM(record.get("NV3.TH")))
							.setDiemXet(
									Cal(mDiem, castTHM(record.get("NV3.TH"))))
							.build();

				if (!record.get("NV4.N").toUpperCase().equals("NA"))
					nv4 = NguyenVong
							.newBuilder()
							.setMaNganh(record.get("NV4.N"))
							.setTT(TrangThai.CHUA_XET)
							.setToHopMon(castTHM(record.get("NV4.TH")))
							.setDiemXet(
									Cal(mDiem, castTHM(record.get("NV4.TH"))))
							.build();
				if (nv2 == null)
					model = NguyenVongSV.newBuilder().setSBD(record.get("SBD"))
							.setNV1(nv1).build();
				else if (nv3 == null)
					model = NguyenVongSV.newBuilder().setSBD(record.get("SBD"))
							.setNV1(nv1).setNV2(nv2).build();
				else if (nv4 == null)
					model = NguyenVongSV.newBuilder().setSBD(record.get("SBD"))
							.setNV1(nv1).setNV2(nv2).setNV3(nv3).build();
				else
					model = NguyenVongSV.newBuilder().setSBD(record.get("SBD"))
							.setNV1(nv1).setNV2(nv2).setNV3(nv3).setNV4(nv4)
							.build();

				key = MyKey.newBuilder().setKey("NV_" + model.getSBD()).build();
				list.add(key.getKey());

				RedisClient.shareInstance().set(key.toByteArray(),
						model.toByteArray());
			}
			ListKey listnv = ListKey.newBuilder().addAllKey(list).build();
			MyKey key = MyKey.newBuilder().setKey("ALL_NVSV").build();
			RedisClient.shareInstance().set(key.toByteArray(),
					listnv.toByteArray());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private double Cal(DIEM_SV mDiem, ToHopXet castTHM) {
		double avg = 0;
		if (mDiem.getDTUT() == DoiTuongUuTien.NDT1)
			avg += 2.0;
		else if (mDiem.getDTUT() == DoiTuongUuTien.NDT2)
			avg += 1.0;
		if (mDiem.getUT() == UuTien.CO_UT)
			avg += 3.0;
		switch (mDiem.getKV()) {
		case KV1:
			avg += 1.5;
			break;
		case KV2:
			avg += 0.5;
			break;
		case KV2_NT:
			avg += 1.0;
			break;
		case KV3:

			break;
		default:
			break;
		}
		avg = avg / 3;
		DiemThi md = mDiem.getDiem();
		switch (castTHM) {
		case TOAN_HOA_ANH_0:
			avg = avg + (md.getToan() + md.getHoa() + md.getAnh()) / 3;
			break;
		case TOAN_HOA_ANH_1:
			avg = avg + (md.getToan() * 2 + md.getHoa() + md.getAnh()) / 4;
			break;
		case TOAN_LY_ANH_1:
			avg = avg + (md.getToan() * 2 + md.getLy() + md.getAnh()) / 4;
			break;
		case TOAN_HOA_SINH_1:
			avg = avg + (md.getToan() * 2 + md.getHoa() + md.getSinh()) / 4;
			break;
		case TOAN_LY_ANH_0:
			avg = avg + (md.getToan() + md.getLy() + md.getAnh()) / 3;
			break;
		case TOAN_LY_HOA_1:
			avg = avg + (md.getToan() * 2 + md.getHoa() + md.getLy()) / 4;
			break;
		case TOAN_LY_HOA_0:
			avg = avg + (md.getToan() + md.getHoa() + md.getLy()) / 3;
			break;
		case TOAN_VAN_ANH_3:
			avg = avg + (md.getToan() + md.getVan() + md.getAnh() * 2) / 4;
			break;
		case TOAN_LY_PHAP_0:
			avg = avg + (md.getToan() + md.getLy() + md.getPhap()) / 3;
			break;
		case TOAN_VAN_ANH_0:
			avg = avg + (md.getToan() + md.getVan() + md.getAnh()) / 3;
			break;
		case TOAN_VAN_PHAP_0:
			avg = avg + (md.getToan() + md.getVan() + md.getPhap()) / 3;
			break;
		default:
			break;
		}
		return avg;
	}

	private ToHopXet castTHM(String thm) {
		String th = thm.toUpperCase();
		if (th.equals("TOAN,LY,HOA,1"))
			return ToHopXet.TOAN_LY_HOA_1;

		if (th.equals("TOAN,LY,ANH,1"))
			return ToHopXet.TOAN_LY_ANH_1;

		if (th.equals("TOAN,HOA,SINH,1"))
			return ToHopXet.TOAN_HOA_SINH_1;

		if (th.equals("TOAN,HOA,ANH,1"))
			return ToHopXet.TOAN_HOA_ANH_1;
		if (th.equals("TOAN,HOA,ANH,0"))
			return ToHopXet.TOAN_HOA_ANH_0;
		if (th.equals("TOAN,LY,HOA,0"))
			return ToHopXet.TOAN_LY_HOA_0;

		if (th.equals("TOAN,LY,ANH,0"))
			return ToHopXet.TOAN_LY_ANH_0;

		if (th.equals("TOAN,VAN,ANH,0"))
			return ToHopXet.TOAN_VAN_ANH_0;

		if (th.equals("TOAN,VAN,ANH,0"))
			return ToHopXet.TOAN_VAN_ANH_0;

		if (th.equals("ANH,TOAN,VAN,1"))
			return ToHopXet.TOAN_VAN_ANH_3;

		if (th.equals("TOAN,LY,PHAP,0"))
			return ToHopXet.TOAN_LY_PHAP_0;

		if (th.equals("TOAN,VAN,PHAP,0"))
			return ToHopXet.TOAN_VAN_PHAP_0;

		return null;
	}
}