package com.wd.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wd.admin.dao.AdminDao;
import com.wd.admin.service.AdminService;
import com.wd.pojo.Goods14530;
import com.wd.response.ResponseResult;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


@Service
public class OrderServiceImpl implements AdminService {
	
	@Autowired
	private AdminDao adminDao;
	@Autowired
	private RedisTemplate<String, Object> redisTemplate;
	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@Override
	public ResponseResult<Goods14530> insertGood (Goods14530 goods14530) {
		if (goods14530 == null) {
//			return ResponseResult.error("新增商品信息错误，请仔细检查后重试。");
			return new ResponseResult<>(5000, "新增商品信息错误，请仔细检查后重试。", null);
		}
		int count = adminDao.insert(goods14530);
		if (count != 1) {
//			return ResponseResult.error("插入失败，请仔细检查后重试。");
			return new ResponseResult<>(5000, "插入失败，请仔细检查后重试。", null);
		}
		return new ResponseResult<>(0, "Success", null);
	}
	
	@Override
	public ResponseResult selectBySellerIdAndQueryKey (Long sellerId
			, Integer pageIndex, Integer pageSize
			, String queryKey) {
		if (queryKey == null || "".equals(queryKey)) {
			return listAllGoods(sellerId, pageIndex, pageSize);
		}
		if (queryKey.matches("[0-9]+")) {
			return selectById(Long.valueOf(queryKey), sellerId);
		}
		return selectLikeGoodsName(sellerId, pageIndex, pageSize, queryKey);
	}
	
	@Override
	public ResponseResult selectById (Long goodsId, Long sellerId) {
		QueryWrapper<Goods14530> wrapper = new QueryWrapper<>();
		wrapper.eq("id", goodsId);
		wrapper.eq("seller_id", sellerId);
		Goods14530 goods14530 = adminDao.selectOne(wrapper);
		if (goods14530 == null) {
			return new ResponseResult<>(5000
					, "未找到相关商品，请检查查询条件是否正确。"
					, goods14530);
		}
		redisTemplate.opsForValue().set("admin:" + "tec_14530_" + goodsId, goods14530, 10, TimeUnit.MINUTES);
		Map<String, Object> map = new HashMap<>();
		map.put("pageIndex", 1);
		map.put("records", goods14530);
		map.put("size", 1);
		map.put("pageTotal", 1);
		return new ResponseResult<Map>(0, "执行成功", map);
	}
	
	@Override
	public ResponseResult selectLikeGoodsName (Long sellerId
			, Integer pageIndex, Integer pageSize
			, String queryKey) {
		IPage<Goods14530> iPage = new Page<>(pageIndex, pageSize);
		QueryWrapper<Goods14530> wrapper = new QueryWrapper<>();
		wrapper.eq("seller_id", sellerId);
		wrapper.like("goods_name", queryKey);
		IPage<Goods14530> results = adminDao.selectPage(iPage, wrapper);
		List<Goods14530> goods = results.getRecords();
		if (goods == null || goods.size() == 0) {
			return new ResponseResult<List>(5000
					, "未找到相关商品，请检查查询条件是否正确。"
					, goods);
		}
		redisTemplate.opsForList().leftPushAll("admin:" + "tec_14530_like_" + sellerId, goods);
		redisTemplate.expire("admin:" + "tec_14530_like_" + sellerId, 10, TimeUnit.MINUTES);
		Map<String, Object> map = new HashMap<>();
		map.put("pageIndex", pageIndex);
		map.put("records", goods);
		map.put("size", pageSize);
		map.put("pageTotal", results.getPages());
		return new ResponseResult<Map>(0, "执行成功", map);
	}
	
	@Override
	public ResponseResult listAllGoods (Long sellerId, Integer pageIndex, Integer pageSize) {
		IPage<Goods14530> iPage = new Page<>(pageIndex, pageSize);
		QueryWrapper<Goods14530> wrapper = new QueryWrapper<>();
		wrapper.eq("seller_id", sellerId);
		IPage<Goods14530> results = adminDao.selectPage(iPage, wrapper);
		List<Goods14530> goods = results.getRecords();
		if (goods == null || goods.size() == 0) {
			return new ResponseResult<List>(5000
					, "未找到相关商品，请检查查询条件是否正确。"
					, goods);
		}
		redisTemplate.opsForList().leftPushAll("admin:" + "tec_14530_list_" + sellerId, goods);
		redisTemplate.expire("admin:" + "tec_14530_list_" + sellerId, 10, TimeUnit.MINUTES);
		Map<String, Object> map = new HashMap<>();
		map.put("pageIndex", pageIndex);
		map.put("records", goods);
		map.put("size", pageSize);
		map.put("pageTotal", results.getPages());
		return new ResponseResult<Map>(0, "执行成功", map);
	}
	
	@Override
	public ResponseResult deleteByGoodsId (Long goodsId) {
		int i = adminDao.deleteById(goodsId);
		if (i != 1) {
			return new ResponseResult(5000, "删除失败，请检查商品ID是否正确.");
		}
		rabbitTemplate.convertAndSend("goods_update_notify"
				, "14530", "admin:" + "tec_14530_" + goodsId);
		return new ResponseResult(0, "Success");
	}
}
