package com.shenghesun.invite.wx.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.shenghesun.invite.cache.RedisResultCache;
import com.shenghesun.invite.config.wx.WxConfig;
import com.shenghesun.invite.model.PlayAge;
import com.shenghesun.invite.model.PlayFrequency;
import com.shenghesun.invite.model.SkillLevel;
import com.shenghesun.invite.utils.JsonUtils;
import com.shenghesun.invite.utils.RandomUtil;
import com.shenghesun.invite.wx.entity.WxUserSelfEvaluation;
import com.shenghesun.invite.wx.service.WxUserInfoService;
import com.shenghesun.invite.wx.service.WxUserSelfEvaluationService;

@RestController
@RequestMapping(value = "/api/wx_user_evaluation")
public class WxUserSelfEvaluationController {
	
	@Autowired
	private WxUserSelfEvaluationService wxUserSelfEvaluationService;
	@Autowired
	private RedisResultCache redisResultCache;
	@Autowired
	private WxUserInfoService wxUserInfoService;
	@Value("${upload.file.path}")
	private String fileSavePath;
	
	public String getFileSavePath() {
		return fileSavePath;
	}
	
	@Value("${show.file.path}")
	private String fileShowPath;
	
	public String getFileShowPath() {
		return fileShowPath;
	}
	
	/**
	 * 设置允许自动绑定的属性名称
	 * 
	 * @param binder
	 * @param req
	 */
	@InitBinder("entity")
	private void initBinder(ServletRequestDataBinder binder,
			HttpServletRequest req) {
		List<String> fields = new ArrayList<String>(Arrays.asList("playFrequency", "playAge", "skillLevel","certificatePath" ,"remark", "wxUserInfoId"));
		switch (req.getMethod().toLowerCase()) {
		case "post": // 新增
			binder.setAllowedFields(fields.toArray(new String[fields.size()]));
			break;
		default:
			break;
		}
	}

	/**
	 * 预处理，一般用于新增和修改表单提交后的预处理
	 * 
	 * @param id
	 * @param req
	 * @return
	 */
	@ModelAttribute("entity")
	public WxUserSelfEvaluation prepare(
			@RequestParam(value = "id", required = false) Long id,
			HttpServletRequest req) {
		String method = req.getMethod().toLowerCase();
		if (id != null && id > 0 && "post".equals(method)) {// 修改表单提交后数据绑定之前执行
			return wxUserSelfEvaluationService.findById(id);
		} else if ("post".equals(method)) {// 新增表单提交后数据绑定之前执行
			return new WxUserSelfEvaluation();
		} else {
			return null;
		}
	}

	
	/**
	 * 获取当前用户的自我评价信息
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/detail", method = RequestMethod.GET)
	public JSONObject detail(HttpServletRequest request) {
		// 获取当前用户信息
		String result = redisResultCache.getResultByToken(request.getHeader(WxConfig.CUSTOM_TOKEN_NAME));
		Long authWxUserInfoId = Long.parseLong(result == null ? "1" : result);// TODO
		WxUserSelfEvaluation wxUserSelfEvaluation = wxUserSelfEvaluationService.findByWxUserInfoId(authWxUserInfoId);
		if(wxUserSelfEvaluation == null) {
			wxUserSelfEvaluation = new WxUserSelfEvaluation();
//			wxUserSelfEvaluation.setWxUserInfo(wxUserInfo);
			wxUserSelfEvaluation.setWxUserInfoId(authWxUserInfoId);
		}
		JSONObject json = new JSONObject();
		json.put("selfEvaluation", wxUserSelfEvaluation);
		json.put("playFrequencyMap", PlayFrequency.getAllPlayFrequencyMap());
		json.put("playAgeMap", PlayAge.getAllPlayAgeMap());
		json.put("skillLevelMap", SkillLevel.getAllSkillLevelMap());
		return JsonUtils.getSuccessJSONObject(json);
	}
	@RequestMapping(value = "/update", method = RequestMethod.POST)
	public JSONObject update(@RequestParam(value = "id", required = false) Long id, 
			@Validated @ModelAttribute("entity") WxUserSelfEvaluation selfEvaluation, BindingResult result) {
		if(result.hasErrors()) {
			return JsonUtils.getFailJSONObject("输入内容有误");
		}
		if(id != null) {
			selfEvaluation.setId(id);
		}
		selfEvaluation.setWxUserInfo(wxUserInfoService.findById(selfEvaluation.getWxUserInfoId()));
		selfEvaluation = wxUserSelfEvaluationService.save(selfEvaluation);//TODO 需要测试该 selfEvaluation 是否真的能包含页面输入的内容
		return JsonUtils.getSuccessJSONObject();
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Object info(@RequestParam(value="userId")Long userId){
		JSONObject json =new JSONObject();
		WxUserSelfEvaluation info = wxUserSelfEvaluationService.findByWxUserInfoId(userId);
		json.put("info", info);
		return JsonUtils.getSuccessJSONObject(json);
				
	}
	
	@RequestMapping(value="/upload",method = RequestMethod.POST)
	public JSONObject upload(@RequestParam("file")MultipartFile imgFile,HttpServletRequest request){
		JSONObject json = new JSONObject();
		String path = "";
		try {
			path = this.uploadFile(imgFile.getOriginalFilename(), imgFile.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		json.put("filepath", path);
		return JsonUtils.getSuccessJSONObject(json);
	}
	
	public String uploadFile(String filename,InputStream is){
		int pos = filename.lastIndexOf('.');
		if (pos == -1) {
			throw new RuntimeException("文件名格式错误，不能读取扩展名");
		}
		if (!filename.endsWith(".png") && !filename.endsWith(".jpg") && !filename.endsWith(".gif") && !filename.endsWith(".bmp")) {
			throw new RuntimeException("文件格式不正确");
		}
		String ext = filename.substring(pos + 1);
		String subPath = generateSubPathStr();
		String path = fileSavePath + subPath;
		String name = System.currentTimeMillis() + RandomUtil.randomString(2) + "." + ext;
		File folder = new File(path);
		try {
			if (!folder.exists()) {
				FileUtils.forceMkdir(folder);
			}
			FileUtils.copyInputStreamToFile(is, new File(folder, name));
			return StringUtils.replaceChars(subPath, File.separatorChar, '/') + name;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String generateSubPathStr(Date date) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date.getTime());
		StringBuilder sb = new StringBuilder(14);
		sb.append(c.get(1));
		sb.append(File.separatorChar);
		sb.append((new DecimalFormat("000")).format((long) c.get(6)));
		sb.append(File.separatorChar);
		sb.append((new DecimalFormat("0000")).format((long) (60 * c.get(11) + c.get(12))));
		sb.append(File.separatorChar);
		return sb.toString();
	}

	public static String generateSubPathStr() {
		return generateSubPathStr(new Date());
	}

}
