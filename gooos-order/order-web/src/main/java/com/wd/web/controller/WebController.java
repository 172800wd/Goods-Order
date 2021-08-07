package com.wd.web.controller;

import com.wd.response.ResponseResult;
import com.wd.web.service.WebService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
//@RequestMapping("/api/goods/web")
public class WebController {
	@Autowired
	private WebService webService;
	
	@RequestMapping(value = "/api/goods/web/list/v1", method = RequestMethod.GET)
	public ResponseResult selectBySellerIdAndQueryKey (
			@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize
			, @RequestParam("queryKey") String queryKey) {
		return webService.selectBySellerIdAndQueryKey(sellerId, pageIndex, pageSize, queryKey);
	}
	
	@RequestMapping(value = "/api/goods/getById/v1", method = RequestMethod.GET)
	public ResponseResult selectGoodsByGoodsId (@RequestParam("id") Long goodsId) {
		return webService.selectGoodsByGoodsId(goodsId);
	}
	
	@RequestMapping(value = "/api/shopping/cart/add/v1", method = RequestMethod.GET)
	public ResponseResult addIntoShoppingCart (@RequestParam("goodsId") Long goodsId
			, @RequestParam("buyNumber") Integer butNumber
			, @RequestParam("buyerId") Long buyerId) {
		return webService.addIntoShoppingCart(goodsId, butNumber, buyerId);
	}
	
	@RequestMapping(value = "/api/shopping/cart/list/v1", method = RequestMethod.GET)
	public ResponseResult listAllGoodsInShoppingCart (@RequestParam("buyerId") Long buyerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize) {
		return webService.listAllGoodsInShoppingCart(buyerId, pageIndex, pageSize);
	}
	
	@RequestMapping(value = "/api/order/add/v1", method = RequestMethod.GET)
	public ResponseResult commitOrderByShoppingCartId (@RequestParam("id") Long shoppingCartId) {
		return webService.commitOrderByShoppingCartId(shoppingCartId);
	}
	
}
