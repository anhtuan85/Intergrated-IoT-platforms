package kr.re.keti.ncube.httpclient;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.X509TrustManager;

public class HttpsTrustManager implements X509TrustManager {

	@Override
	public void checkClientTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public void checkServerTrusted(X509Certificate[] arg0, String arg1)
			throws CertificateException {
		// TODO Auto-generated method stub

	}

	@Override
	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[]{};
	}

}