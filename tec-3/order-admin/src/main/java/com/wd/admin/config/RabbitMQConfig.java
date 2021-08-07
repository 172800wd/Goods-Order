package com.wd.admin.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
	public static final String NORMAL_EXCHANGE = "goods_update_notify";
	public static final String NORMAL_QUEUE_A = "14530_normal_queue_a";
	
	@Bean("normalExchange")
	public DirectExchange normalExchange () {
		return new DirectExchange(NORMAL_EXCHANGE);
	}
	
	@Bean("normalQueueA")
	public Queue normalQueueA () {
		return QueueBuilder.nonDurable(NORMAL_QUEUE_A).build();
	}
	
	@Bean
	public Binding normalQueueABindingExchange (
			@Qualifier("normalQueueA") Queue queueA
			, @Qualifier("normalExchange") DirectExchange normalExchange) {
		
		return BindingBuilder.bind(queueA).to(normalExchange).with("14530");
	}
}