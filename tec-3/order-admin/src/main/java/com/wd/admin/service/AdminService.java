package com.wd.admin.service;

import com.wd.pojo.Goods14530;
import com.wd.response.ResponseResult;

public interface AdminService {
	ResponseResult insertGood (Goods14530 goods14530);
	
	ResponseResult selectBySellerIdAndQueryKey (Long sellerId
			, Integer pageIndex, Integer pageSize
			, String queryKey);
	
	ResponseResult selectById (Long goodsId, Long sellerId);
	
	ResponseResult selectLikeGoodsName (Long sellerId
			, Integer pageIndex, Integer pageSize
			, String queryKey);
	
	ResponseResult listAllGoods (Long sellerId, Integer pageIndex, Integer pageSize);
	
	ResponseResult deleteByGoodsId (Long goodsId);
	
	
}
