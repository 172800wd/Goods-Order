package com.wd.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ResponseResult <T> {
	private Integer code;
	//	private Map<String, Object> data = new HashMap<>();
	private String msg;
	private T data;
	
	public ResponseResult(Integer code, String message) {
		this(code, message, null);
	}
	
	// 成功
//	public static ResponseResult success (String msg) {
//		ResponseResult result = new ResponseResult();
//		result.setCode(0);
//		result.setMsg(msg);
//		return result;
//	}
//
//	// 失败
//	public static ResponseResult error (String msg) {
//		ResponseResult result = new ResponseResult();
//		result.setCode(5000);
//		result.setMsg(msg);
//		return result;
//	}
//
//
//	public ResponseResult msg (String msg) {
//		this.setMsg(msg);
//		return this;
//	}
//
//	public ResponseResult code (Integer code) {
//		this.setCode(code);
//		return this;
//	}
//
//	public ResponseResult data (T data) {
//		this.data=data;
//		return this;
//	}
}
