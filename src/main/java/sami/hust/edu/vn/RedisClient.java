package sami.hust.edu.vn;

import redis.clients.jedis.Jedis;
import sami.hust.edu.vn.ProtoModels.ChiTieuKhoa;
import sami.hust.edu.vn.ProtoModels.DIEM_SV;
import sami.hust.edu.vn.ProtoModels.ListKey;
import sami.hust.edu.vn.ProtoModels.MyKey;
import sami.hust.edu.vn.ProtoModels.NguyenVongSV;

import com.google.protobuf.InvalidProtocolBufferException;

public class RedisClient {

	private static RedisClient _shareInstance;
	private Jedis client;

	public RedisClient() {
		super();
		_shareInstance = this;
		client = new Jedis("192.168.213.128");
	}

	public synchronized static RedisClient shareInstance() {
		if (_shareInstance == null) {
			new RedisClient();
		}
		return _shareInstance;
	}

	private void resetClient() {
		if (client != null) {
			client.shutdown();
			client = null;
		}
		client = new Jedis("192.168.213.128");
	}

	public Jedis getJedisClient() {
		if (client == null)
			resetClient();
		return client;
	}

	public boolean set(String key, String value) {
		if (client != null) {
			try {
				getJedisClient().set(key, value);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			resetClient();
		}
		return false;
	}

	public boolean set(byte[] key, byte[] value) {
		if (client != null) {
			try {
				getJedisClient().set(key, value);
				return true;
			} catch (Exception e) {
				return false;
			}
		} else {
			resetClient();
		}
		return false;
	}

	public String get(String key) {
		if (client != null) {
			try {
				return getJedisClient().get(key);
			} catch (Exception e) {
				resetClient();
			}
		} else {
			resetClient();
		}
		return null;
	}

	public byte[] get(byte[] key) {
		if (client != null) {
			try {
				return getJedisClient().get(key);
			} catch (Exception e) {
				resetClient();
			}
		} else {
			resetClient();
		}
		return null;
	}

	public boolean remove(String key) {
		if (client != null) {
			try {
				getJedisClient().del(key);
				return true;
			} catch (Exception e) {
				resetClient();
			}
		} else {
			resetClient();
		}
		return false;
	}

	public boolean remove(byte[] key) {
		if (client != null) {
			try {
				getJedisClient().del(key);
				return true;
			} catch (Exception e) {
				resetClient();
			}
		} else {
			resetClient();
		}
		return false;
	}

	public void close() {
		if (client != null) {
			client.shutdown();
			client = null;
		}
	}
	public void clearCache(){
		client.flushDB();
	}

	public static void main(String[] args)
			throws InvalidProtocolBufferException {
		String SBD = "TLA060950";
		MyKey key = MyKey.newBuilder().setKey("NV_" + SBD).build();
		System.out.println(NguyenVongSV.parseFrom(
				RedisClient.shareInstance().getJedisClient()
						.get(key.toByteArray())).toString());
		MyKey keylist = MyKey.newBuilder().setKey("ALL_NVSV").build();
		System.out.println("SLSVDK:");
		System.out.println(ListKey.parseFrom(
				RedisClient.shareInstance().getJedisClient()
						.get(keylist.toByteArray())).getKeyCount());

		MyKey keydiem = MyKey.newBuilder().setKey(SBD).build();
		System.out.println(DIEM_SV.parseFrom(
				RedisClient.shareInstance().getJedisClient()
						.get(keydiem.toByteArray())).toString());
		MyKey keykhoa = MyKey.newBuilder().setKey("KT23").build();
		System.out.println(ChiTieuKhoa.parseFrom(
				RedisClient.shareInstance().getJedisClient()
						.get(keykhoa.toByteArray())).getDiemChuan());
		System.out.println(ChiTieuKhoa.parseFrom(
				RedisClient.shareInstance().getJedisClient()
						.get(keykhoa.toByteArray())).toString());
	}
}