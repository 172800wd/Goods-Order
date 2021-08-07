package com.wd.pojo;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * <p>
 * 商品表
 * </p>
 *
 * @author testjava
 * @since 2021-08-04
 */
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName("tec_goods_14530")
public class Goods14530 implements Serializable {
	
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	
	/**
	 * 卖家ID/商家ID
	 */
//	@JsonIgnore
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	private Long sellerId;
	
	/**
	 * 商品名称
	 */
	private String goodsName;
	
	/**
	 * 价格
	 */
	private BigDecimal goodsPrice;
	
	/**
	 * 剩余库存数量
	 */
	@TableField(value = "goods_stock_number")
	private Integer stockNumber;
	
	/**
	 * 商品备注
	 */
//	@JsonIgnore
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@TableField(value = "goods_description")
	private String description;
	
	/**
	 * 删除状态：1：已删除；0：未删除
	 */
	@TableLogic
	@JsonIgnore
	private Integer deleted;
	
	/**
	 * 创建时间
	 */
	@JsonIgnore
	@TableField(fill = FieldFill.INSERT)
	private Date createTime;
	
	/**
	 * 更新时间
	 */
	@JsonIgnore
	@TableField(fill = FieldFill.INSERT_UPDATE)
	private Date updateTime;
	
}
