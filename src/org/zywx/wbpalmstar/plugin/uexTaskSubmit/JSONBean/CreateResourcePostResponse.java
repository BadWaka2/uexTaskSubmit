package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

/**
 * 创建资源请求返回Bean类
 * 
 * @author waka
 *
 */
public class CreateResourcePostResponse {
	public String status = "";
	public Message message = new Message();
	public String message_failed = "";

	public static class Message {
		public String id = "";
		public String createdAt = "";
		public String updatedAt = "";
		public String del = "";
		public String name = "";
		public String type = "";
		public String parentId = "";
		public String userId = "";
		public String userName = "";
		public String projectId = "";
		public String fileSize = "";
		public String filePath = "";
		public String sizeStr = "";
		public String createdAtStr = "";
		public String updatedAtStr = "";

		// 构造方法
		public Message() {

		}

		@Override
		public String toString() {
			return "Message [id=" + id + ", createdAt=" + createdAt + ", updatedAt=" + updatedAt + ", del=" + del
					+ ", name=" + name + ", type=" + type + ", parentId=" + parentId + ", userId=" + userId
					+ ", userName=" + userName + ", projectId=" + projectId + ", fileSize=" + fileSize + ", filePath="
					+ filePath + ", sizeStr=" + sizeStr + ", createdAtStr=" + createdAtStr + ", updatedAtStr="
					+ updatedAtStr + "]";
		}
	}
}
