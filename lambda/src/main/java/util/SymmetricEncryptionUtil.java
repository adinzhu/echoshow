package util;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class SymmetricEncryptionUtil {
	public static String encrypt(String keyString,String content){
		String AES_encode = "";
		try{
			
			byte[] raw = Bytes2HexStringUtil.hexStringToByte(keyString);
			SecretKey key=new SecretKeySpec(raw, "AES");
			//6.根据指定算法AES自成密码器
			Cipher cipher=Cipher.getInstance("AES");
			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.ENCRYPT_MODE, key);
			//8.获取加密内容的字节数组(这里要设置为utf-8)不然内容中如果有中文和英文混合中文就会解密为乱码
			byte [] byte_encode=content.getBytes("utf-8");
			//9.根据密码器的初始化方式--加密：将数据加密
			byte [] byte_AES=cipher.doFinal(byte_encode);
			//10.将加密后的数据转换为字符串
			//这里用Base64Encoder中会找不到包
			//解决办法：
			//在项目的Build path中先移除JRE System Library，再添加库JRE System Library，重新编译后就一切正常了。
			AES_encode=new String(new BASE64Encoder().encode(byte_AES));
		}catch(Exception e){
			return AES_encode;
		}
		return AES_encode;
		
	}
	
	public static String decrypt(String keyString,String content){
		String AES_decode = "";
		try{
			
			SecretKey decriptKey=new SecretKeySpec(Bytes2HexStringUtil.hexStringToByte(keyString), "AES");
			//6.根据指定算法AES自成密码器
			Cipher cipher=Cipher.getInstance("AES");
			//7.初始化密码器，第一个参数为加密(Encrypt_mode)或者解密(Decrypt_mode)操作，第二个参数为使用的KEY
			cipher.init(Cipher.DECRYPT_MODE, decriptKey);
			//8.将加密并编码后的内容解码成字节数组
			byte [] byte_content= new BASE64Decoder().decodeBuffer(content);
			/*
			 * 解密
			 */
			byte [] byte_decode=cipher.doFinal(byte_content);
			AES_decode=new String(byte_decode,"utf-8");
		}catch(Exception e){
			return AES_decode;
		}
		return AES_decode;
		
	}
	
	public static String generateKey(String ecnodeRules){
		//1.构造密钥生成器，指定为AES算法,不区分大小写
		KeyGenerator keygen;
		String keyString = "";
		try {
			keygen = KeyGenerator.getInstance("AES");
			//2.根据ecnodeRules规则初始化密钥生成器
			//生成一个128位的随机源,根据传入的字节数组
			keygen.init(128, new SecureRandom("使用AES对称加密".getBytes()));
			//3.产生原始对称密钥
			SecretKey original_key=keygen.generateKey();
			//4.获得原始对称密钥的字节数组
			byte [] raw=original_key.getEncoded();
			keyString = Bytes2HexStringUtil.bytesToHexString(raw);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			return keyString;
		}
		return keyString;
		
	}
}
