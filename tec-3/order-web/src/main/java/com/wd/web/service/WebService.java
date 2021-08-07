package com.wd.web.service;

import com.rabbitmq.client.Channel;
import com.wd.response.ResponseResult;
import org.springframework.amqp.core.Message;

public interface WebService {
	ResponseResult selectBySellerIdAndQueryKey (Long sellerId
			, Integer pageIndex, Integer pageSize
			, String queryKey);
	
	ResponseResult selectGoodsByGoodsId (Long goodsId);
	
	ResponseResult addIntoShoppingCart (Long goodsId, Integer buyNumber, Long buyerId);
	
	ResponseResult listAllGoodsInShoppingCart (Long buyerId, Integer pageIndex, Integer pageSize);
	
	ResponseResult commitOrderByShoppingCartId (Long shoppingCartId);

	void deleteById (Message message, Channel channel);
	
}
