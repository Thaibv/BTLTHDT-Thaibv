package sami.hust.edu.vn;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa.DiemXet;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa.DiemXet.NVXet;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.NguyenVong;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV.NguyenVong.TrangThai;

import com.google.protobuf.InvalidProtocolBufferException;

public class Process {
	static ArrayList<String> mlList = new ArrayList<String>();
	static ArrayList<String> dsTruot = new ArrayList<String>();
	
	private List<String> LayNVSV() {
		MyKey keylist = MyKey.newBuilder().setKey("ALL_NVSV").build();
		try {
			return ListKey.parseFrom(
					RedisClient.shareInstance().getJedisClient()
							.get(keylist.toByteArray())).getKeyList();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
	}

	public static void main(String[] args) {
		System.out.println("ResetData");
		new ReadCSV_DBKhoa().run();
		new ReadCSV_DBDangKyNV().run();
		System.out.println("Start Process");
		long time = System.currentTimeMillis();
		mlList.addAll(new Process().LayNVSV());
		int k = 0;
		int sl = 0;
		while (k < 4 || sl != mlList.size()) {
			sl = mlList.size();
			System.out.println(sl);
			ArrayList<String> ds = new ArrayList<String>();
			ds.addAll(mlList);
			for (String sbd : ds) {
				SapXepKhoa(sbd.replace("NV_", ""));
			}
			if (sl == mlList.size())
				k++;
			else
				k = 0;
		}

		time = System.currentTimeMillis() - time;
		MyKey key = MyKey.newBuilder().setKey("DSSVTruot").build();
		ListKey dstr=ListKey.newBuilder().addAllKey(dsTruot).build();
		RedisClient.shareInstance().set(key.toByteArray(), dstr.toByteArray());
		System.out.println("Finish Process: " + time);
		WriteResult();

	}

	private static void SapXepKhoa(String sbd) {
		MyKey key = MyKey.newBuilder().setKey("NV_" + sbd).build();
		try {
			NguyenVongSV nvsv = NguyenVongSV.parseFrom(RedisClient
					.shareInstance().get(key.toByteArray()));
			if (nvsv.getNV1().getTT() == TrangThai.CHUA_XET) {
				nvsv = NguyenVongSV.newBuilder(nvsv)
						.setNV1(XetNV(nvsv.getNV1(), sbd, 1)).build();
			} else if (nvsv.getNV1().getTT() == TrangThai.TRUNG_TUYEN)
				return;
			else if (nvsv.getNV2() == null) {
				XepXong(sbd);
				return;
			} else if (nvsv.getNV2().getTT() == TrangThai.CHUA_XET) {
				nvsv = NguyenVongSV.newBuilder(nvsv)
						.setNV2(XetNV(nvsv.getNV2(), sbd, 2)).build();
			} else if (nvsv.getNV2().getTT() == TrangThai.TRUNG_TUYEN)
				return;
			else if (nvsv.getNV3() == null) {
				XepXong(sbd);
				return;
			} else if (nvsv.getNV3().getTT() == TrangThai.CHUA_XET) {
				nvsv = NguyenVongSV.newBuilder(nvsv)
						.setNV3(XetNV(nvsv.getNV3(), sbd, 3)).build();
			} else if (nvsv.getNV3().getTT() == TrangThai.TRUNG_TUYEN)
				return;
			else if (nvsv.getNV4() == null) {
				XepXong(sbd);
				return;
			} else if (nvsv.getNV4().getTT() == TrangThai.CHUA_XET) {
				nvsv = NguyenVongSV.newBuilder(nvsv)
						.setNV4(XetNV(nvsv.getNV4(), sbd, 4)).build();
			} else if (nvsv.getNV4().getTT() == TrangThai.TRUNG_TUYEN)
				return;
			else {
				XepXong(sbd);
				return;
			}
			RedisClient.shareInstance().set(key.toByteArray(),
					nvsv.toByteArray());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
	}

	private static void WriteResult() {
		new WriteExellDSXetTuyen().run();
		new WriteExellDiemChuan().run();
		new WriteExellDSPhoDiem().run();
		new WriteExellDSTrungTuyen().run();
		new WriteExellDSTruot().run();
		System.out.println("Please check the result file under d:/DATN/Result");

	}

	private static void XepXong(String sbd) {
		mlList.remove("NV_" + sbd);
		dsTruot.add(sbd);
	}

	private static NguyenVong XetNV(NguyenVong nv, String sbd, int nvs) {
		MyKey key = MyKey.newBuilder().setKey(nv.getMaNganh()).build();
		try {
			ChiTieuKhoa mKhoa = ChiTieuKhoa.parseFrom(RedisClient
					.shareInstance().get(key.toByteArray()));
			ArrayList<DiemXet> dsdiem = new ArrayList<DiemXet>();
			dsdiem.addAll(mKhoa.getDSDiemXetList());
			int chitieu = mKhoa.getChiTieu();
			int slhientai = mKhoa.getSLHienTai();
			if (slhientai < chitieu) {
				slhientai++;
				dsdiem = addNVSV(dsdiem, nv, sbd, nvs);
				mKhoa = ChiTieuKhoa.newBuilder(mKhoa)
						.setDiemChuan(dsdiem.get(dsdiem.size() - 1).getDiem())
						.setSLHienTai(slhientai).clearDSDiemXet()
						.addAllDSDiemXet(dsdiem).build();
				RedisClient.shareInstance().set(key.toByteArray(),
						mKhoa.toByteArray());
				nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUNG_TUYEN)
						.build();

			} else {
				if (mKhoa.getDiemChuan() < nv.getDiemXet()) {
					slhientai++;
					dsdiem = addNVSV(dsdiem, nv, sbd, nvs);
					int slbot = dsdiem.get(dsdiem.size() - 1).getDSNVXetCount();
					if ((slhientai - slbot) >= chitieu) {
						updateTTNV(dsdiem.get(dsdiem.size() - 1));
						dsdiem.remove(dsdiem.size() - 1);
						slhientai -= slbot;
					}
					mKhoa = ChiTieuKhoa
							.newBuilder(mKhoa)
							.setDiemChuan(
									dsdiem.get(dsdiem.size() - 1).getDiem())
							.setSLHienTai(slhientai).clearDSDiemXet()
							.addAllDSDiemXet(dsdiem).build();

					RedisClient.shareInstance().set(key.toByteArray(),
							mKhoa.toByteArray());
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUNG_TUYEN)
							.build();

				} else if (mKhoa.getDiemChuan() == nv.getDiemXet()) {
					NVXet nvx = NVXet.newBuilder().setDiem(nv.getDiemXet())
							.setNv(nvs).setSbd(sbd).build();
					DiemXet dx = dsdiem.get(dsdiem.size() - 1);
					dsdiem.remove(dx);
					dx = DiemXet.newBuilder(dx).addDSNVXet(nvx).build();
					dsdiem.add(dx);
					slhientai++;
					mKhoa = ChiTieuKhoa
							.newBuilder(mKhoa)
							.setDiemChuan(
									dsdiem.get(dsdiem.size() - 1).getDiem())
							.setSLHienTai(slhientai).clearDSDiemXet()
							.addAllDSDiemXet(dsdiem).build();

					RedisClient.shareInstance().set(key.toByteArray(),
							mKhoa.toByteArray());
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUNG_TUYEN)
							.build();
				} else {
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT)
							.build();

				}

			}

		} catch (InvalidProtocolBufferException e) {
			nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT).build();
			e.printStackTrace();
		}
		return nv;
	}

	private static ArrayList<DiemXet> addNVSV(ArrayList<DiemXet> dsdiem,
			NguyenVong nv, String sbd, int nvs) {
		NVXet nvx = NVXet.newBuilder().setDiem(nv.getDiemXet()).setNv(nvs)
				.setSbd(sbd).build();
		ArrayList<DiemXet> temp = new ArrayList<DiemXet>();
		temp.addAll(dsdiem);
		for (DiemXet diem : temp) {
			if (nv.getDiemXet() == diem.getDiem()) {
				dsdiem.remove(diem);
				diem = DiemXet.newBuilder(diem).addDSNVXet(nvx).build();
				dsdiem.add(diem);
				dsdiem.sort(new Comparator<DiemXet>() {

					public int compare(DiemXet o1, DiemXet o2) {
						if (o1.getDiem() < o2.getDiem())
							return 1;
						else if (o1.getDiem() == o2.getDiem())
							return 0;
						else
							return -1;
					}
				});
				return dsdiem;
			}
		}
		dsdiem.add(DiemXet.newBuilder().setDiem(nv.getDiemXet())
				.addDSNVXet(nvx).build());

		dsdiem.sort(new Comparator<DiemXet>() {

			public int compare(DiemXet o1, DiemXet o2) {
				if (o1.getDiem() < o2.getDiem())
					return 1;
				else if (o1.getDiem() == o2.getDiem())
					return 0;
				else
					return -1;
			}
		});
		return dsdiem;
	}

	private static void updateTTNV(DiemXet ds) {
		for (NVXet mdx : ds.getDSNVXetList()) {
			MyKey key = MyKey.newBuilder().setKey("NV_" + mdx.getSbd()).build();

			try {
				NguyenVongSV nvsv = NguyenVongSV.parseFrom(RedisClient
						.shareInstance().get(key.toByteArray()));
				NguyenVong nv;
				switch (mdx.getNv()) {
				case 1:
					nv = nvsv.getNV1();
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT)
							.build();
					nvsv = NguyenVongSV.newBuilder(nvsv).setNV1(nv).build();
					RedisClient.shareInstance().set(key.toByteArray(),
							nvsv.toByteArray());
					break;
				case 2:
					nv = nvsv.getNV2();
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT)
							.build();
					nvsv = NguyenVongSV.newBuilder(nvsv).setNV2(nv).build();
					RedisClient.shareInstance().set(key.toByteArray(),
							nvsv.toByteArray());
					break;
				case 3:
					nv = nvsv.getNV3();
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT)
							.build();
					nvsv = NguyenVongSV.newBuilder(nvsv).setNV3(nv).build();
					RedisClient.shareInstance().set(key.toByteArray(),
							nvsv.toByteArray());
					break;
				case 4:
					nv = nvsv.getNV4();
					nv = NguyenVong.newBuilder(nv).setTT(TrangThai.TRUOT)
							.build();
					nvsv = NguyenVongSV.newBuilder(nvsv).setNV4(nv).build();
					RedisClient.shareInstance().set(key.toByteArray(),
							nvsv.toByteArray());
					break;
				default:
					break;
				}
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}

		}

	}

}
