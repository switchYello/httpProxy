package com.utils;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLException;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.List;

public class ContextSSLFactory {

	private ContextSSLFactory() {
	}

	private static List<String> ciphers = Arrays.asList("ECDHE-RSA-AES128-SHA", "ECDHE-RSA-AES256-SHA", "AES128-SHA", "AES256-SHA", "DES-CBC3-SHA", "TLS_AES_128_GCM_SHA256", "TLS_AES_256_GCM_SHA384");

	public static SslContext getSslContextClient() {
		try {
			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(ResourceManager.gerResourceForFile("cChat.jks"), "4512357896".toCharArray());

			//信任管理器(信任列表的客户端才会被接受)
			TrustManagerFactory tf = TrustManagerFactory.getInstance("SunX509");
			tf.init(keyStore);

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, "4512357896".toCharArray());

			return SslContextBuilder
					.forClient()
					.keyManager(keyManagerFactory)
					.trustManager(tf)
					.protocols("TLSv1.3")
					.ciphers(ciphers)
					.sslProvider(SslProvider.OPENSSL)
					.build();

		} catch (Exception e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("无法创建SslContext");
	}

	public static SslContext getServiceTest() {
		try {
			SelfSignedCertificate self = new SelfSignedCertificate();
			return SslContextBuilder.forServer(self.certificate(), self.privateKey())
					//.sslProvider(SslProvider.OPENSSL)
					.build();
		} catch (CertificateException | SSLException e) {
			e.printStackTrace();
		}
		throw new IllegalArgumentException("无法创建SslContext");
	}


}
