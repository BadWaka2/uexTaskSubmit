package org.zywx.wbpalmstar.plugin.uexTaskSubmit;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import android.util.Base64;

import java.security.*;

/**
 * DES加密解密算法
 * 
 * @author waka
 *
 */
public class EncryptionDES {
	private KeyGenerator keyGenerator;// 秘钥生成器
	private SecretKey secretKey;// 保存秘钥
	private Cipher cipher;// 完成加密或解密工作,cipher:密码
	private byte[] cipherByte;// 该字节数组用来保存加密后的结果

	/**
	 * 构造方法
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 */
	public EncryptionDES() {
		try {
			// 实例化支持DES算法的密钥生成器(算法名称命名需按规定，否则抛出异常)
			keyGenerator = KeyGenerator.getInstance("DES");
			// 生成密钥
			secretKey = keyGenerator.generateKey();
			// 生成Cipher对象,指定其支持的DES算法
			cipher = Cipher.getInstance("DES");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 对字符串进行加密
	 * 
	 * @param string
	 * @return
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 */
	public String Encryptor(String string) {
		// 根据密钥，对Cipher对象进行初始化，ENCRYPT_MODE表示加密模式
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] byteArray = string.getBytes();
			// 加密，结果保存进cipherByte
			cipherByte = cipher.doFinal(byteArray);
			String strBase64 = Base64.encodeToString(cipherByte, Base64.DEFAULT);// 将byte数组文件转换为Base64加密文件，因为SharedPreferences不能存byte数组，只能存string,而中途直接转string的话解析时会报IllegalBlockSizeException异常，所以必须转为base64
			return strBase64;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 对字符串解密
	 * 
	 * @param string
	 * @return
	 * @throws InvalidKeyException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	public String Decryptor(String strBase64) {
		// 根据密钥，对Cipher对象进行初始化，DECRYPT_MODE表示解密模式
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			byte[] byteArray = Base64.decode(strBase64, Base64.DEFAULT);// 将base64字符串转换为byte数组
			cipherByte = cipher.doFinal(byteArray);
			String stringDecryptor = new String(cipherByte);
			return stringDecryptor;
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}
		return null;
	}

}
