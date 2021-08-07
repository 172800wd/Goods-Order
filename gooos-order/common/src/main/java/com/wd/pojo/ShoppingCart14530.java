package com.wd.pojo;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.*;

import java.util.Date;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

/**
 * <p>
 * 购物车
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
@TableName("tec_shopping_cart_14530")
public class ShoppingCart14530 implements Serializable {
	
	/**
	 * 主键
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	
	/**
	 * 买家ID/客户ID
	 */
	@JsonIgnore
	private Long buyId;
	
	/**
	 * 商品ID
	 */
	private Long goodsId;
	
	/**
	 * 商品名称
	 */
	private String goodsName;
	
	/**
	 * 商品价格
	 */
	private BigDecimal goodsPrice;
	
	/**
	 * 商品数量
	 */
	private Integer goodsNumber;
	
	/**
	 * 备注/留言
	 */
	private String description;
	
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
