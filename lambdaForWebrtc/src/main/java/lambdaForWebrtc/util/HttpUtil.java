package lambdaForWebrtc.util;

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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HttpUtil {

	private static RequestConfig requestConfig;
	private static RequestConfig longRequestConfig;
	private static CloseableHttpClient httpclient;

	static {
		SSLContext sslcontext = SSLContexts.createSystemDefault();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslcontext,
				new String[] { "TLSv1" },
				null,
				SSLConnectionSocketFactory.getDefaultHostnameVerifier());
		httpclient = HttpClients.custom()
				.setSSLSocketFactory(sslsf)
				.build();

		requestConfig = RequestConfig.custom().setConnectionRequestTimeout(20000)
				.setConnectTimeout(20000).setSocketTimeout(20000).build();
		longRequestConfig = RequestConfig.custom().setConnectionRequestTimeout(5000)
				.setConnectTimeout(5000).setSocketTimeout(5000).build();
	}


	public static String requestGet(String urlWithParams) throws Exception {
		return requestGetInner(urlWithParams,requestConfig);
	}
	public static String requestGetLonger(String urlWithParams) throws Exception {
		return requestGetInner(urlWithParams,longRequestConfig);
	}

	private static String requestGetInner(String urlWithParams,RequestConfig requestConfig) throws Exception {
		HttpEntity entity;
		HttpGet httpget = new HttpGet(urlWithParams);
		CloseableHttpResponse response;
		String jsonStr = "";
		try{
			httpget.setConfig(requestConfig);
			response = httpclient.execute(httpget);
			entity = response.getEntity();
			jsonStr = EntityUtils.toString(entity);
		}catch(Exception e){
			throw new Exception();
		}finally{
			httpget.releaseConnection();
		}
		return jsonStr;
	}

	public static String requestPostUrlEncodedForm(String url,JSONObject params) throws IOException {

		List<NameValuePair> postParam = new ArrayList<>();
		params.forEach((s, o) -> postParam.add(new BasicNameValuePair(s,(String)o)));
		return requestPost(url,postParam,null,null,null);
	}


	public static String requestPostApplicationJson(String url,JSONObject params,JSONObject headers) throws IOException {

		List<NameValuePair> postParam = new ArrayList<>();
		//params.forEach((s, o) -> postParam.add(new BasicNameValuePair(s,(String)o)));
		List<BasicHeader> postHeaders = new ArrayList<>();
		headers.forEach((s, o) -> postHeaders.add(new BasicHeader(s,(String)o)));
		return requestPost(url,null,null,params,postHeaders);
	}

	public static String requestPostApplicationJson(String url,JSONObject params) throws IOException {

		List<NameValuePair> postParam = new ArrayList<>();
		return requestPost(url,null,null,params,null);
	}

    private static String requestPost(String url,List<NameValuePair> params,JSONObject bodyTextDate,JSONObject jsonData,List<BasicHeader> headers) throws IOException {

        HttpPost httppost = new HttpPost(url);
        httppost.setHeader("Accept", "application/json");
        if(headers != null){
        	Header[] postHeaders = new Header[headers.size()];
        	httppost.setHeaders(headers.toArray(postHeaders));
		}

		if(jsonData != null){
			httppost.setHeader("Content-Type", "application/json");
			HttpEntity reqEntity = EntityBuilder.create().setContentType(ContentType.create("application/json", "UTF-8")).setText(jsonData.toJSONString())
					.build();
			httppost.setEntity(reqEntity);
		}
        if(params != null){
            httppost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));
        }
        if(bodyTextDate != null) {
            HttpEntity reqEntity = EntityBuilder.create().setContentType(ContentType.create("text/plain", "UTF-8")).setText(bodyTextDate.toJSONString())
                    .build();
            httppost.setEntity(reqEntity);
        }
        CloseableHttpResponse  response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();
        String jsonStr = EntityUtils.toString(entity, "utf-8");
        httppost.releaseConnection();
        return jsonStr;
    }

}


