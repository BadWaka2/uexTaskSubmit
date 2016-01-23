package org.zywx.wbpalmstar.plugin.uexTaskSubmit.JSONBean;

/**
 * 登录成功返回登录信息JavaBean
 * 
 * @author waka
 *
 */
public class UserLoginResponse {
	public String status = "";
	public String statusInfo = "";
	public Message message = new Message();

	public static class Message {
		public Permissions permissions = new Permissions();
		public Object object = new Object();

		public Message() {

		}
	}

	public static class Permissions {
		public String team_create = "";
		public String project_create = "";

		public Permissions() {

		}
	}

	public static class Object {
		public String id = "";
		public String userid = "";
		public String createdAt = "";
		public String updatedAt = "";
		public String del = "";
		public String account = "";
		public String password = "";
		public String icon = "";
		public String status = "";
		public String type = "";
		public String cellphone = "";
		public String address = "";
		public String qq = "";
		public String email = "";
		public String joinPlat = "";
		public String receiveMail = "";
		public String userName = "";
		public String userlevel = "";
		public String teamCreator = "";
		public String createdAtStr = "";
		public String updatedAtStr = "";

		public Object() {

		}
	}

	//@formatter:off
	public String toString() {
		return "status----->" + status + "\n" 
				+ "message.permissions.team_create----->"+ message.permissions.team_create + "\n" 
				+ "message.permissions.project_create----->"+ message.permissions.project_create + "\n"
				+ "message.object.id----->"+ message.object.id + "\n"
				+ "message.object.updatedAt----->"+ message.object.updatedAt + "\n"
				+ "message.object.del----->"+ message.object.del + "\n"
				+ "message.object.account----->"+ message.object.account + "\n"
				+ "message.object.password----->"+ message.object.password + "\n"
				+ "message.object.icon----->"+ message.object.icon + "\n"
				+ "message.object.status----->"+ message.object.status + "\n"
				+ "message.object.type----->"+ message.object.type + "\n"
				+ "message.object.cellphone----->"+ message.object.cellphone + "\n"
				+ "message.object.address----->"+ message.object.address + "\n"
				+ "message.object.email----->"+ message.object.email + "\n"
				+ "message.object.joinPlat----->"+ message.object.joinPlat + "\n"
				+ "message.object.receiveMail----->"+ message.object.receiveMail + "\n"
				+ "message.object.userName----->"+ message.object.userName + "\n"
				+ "message.object.userlevel----->"+ message.object.userlevel + "\n"
				+ "message.object.teamCreator----->"+ message.object.teamCreator + "\n"
				+ "message.object.updatedAtStr----->"+ message.object.updatedAtStr + "\n"
				;
	}
}
