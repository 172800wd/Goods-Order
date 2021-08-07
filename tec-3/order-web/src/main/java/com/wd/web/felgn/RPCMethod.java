package com.wd.web.felgn;

import com.wd.response.ResponseResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "order-admin-service")
@RequestMapping(value = "/api/goods")
public interface RPCMethod {
	@RequestMapping(value = "/list/v1", method = RequestMethod.GET)
	public ResponseResult listGoods (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize
			, @RequestParam("queryKey") String queryKey);
	
	@RequestMapping(value = "/selectById/v1", method = RequestMethod.GET)
	public ResponseResult selectById (@RequestParam("goodsId") Long goodsId
			, @RequestParam("sellerId") Long sellerId);
	
	@RequestMapping(value = "/selectLikeGoodsName/v1", method = RequestMethod.GET)
	public ResponseResult selectLikeGoodsName (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize
			, @RequestParam("queryKey") String queryKey);
	
	@RequestMapping(value = "/listAllGoods/v1", method = RequestMethod.GET)
	public ResponseResult listAllGoods (@RequestParam("sellerId") Long sellerId
			, @RequestParam("pageIndex") Integer pageIndex
			, @RequestParam("pageSize") Integer pageSize) ;
}
