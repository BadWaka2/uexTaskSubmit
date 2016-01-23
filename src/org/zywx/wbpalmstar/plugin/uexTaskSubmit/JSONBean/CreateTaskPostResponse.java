package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

public class CreateTaskPostResponse {
	public String status = "";
	public Message message = new Message();

	public static class Message {
		public String id = "";
		public String createdAt = "";
		public String updatedAt = "";
		public String del = "";
		public String name = "";
		public String detail = "";
		public String processId = "";
		public String appId = "";
		public String repeatable = "";
		public String priority = "";
		public String status = "";
		public String lastStatusUpdateTime = "";
		public String progress = "";
		public String deadline = "";
		public String resourceTotal = "";
		public String commentTotal = "";
		public String processName = "";
		public String projectName = "";
		public String appName = "";
		public String projectId = "";
		public String createdAtStr = "";
		public String updatedAtStr = "";

		// 构造方法
		public Message() {

		}

		@Override
		public String toString() {
			return "Message [id=" + id + "\n" 
					+ ", createdAt=" + createdAt + "\n"
					+ ", updatedAt=" + updatedAt + "\n"
					+ ", del=" + del + "\n" 
					+ ", name=" + name + "\n" 
					+ ", detail=" + detail + "\n" 
					+ ", processId="+ processId + "\n"
					+ ", appId=" + appId + "\n" 
					+ ", priority=" + priority + "\n" 
					+ ", repeatable="+ repeatable + "\n" 
					+ ", status=" + status + "\n" 
					+ ", lastStatusUpdateTime=" + lastStatusUpdateTime + "\n" 
					+ ", progress=" + progress + "\n" 
					+ ", deadline=" + deadline + "\n" 
					+ ", resourceTotal=" + resourceTotal + "\n" 
					+ ", commentTotal=" + commentTotal + "\n" 
					+ ", processName=" + processName + "\n" 
					+ ", projectName=" + projectName + "\n" 
					+ ", appName=" + appName + "\n" 
					+ ", projectId=" + projectId + "\n" 
					+ ", createdAtStr=" + createdAtStr + "\n" 
					+ ", updatedAtStr=" + updatedAtStr + "\n" 
					+ "]";
		}
	}
}
