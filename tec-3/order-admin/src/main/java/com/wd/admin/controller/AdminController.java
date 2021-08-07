package com.wd.admin.controller;

import com.wd.admin.service.AdminService;
import com.wd.pojo.Goods14530;
import com.wd.response.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/goods")
public class AdminController {
	@Autowired
	private AdminService adminService;
	
	@RequestMapping(value = "/add/v1", method = RequestMethod.GET)
	public ResponseResult insertGood (@RequestBody Goods14530 goods14530) {
		return adminService.insertGood(goods14530);
	}
	
	@RequestMapping(value = "/list/v1", method = RequestMethod.GET)
	public ResponseResult listGoods (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize
			, @RequestParam("queryKey") String queryKey) {
		return adminService.selectBySellerIdAndQueryKey(sellerId, pageIndex, pageSize, queryKey);
	}
	
	@RequestMapping(value = "/deleteById/v1", method = RequestMethod.GET)
	public ResponseResult deleteByGoodsId (@RequestParam("goodsId") Long goodsId) {
		return adminService.deleteByGoodsId(goodsId);
	}
	
	@RequestMapping(value = "/selectById/v1", method = RequestMethod.GET)
	public ResponseResult selectById (@RequestParam("goodsId") Long goodsId
			, @RequestParam("sellerId") Long sellerId) {
		return adminService.selectById(goodsId, sellerId);
	}
	
	@RequestMapping(value = "/selectLikeGoodsName/v1", method = RequestMethod.GET)
	public ResponseResult selectLikeGoodsName (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize
			, @RequestParam("queryKey") String queryKey) {
		return adminService.selectLikeGoodsName(sellerId, pageIndex, pageSize, queryKey);
	}
	
	@RequestMapping(value = "/listAllGoods/v1", method = RequestMethod.GET)
	public ResponseResult listAllGoods (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize) {
		return adminService.listAllGoods(sellerId, pageIndex, pageSize);
	}
}
