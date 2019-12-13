package com.meari.echoshow.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.EntityBuilder;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.*;

public class HttpUtil {


	public String requestGet(String urlWithParams) throws Exception {
		HttpEntity entity;
		HttpGet httpget = new HttpGet(urlWithParams);
		CloseableHttpResponse response;
		String jsonStr = "";
		try{
			 SSLContext sslcontext = SSLContexts.createSystemDefault();
			 SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		                sslcontext,
		                new String[] { "TLSv1" },
		                null,
		                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		                CloseableHttpClient httpclient = HttpClients.custom()
		                .setSSLSocketFactory(sslsf)
		                .build();
			
			//CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
					.setConnectTimeout(5000).setSocketTimeout(5000).build();
			httpget.setConfig(requestConfig);
			response = httpclient.execute(httpget);
			Log4jUtil.getLog(HttpUtil.class).info("StatusCode ->" + response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity);
		}catch(Exception e){
			Log4jUtil.getLog(HttpUtil.class).error("<requestGet result>",e);
			throw new Exception();
		}finally{
			httpget.releaseConnection();
		}
		Log4jUtil.getLog(HttpUtil.class).info("http result ->" + response.getStatusLine().getStatusCode());
		return jsonStr;
	}
	public String requestGetLonger(String urlWithParams) throws Exception {
		HttpEntity entity;
		HttpGet httpget = new HttpGet(urlWithParams);
		CloseableHttpResponse response;
		String jsonStr = "";
		try{
			 SSLContext sslcontext = SSLContexts.createSystemDefault();
			 SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		                sslcontext,
		                new String[] { "TLSv1" },
		                null,
		                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		                CloseableHttpClient httpclient = HttpClients.custom()
		                .setSSLSocketFactory(sslsf)
		                .build();
			
			//CloseableHttpClient httpclient = HttpClientBuilder.create().build();
			RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(20000)
					.setConnectTimeout(20000).setSocketTimeout(20000).build();
			httpget.setConfig(requestConfig);
			response = httpclient.execute(httpget);
			Log4jUtil.getLog(HttpUtil.class).info("StatusCode ->" + response.getStatusLine().getStatusCode());
			entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity);
		}catch(Exception e){
			Log4jUtil.getLog(HttpUtil.class).error("<requestGet result>",e);
			throw new Exception();
		}finally{
			httpget.releaseConnection();
		}
		Log4jUtil.getLog(HttpUtil.class).info("http result ->" + response.getStatusLine().getStatusCode());
		return jsonStr;
	}

    public String requestPost(String url,List<NameValuePair> params,JSONObject bodyDate) throws ClientProtocolException,IOException {
        SSLContext sslcontext = SSLContexts.createSystemDefault();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                sslcontext,
                new String[] { "TLSv1" },
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier());
        CloseableHttpClient httpclient = HttpClients.custom()
                .setSSLSocketFactory(sslsf)
                .build();
        //CloseableHttpClient httpclient = HttpClientBuilder.create().build();

        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-Type", "application/json");
        if(params != null){
            httppost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
        }
        if(bodyDate != null) {
            HttpEntity reqEntity = EntityBuilder.create().setContentType(ContentType.create("text/plain", "UTF-8")).setText(bodyDate.toJSONString())
                    .build();
            httppost.setEntity(reqEntity);
        }
        CloseableHttpResponse  response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity, "utf-8");
        Log4jUtil.getLog(HttpUtil.class).info("StatusCode ->" + response.getStatusLine().getStatusCode());
        httppost.releaseConnection();
        return jsonStr;
    }


	
	public static void main(String[] args) {

        //System.out.println(JSONObject.parse("1fiwefh"));
        /*UUID uuid = UUID.randomUUID();
        System.out.println(uuid);*/
        //System.out.println(SymmetricEncryptionUtil.decrypt("FCEEB1B960E91A5AD1EBC29C6B03000B","test"));
		/*try {
			String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALsE8RCWmDZHnnwc\n" +
					"fqCXRIrk+Xu7uD/0A+QVmx/dtjBhKrZhZM8UUtdgvy4axS3HfhKq842Do3VnRxFt\n" +
					"LkCw+b27+9t47xUyRmE8ES62t05oHwcos+woGPDXI6BA6XaA8/BFCJ8q2qe033F2\n" +
					"dfAGybssqiMDFnL2ZwMSl3eeVw3LAgMBAAECgYBA9dp2FJkSYZYfLhad2W0pgZdW\n" +
					"70F0lWG0m/yh+PULoPShHPtTIpAT17sQXKQY3q5f9Zlbkln4scLxvqbOOarFMGsQ\n" +
					"Wk414dloFv52DkMMxvU348SNtXRhWtxhj6bAzf86YZbDBSU/hRJHm5ojKToth9D3\n" +
					"iIqarkmpvBPKGCIOgQJBAOnYaOspE17pIi4n978h36nOPDQVvlff3mPYdQ8rRNVO\n" +
					"AsZHO+EPKJ1kqd9+l9Y2AM/pAtOg3e2jTDO+u8r+AbsCQQDMvNT2PakeLfJxebB1\n" +
					"esSvFrt4XndLI4nYN3YXVY7HCuk2aM8RviEBauSVMTT5l6fOiLlWKngCjY3fAmim\n" +
					"/hsxAkEA2mjZXcLIp94BobCM0gT2XSgVzOP+Gx8qHIGAnAFi0gOBzeWchNFestrh\n" +
					"WrBnapiX25ibnkPi5GCf/LiJt8fUnwJAfT6K3HPN7/tVk5tImiVXCnDSkAjh2yJL\n" +
					"YexTnmSlahe/oJwvCACSOkbEvIuUh5cBfXPgRNjH/2/OuPzFPlq2EQJBAK2rOxUE\n" +
					"iyxIJJn4B4WltBH4JK5gBWbiB/VNc4mbwhjio/N69eWU5+60S07VKzvY3B8WdRLs\n" +
					"Dy5MPEGn1rOTAw4=";
			*//*String key = RSA.decrypt(Bytes2HexStringUtil.hexStringToByte("0ff925ec1728942514252d487867bf1165d483dede5b3ef28dbbcf7def6258067dacce72186b1cc6425497e2227e2c6992b7a97e2f4e435ffe26889cea0d732b25061d18a9697ae65bcbfa7a6b98da65f11fa27c1fcaba6afd745bd626d376886869284fb215bb0af760e12a73a1b81793636987a9fff255c33b1d2e1695c45a".toUpperCase()),
					privateKey
					 );*//*
			String key = RSA.decrypt(Bytes2HexStringUtil.hexStringToByte("469b5abe404f21ad49e21295bd95e614a99e2bf902204b7676f18078c4c10343f7ab7c9510edd1968b8e56052cfbfb04e030f394ade9138bbc46d40a8072d4c793e3a4a7134413c3405fd7d0d10f5462884e899b353df9695158e28091c1cd2175b0d3fc1bfb66e941e727f7bddf1545fafbd77081c4bdae39c2a613b361f109".toUpperCase()),privateKey);
			System.out.println(Bytes2HexStringUtil.bytesToHexString(Base64.decode("RptavkBPIa1J4hKVvZXmFKmeK/kCIEt2dvGAeMTBA0P3q3yVEO3RlouOVgUs+/sE4DDzlK3pE4u8RtQKgHLUx5PjpKcTRBPDQF/X0NEPVGKITombNT35aVFY4oCRwc0hdbDT/Bv7ZulB5yf3vd8VRfr713CBxL2uOcKmE7Nh8Qk=")));


			System.out.print(key);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		String pushCmd = "d:\\application\\ffmpeg\\ffmpeg-20180424-d06b01f-win64-static\\bin\\ffmpeg.exe -rtsp_transport tcp -stream_loop -1 -i rtsp://184.72.239.149/vod/mp4://BigBuckBunny_175k.mov  -vcodec copy -acodec copy  -rtsp_transport tcp -f rtsp rtsp://www.windhome.me:8554/camera/cc/";
		ArrayList<Thread> threads = new ArrayList<>();
		for(int i=0;i<20;i++){
		    try{
                if(i%5 == 0){
                    //Thread.sleep(3000);
                }
            }catch(Exception e){

            }

			Thread thread = new Thread() {
				public void run(){
					String channel = String.valueOf(this.getId());
					System.out.println(channel);
					Runtime runtime = Runtime.getRuntime();
					try{
						Process process = runtime.exec(pushCmd + channel);

					}catch(Exception e){
						System.out.println(e.getMessage());
					}

				}
			};
			//threads.add(thread);
			thread.start();
		}
		/*try{
            System.out.println(DesUtils.encode("8TjvVsFBLsc/k79tXLjBIw=="));
        }catch(Exception e){

        }*/


				/*try{
					System.in.read();
				}catch(Exception e){
					threads.forEach(subThread -> subThread.interrupt());
				}

				threads.forEach(subThread -> subThread.interrupt());*/




	}

}


