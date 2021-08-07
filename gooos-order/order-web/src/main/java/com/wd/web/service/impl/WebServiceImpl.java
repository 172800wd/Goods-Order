package com.wd.web.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rabbitmq.client.Channel;
import com.wd.pojo.Goods14530;
import com.wd.pojo.Order14530;
import com.wd.pojo.ShoppingCart14530;
import com.wd.response.ResponseResult;
import com.wd.web.dao.WebGoodsDao;
import com.wd.web.dao.WebOrderDao;
import com.wd.web.dao.WebShoppingCartDao;
import com.wd.web.felgn.RPCMethod;
import com.wd.web.service.WebService;
import feign.Feign;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class WebServiceImpl implements WebService {
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RPCMethod rpcMethod;
	@Autowired
	private WebGoodsDao webGoodsDao;
	@Autowired
	private WebShoppingCartDao webShoppingCartDao;
	@Autowired
	private WebOrderDao webOrderDao;
	
	@Override
	public ResponseResult selectBySellerIdAndQueryKey (Long sellerId, Integer pageIndex, Integer pageSize, String queryKey) {
		if (queryKey == null || "".equals(queryKey)) {
			List<Object> goods = redisTemplate.opsForList().range("admin:" + "tec_14530_list_" + sellerId, 0, -1);
			if (goods == null || goods.size() == 0) {
				return rpcMethod.listAllGoods(sellerId, pageIndex, pageSize);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("pageIndex", pageIndex);
			map.put("records", goods);
			map.put("size", pageSize);
			return new ResponseResult<Map>(0, "执行成功", map);
		}
		if (queryKey.matches("[0-9]+")) {
			Object good = redisTemplate.opsForValue().get("admin:" + "tec_14530_" + queryKey);
			if (good == null) {
				return rpcMethod.listGoods(sellerId, pageIndex, pageSize, queryKey);
			}
			Map<String, Object> map = new HashMap<>();
			map.put("pageIndex", 1);
			map.put("records", good);
			map.put("size", 1);
			return new ResponseResult<Map>(0, "执行成功", map);
		}
		List<Object> goods = redisTemplate.opsForList().range("admin:" + "tec_14530_like_" + sellerId, 0, -1);
		if (goods == null || goods.size() == 0) {
			return rpcMethod.listGoods(sellerId, pageIndex, pageSize, queryKey);
		}
		Map<String, Object> map = new HashMap<>();
		map.put("pageIndex", pageIndex);
		map.put("records", goods);
		map.put("size", pageSize);
		return new ResponseResult<Map>(0, "执行成功", map);
	}
	
	@Override
	public ResponseResult selectGoodsByGoodsId (Long goodsId) {
		Object good = redisTemplate.opsForValue().get("admin:" + "tec_14530_" + goodsId);
		if (good == null) {
			Goods14530 goods14530 = webGoodsDao.selectById(goodsId);
			if (goods14530 == null) {
				return new ResponseResult(5000, "未找到商品，请检查商品ID是否正确。");
			}
			redisTemplate.opsForValue().set("admin:" + "tec_14530_" + goodsId, goods14530);
			redisTemplate.expire("admin:" + "tec_14530_" + goodsId, 10, TimeUnit.MINUTES);
			return new ResponseResult<>(0, "执行成功", goods14530);
		}
		return new ResponseResult<>(0, "执行成功", good);
	}
	
	@Override
	@Transactional
	public ResponseResult addIntoShoppingCart (Long goodsId, Integer buyNumber, Long buyerId) {
		QueryWrapper<ShoppingCart14530> wrapper = new QueryWrapper<>();
		wrapper.eq("buy_id", buyerId).eq("goods_id", goodsId);
		ShoppingCart14530 shoppingCart14530 = webShoppingCartDao.selectOne(wrapper);
		int count = 0;
		if (shoppingCart14530 == null) {
			ShoppingCart14530 shoppingCart = new ShoppingCart14530();
			Goods14530 goods14530 = webGoodsDao.selectById(goodsId);
			if (goods14530 == null) {
				return new ResponseResult(5000, "商品ID错误，加入购物车失败。");
			}
			shoppingCart.setBuyId(buyerId);
			shoppingCart.setGoodsId(goodsId);
			shoppingCart.setGoodsName(goods14530.getGoodsName());
			shoppingCart.setGoodsNumber(buyNumber);
			shoppingCart.setGoodsPrice(goods14530.getGoodsPrice());
			shoppingCart.setDescription("买家" + buyerId
					+ "在" + new Date() + "购买了" + buyNumber
					+ "个" + goods14530.getGoodsName());
			count = webShoppingCartDao.insert(shoppingCart);
		} else {
			UpdateWrapper<ShoppingCart14530> updateWrapper = new UpdateWrapper<>();
			updateWrapper.set("goods_number", shoppingCart14530.getGoodsNumber() + buyNumber)
					.eq("buy_id", buyerId).eq("goods_id", goodsId);
			count = webShoppingCartDao.update(new ShoppingCart14530(), updateWrapper);
		}
		if (count != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return new ResponseResult<>(5000
					, "加入购物车失败，请检查购物车信息是否正确。");
		}
		return new ResponseResult<>(0, "执行成功");
	}
	
	@Override
	public ResponseResult listAllGoodsInShoppingCart (Long buyerId, Integer pageIndex, Integer pageSize) {
		IPage<ShoppingCart14530> iPage = new Page<>(pageIndex, pageSize);
		QueryWrapper<ShoppingCart14530> wrapper = new QueryWrapper<>();
		wrapper.eq("buy_id", buyerId);
		IPage<ShoppingCart14530> results = webShoppingCartDao.selectPage(iPage, wrapper);
		List<ShoppingCart14530> records = results.getRecords();
		if (records == null || records.size() == 0) {
			return new ResponseResult<>(5000
					, "购物车中没有任何商品，请确认买家ID是否正确。");
		}
		Map<String, Object> map = new HashMap<>();
		map.put("pageIndex", pageIndex);
		map.put("records", records);
		map.put("size", pageSize);
		map.put("pageTotal", results.getPages());
		return new ResponseResult<Map>(0, "执行成功", map);
	}
	
	@Override
	@Transactional
	public ResponseResult commitOrderByShoppingCartId (Long shoppingCartId) {
		ShoppingCart14530 shoppingCart = webShoppingCartDao.selectById(shoppingCartId);
		if (shoppingCart == null) {
			return new ResponseResult<>(5000
					, "购物车中没有商品，请确认购物车ID是否正确。");
		}
		Goods14530 goods14530 = webGoodsDao.selectById(shoppingCart.getId());
		Order14530 order14530 = new Order14530();
		order14530.setBuyId(shoppingCart.getBuyId());
		order14530.setSellerId(goods14530.getSellerId());
		order14530.setGoodsId(shoppingCart.getGoodsId());
		order14530.setGoodsName(shoppingCart.getGoodsName());
		order14530.setGoodsPrice(shoppingCart.getGoodsPrice());
		order14530.setGoodsNumber(shoppingCart.getGoodsNumber());
		order14530.setDescription("买家" + shoppingCart.getBuyId() + new Date() + "提交了购物车" + shoppingCartId);
		int insertCount = webOrderDao.insert(order14530);
		int deleteCount = webShoppingCartDao.deleteById(shoppingCartId);
		if (insertCount != 1 || deleteCount != 1) {
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return new ResponseResult<>(5000
					, "提交订单失败，请检查购物车信息是否正确。");
		}
		return new ResponseResult(0, "执行成功");
	}
	
	@Override
	@RabbitListener(queues = "14530_normal_queue_a")
	public void deleteById (Message message, Channel channel) {
		Boolean delete = redisTemplate.delete(new String(message.getBody()));
	}
}
