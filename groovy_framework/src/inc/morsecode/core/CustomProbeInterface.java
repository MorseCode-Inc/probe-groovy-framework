package inc.morsecode.core;

import java.util.List;

import inc.morsecode.NDS;

import com.nimsoft.nimbus.NimException;
import com.nimsoft.nimbus.NimRequest;

public interface CustomProbeInterface {

	public static final String NIM_ROBOT_NAME = "robotname";
	public static final String NIM_OS_DESCRIPTION = "os_description"; // Linux 2.6.32-279.el6.x86_64 #1 SMP Fri Jun 22 12:19:21 UTC 2012 x86_64
	public static final String NIM_HUB_NAME = "hubname"; // redfish
	public static final String NIM_TIMEZONE_NAME = "timezone_name"; // MST
	public static final String NIM_WORKDIR = "workdir"; // /opt/nimsoft
	public static final String NIM_ACCESS_0 = "access_0"; // 0
	public static final String NIM_ACCESS_1 = "access_1"; // 1
	public static final String NIM_ACCESS_2 = "access_2"; // 2
	public static final String NIM_ACCESS_3 = "access_3"; // 3
	public static final String NIM_ACCESS_4 = "access_4"; // 4
	public static final String NIM_REQUESTS = "requests"; // 5806
	public static final String NIM_HUB_DNS_NAME = "hub_dns_name"; // redfish.home
	public static final String NIM_LOG_FILE = "log_file"; // controller.log
	public static final String NIM_DOMAIN = "domain"; // UIM
	public static final String NIM_LICENSE = "license"; // 1
	public static final String NIM_ROBOT_MODE = "robot_mode"; // 0
	public static final String NIM_SPOOLPORT = "spoolport"; // 48001
	public static final String NIM_HUB_ROBOT_NAME = "hubrobotname"; // _hub
	public static final String NIM_ORIGIN = "origin"; // redfish
	public static final String NIM_UPTIME = "uptime"; // 179706
	public static final String NIM_CURRENT_TIME = "current_time"; // 1432501998
	public static final String NIM_ROBOT_IP = "robotip"; // 10.14.47.133
	public static final String NIM_OS_USER1 = "os_user1"; // 
	public static final String NIM_OS_USER2 = "os_user2"; // 
	public static final String NIM_LAST_INST_CHANGE = "last_inst_change"; // 1427955964
	public static final String NIM_OS_MAJOR = "os_major"; // UNIX
	public static final String NIM_OS_VERSION = "os_version"; // 2.6
	public static final String NIM_TIMEZONE_DIFF = "timezone_diff"; // 25200
	public static final String NIM_SOURCE = "source"; // centos-java-01
	public static final String NIM_HUB_IP = "hubip"; // 10.14.47.129
	public static final String NIM_ROBOT_DEVICE_ID = "robot_device_id"; // DAEA3E72CD247129F42734954C9E4DCC5
	public static final String NIM_NIM_STARTED = "started"; // 1432322292
	public static final String NIM_LOG_LEVEL = "log_level"; // 3
	public static final String NIM_OS_MINOR = "os_minor"; // Linux

	public abstract void refreshConfiguration() throws NimException;

	public abstract void bootstrap() throws NimException;

	public abstract NDS updateControllerInfo() throws NimException;

	public abstract void shutdown();

	public abstract String getSubsystemId();

	public abstract NDS getQoSDefinitions();

	public abstract String getRobotName();

	public abstract String getSource();

	public abstract NDS call(String address, String command, int retries, String[]... params) throws NimException;

	public abstract NDS getMessage(String name);
	

}