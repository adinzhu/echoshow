package pojo;

/**
 * @author huangqihao
 * @title: UserInfoDTO
 * @description: TODO
 * @date 2019/8/13 10:06
 */
public class UserInfoDTO {
	private String userID;
	private String region;
	private String userDeviceSecret;
	private String sourceApp;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getUserDeviceSecret() {
		return userDeviceSecret;
	}

	public void setUserDeviceSecret(String userDeviceSecret) {
		this.userDeviceSecret = userDeviceSecret;
	}

	public String getSourceApp() {
		return sourceApp;
	}

	public void setSourceApp(String sourceApp) {
		this.sourceApp = sourceApp;
	}

	@Override
	public String toString() {
		return "UserInfoDTO{" +
				"userID='" + userID + '\'' +
				", region='" + region + '\'' +
				", userDeviceSecret='" + userDeviceSecret + '\'' +
				", sourceApp='" + sourceApp + '\'' +
				'}';
	}
}
