package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

import java.util.Vector;

/**
 * MobilePrjMembers的JSON格式
 * 
 * @author waka
 *
 */
public class MobilePrjMembers {
	public String status = "";
	public Vector<Message> messageVector = new Vector<MobilePrjMembers.Message>();

	/**
	 * 定义内部类Message,需要静态类，要不在解析JSON调用时会出错
	 * 在Java中，类中的静态方法不能直接调用动态方法。只有将某个内部类修饰为静态类，然后才能够在静态类中调用该类的成员变量与成员方法。
	 */
	public static class Message {
		public int id = -1;
		public String createdAt = "";
		public String updatedAt = "";
		public String del = "";
		public String account = "";
		public String icon = "";
		public String status = "";
		public String type = "";
		public String cellphone = "";
		public String qq = "";
		public String address = "";
		public String email = "";
		public String gender = "";
		public String joinPlat = "";
		public String receiveMail = "";
		public String userName = "";
		public String userlevel = "";
		public String remark = "";
		public String createdAtStr = "";
		public String updatedAtStr = "";

		// 构造方法
		public Message() {

		}

		@Override
		public String toString() {
			return "[Message {id=" + id + ", createdAt=" + createdAt
					+ ", updatedAt=" + updatedAt + ", del=" + del
					+ ", account=" + account + ", icon=" + icon + ", status="
					+ status + ", type=" + type + ", cellphone=" + cellphone
					+ ", qq=" + qq + ", address=" + address + ", email="
					+ email + ", gender=" + gender + ", joinPlat=" + joinPlat
					+ ", receiveMail=" + receiveMail + ", userName=" + userName
					+ ", userlevel=" + userlevel + ", remark=" + remark
					+ ", createdAtStr=" + createdAtStr + ", updatedAtStr="
					+ updatedAtStr + "}]";
		}
	}
	
	@Override
	public String toString() {
		String  toString = "MobilePrjMembers :>> {status=" + status + ", messageVector={";
		int length = messageVector.size();
		if(length < 1){
			return toString = toString + "[]}}";	
		}
		for(int i = 0; i < length - 1 ;i++ ){
			toString = toString + messageVector.get(i).toString() + ",\n >>>>>> \n ";
		}
		toString = toString + messageVector.get(length - 1).toString() + "}";	
        return toString;
	}
}
