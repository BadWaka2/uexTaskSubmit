package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

import java.util.Vector;

/**
 * MobileProcess的JSON格式
 * 
 * @author waka
 *
 */
public class MobileProcess {
	public String status = "";
	public Vector<Message> messageVector = new Vector<MobileProcess.Message>();

	/**
	 * 定义内部类Message,需要静态类，要不在解析JSON调用时会出错
	 * 在Java中，类中的静态方法不能直接调用动态方法。只有将某个内部类修饰为静态类，然后才能够在静态类中调用该类的成员变量与成员方法。
	 */
	public static class Message {
		public int id = -1;
		public String createdAt = "";
		public String updatedAt = "";
		public String del = "";
		public String name = "";
		public String detail = "";
		public int weight = -1;
		public String startDate = "";
		public String endDate = "";
		public int projectId = -1;
		public int progress = -1;
		public int resourceTotal = -1;
		public int memberTotal = -1;
		public int taskTotal = -1;
		public String createdAtStr = "";
		public String updatedAtStr = "";

		// 构造方法
		public Message() {

		}

		@Override
		public String toString() {
			return "[Message {id=" + id + ", createdAt=" + createdAt
					+ ", updatedAt=" + updatedAt + ", del=" + del + ", name="
					+ name + ", detail=" + detail + ", weight=" + weight
					+ ", startDate=" + startDate + ", endDate=" + endDate
					+ ", projectId=" + projectId + ", progress=" + progress
					+ ", resourceTotal=" + resourceTotal + ", memberTotal="
					+ memberTotal + ", taskTotal=" + taskTotal
					+ ", createdAtStr=" + createdAtStr + ", updatedAtStr="
					+ updatedAtStr + "}]";
		}
	}

	@Override
	public String toString() {
		String  toString = "MobileProcess :>> {status=" + status + ", messageVector={";
		int length = messageVector.size();
		if(length < 1){
			return toString = toString + "}";	
		}
		for(int i = 0; i < length - 1 ;i++ ){
			toString = toString + messageVector.get(i).toString() + ", ";
		}
		toString = toString + messageVector.get(length - 1).toString() + "}";	
        return toString;
	}
}

