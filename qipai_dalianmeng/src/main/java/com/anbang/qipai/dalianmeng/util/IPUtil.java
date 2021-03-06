package com.anbang.qipai.dalianmeng.util;

import javax.servlet.http.HttpServletRequest;
import java.net.*;
import java.util.Enumeration;

public class IPUtil {

	/**
	 * 获取真实ip
	 */
	public static String getRealIp(HttpServletRequest request) {
		String ip;
		ip = request.getHeader("X-Real-IP");
		if (ip == null) {
			String xip = request.getHeader("x-forwarded-for");
			if (xip != null) {
				String[] ips = xip.split(",");
				ip = ips[0];
			} else {
				ip = request.getRemoteAddr();
			}
		}
		return ip;
	}

	/**
	 * 返回本机外网ip地址，如果没有外网ip就返回内网ip
	 */
	public static String getLocalHostRelIP() throws UnknownHostException, SocketException {
		String INTRANET_IP = InetAddress.getLocalHost().getHostAddress(); // 内网IP

		Enumeration<NetworkInterface> networks = NetworkInterface.getNetworkInterfaces();
		InetAddress ip = null;
		Enumeration<InetAddress> addrs;
		while (networks.hasMoreElements()) {
			addrs = networks.nextElement().getInetAddresses();
			while (addrs.hasMoreElements()) {
				ip = addrs.nextElement();
				if (ip != null && ip instanceof Inet4Address && ip.isSiteLocalAddress()
						&& !ip.getHostAddress().equals(INTRANET_IP)) {
					return ip.getHostAddress();
				}
			}
		}

		// 如果没有外网IP，就返回内网IP
		return INTRANET_IP;
	}
}
