
package com.meari.echoshow.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public class RSA{
	
	public static final String  SIGN_ALGORITHMS = "SHA1WithRSA";

	private static final String ALGORITHM = "RSA";

	private static final String DEFAULT_CHARSET = "UTF-8";

	//客户端的js的rsa加密是PKCS#1标准的，java是PKCS#8标准的，在js加密解密成功的秘钥需要转换为PKCS#8标准的秘钥，才能在java上机密成功
	private static String privateKey = "MIICdwIBADANBgkqhkiG9w0BAQEFAASCAmEwggJdAgEAAoGBALsE8RCWmDZHnnwc\n" +
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
	public static String getDefaultPrivateKey(){
		return privateKey;
	}
	public static String sign(String content, String privateKey) {
		try {
			PKCS8EncodedKeySpec priPKCS8 = new PKCS8EncodedKeySpec(
					Base64.decode(privateKey));
			KeyFactory keyf = KeyFactory.getInstance(ALGORITHM);
			PrivateKey priKey = keyf.generatePrivate(priPKCS8);

			java.security.Signature signature = java.security.Signature
					.getInstance(SIGN_ALGORITHMS);

			signature.initSign(priKey);
			signature.update(content.getBytes(DEFAULT_CHARSET));

			byte[] signed = signature.sign();

			return Base64.encode(signed);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}


	
	/**
	* RSA签名
	* @param content 待签名数据
	* @param privateKey 商户私钥
	* @param input_charset 编码格式
	* @return 签名值
	*/
	public static String sign(String content, String privateKey, String input_charset)
	{
        try 
        {
        	PKCS8EncodedKeySpec priPKCS8 	= new PKCS8EncodedKeySpec( Base64.decode(privateKey) ); 
        	KeyFactory keyf 				= KeyFactory.getInstance("RSA");
        	PrivateKey priKey 				= keyf.generatePrivate(priPKCS8);

            java.security.Signature signature = java.security.Signature
                .getInstance(SIGN_ALGORITHMS);

            signature.initSign(priKey);
            signature.update( content.getBytes(input_charset) );

            byte[] signed = signature.sign();
            
            return Base64.encode(signed);
        }
        catch (Exception e) 
        {
        	e.printStackTrace();
        }
        
        return null;
    }
	
	/**
	* RSA验签名检查
	* @param content 待签名数据
	* @param sign 签名值
	* @param ali_public_key 支付宝公钥
	* @param input_charset 编码格式
	* @return 布尔值
	*/
	public static boolean verify(String content, String sign, String ali_public_key, String input_charset)
	{
		try 
		{
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	        byte[] encodedKey = Base64.decode(ali_public_key);
	        PublicKey pubKey = keyFactory.generatePublic(new X509EncodedKeySpec(encodedKey));

		
			java.security.Signature signature = java.security.Signature
			.getInstance(SIGN_ALGORITHMS);
		
			signature.initVerify(pubKey);
			signature.update( content.getBytes(input_charset) );
		
			boolean bverify = signature.verify( Base64.decode(sign));
			return bverify;
			
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	* 解密
	* @param content 密文
	* @param private_key 商户私钥
	* @param input_charset 编码格式
	* @return 解密后的字符串
	*/
	public static String decrypt(String content, String private_key, String input_charset) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);

        InputStream ins = new ByteArrayInputStream(Base64.decode(content));
        ByteArrayOutputStream writer = new ByteArrayOutputStream();
        //rsa解密的字节大小最多是128，将需要解密的内容，按128位拆开解密
        byte[] buf = new byte[128];
        int bufl;

        while ((bufl = ins.read(buf)) != -1) {
            byte[] block = null;

            if (buf.length == bufl) {
                block = buf;
            } else {
                block = new byte[bufl];
                for (int i = 0; i < bufl; i++) {
                    block[i] = buf[i];
                }
            }

            writer.write(cipher.doFinal(block));
        }

        return new String(writer.toByteArray(), input_charset);
    }
	
	/**
	* 解密
	* @param content 密文
	* @param private_key 商户私钥
	* @return 解密后的字符串
	*/
	public static String decrypt(byte[] content, String private_key) throws Exception {
        PrivateKey prikey = getPrivateKey(private_key);

        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, prikey);
        return new String(cipher.doFinal(content));
    }

	
	/**
	* 得到私钥
	* @param key 密钥字符串（经过base64编码）
	* @throws Exception
	*/
	public static PrivateKey getPrivateKey(String key) throws Exception {

		byte[] keyBytes ;
		
		keyBytes = Base64.decode(key);
		
		PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
		
		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
		
		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
		
		return privateKey;
	}
}
