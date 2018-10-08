package com.shenghesun.invite.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件管理工具
 * 	提供获取路径、储存文件模型、储存文件功能
 * @author kevin
 *
 */
public class FileIOUtil {
	
	/**
	 * 如果传入的字符串（文件前缀）小于一定的值，在其后面添加时间戳和一个长度的由随机数字组成的字符串
	 * 	解决的问题是：在某种环境下，文件名的前缀太短，会在服务器端出现储存错误（原因不明），所以加长文件名前缀。
	 * @param prefix
	 * @return
	 */
	public static String updatePrefixIfTooShort(String prefix){
		if(prefix != null && prefix.length() < 3){
			prefix += getRandomFileName();
		}
		return prefix;
	}
	
	/**
	 * 把传入文件保存为文件模板，并获取文件模板的绝对位置
	 * 	返回 map 中 key = path 是获取文件模板的绝对位置
	 * @param mf
	 * @return
	 * @throws IOException
	 */
	public static Map<String, String> saveTempFile(MultipartFile mf)
			throws IOException {
		String ext = getExtension(mf.getOriginalFilename());
		return saveTempFile(mf, getRandomFileName() + ext, ext);
	}
	
	/**
	 * 生成一个由 时间戳 和 一位由随机数字 组成的字符串
	 * @return
	 */
	public static String getRandomFileName() {
		return System.currentTimeMillis() + RandomUtil.randomNum(1);
	}

	/**
	 * 使用 java 原生的方法保存文件模板
	 * 
	 * @param mf
	 * @param fileName
	 * @param extension
	 * @return 真实的文件路径
	 * @throws IOException
	 */
	public static Map<String, String> saveTempFile(MultipartFile mf, String fileName,
			String extension) throws IOException {
		fileName = updatePrefixIfTooShort(fileName);
		File tempFile = File.createTempFile(fileName, extension);
		mf.transferTo(tempFile);
		Map<String, String> map = new HashMap<>();
		// map.put("fileName", fileName);
		map.put("path", tempFile.getAbsolutePath());
		// map.put("extension", extension);
		return map;
	}
	/**
	 * 通过时间戳，拆分出文件子目录
	 *	/yyyy/MMdd/HHmm/ssSSS/
	 * @return
	 */
	public static String generateSubPathStr() {
		
		StringBuilder b = new StringBuilder();
		
		Date d = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
		//b.append(File.separator);
		b.append(sdf.format(d));
		sdf = new SimpleDateFormat("MMdd");
		b.append(File.separator);
		b.append(sdf.format(d));
		sdf = new SimpleDateFormat("HHmm");
		b.append(File.separator);
		b.append(sdf.format(d));
//		sdf = new SimpleDateFormat("ssSSS");//毫秒值有点儿太小了，很容易导致每个文件夹下只有一个文件
		sdf = new SimpleDateFormat("ss");//改为秒值
		b.append(File.separator);
		b.append(sdf.format(d));
		b.append(File.separator);
		
		return b.toString();
	}
	/**
	 * 
	 * @param tempFile 模板文件名称
	 * @param newFileName 定制的文件名称
	 * @return 文件名和路径
	 */
	public static Map<String, String> saveFile(File tempFile, String newFileName, String pageSavePath) {
		String extension = getExtension(tempFile.getName());// .jpg
		String fileName = newFileName + extension;// 100.jpg

		String subPath = generateSubPathStr();// 根据时间计算出的文件目录 /2016/04/086/
		File folder = new File(pageSavePath + subPath);// http://c4.py0.cc/2016/04/086/
		if (!folder.exists()) {
			try {
				org.apache.commons.io.FileUtils.forceMkdir(folder);
			} catch (IOException e) {
				throw new RuntimeException("创建目录时出错", e);
			}
		}
		subPath = subPath + fileName;// 返回值时使用
		File newFile = new File(pageSavePath + subPath);// http://c4.py0.cc/2016/04/086/100.jpg
		try {
			FileUtils.copyFile(tempFile, newFile);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Map<String, String> map = new HashMap<String, String>();
		map.put("fileName", fileName);
		map.put("path", StringUtils.replace(subPath, File.separator, "/"));
		// map.put("extension", extension);
		return map;
	}

	/**
	 * 取到以 .开头的文件扩展名（小写）
	 * 
	 * @param fileName 文件名
	 * @return 扩展名
	 */
	public static String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
	}
	
	/**
	 * 检验上传文件大小
	 *  file.length()返回的是bit；/1024得到 k 
	 * @param file
	 * @param length
	 * @return
	 */
	public static String validateSize(File file, int length) {
		return file.length() / 1024 > length ? "size is bigger than " + length + " k" : "true";
	}
    
//	/**
//	 * 保存上传文件
//	 * @param filename 上传文件名称
//	 * @param is 上传文件流
//	 * @param fileSavePath 文件保存路径
//	 * @param changeFileNameFlag 是否修改原文件名称标志
//	 * @return
//	 */
//	public static String uploadFile(String filename, InputStream is, String fileSavePath,boolean changeFileNameFlag) {
//		int pos = filename.lastIndexOf('.');
//		if (pos == -1) {
//			throw new RuntimeException("文件名格式错误，不能读取扩展名");
//		}
//		
//		String subPath = generateSubPathStr();
//		String path = fileSavePath + subPath;
//		String name = filename;
//		if(changeFileNameFlag) {
//			String ext = filename.substring(pos + 1);
//			name = RandomUtil.randomString(4) + "." + ext;
//		}
//		File folder = new File(path);
//		try {
//			if (!folder.exists()) {
//				FileUtils.forceMkdir(folder);
//			}
//			FileUtils.copyInputStreamToFile(is, new File(folder, name));
//			return StringUtils.replaceChars(subPath, File.separatorChar, '/') + name;
//		} catch (IOException e) {
//			throw new RuntimeException("保存上传文件时出现错误", e);
//		}
//	}
	
	public static void main(String[] args) {
		
	}
	
}
