package com.recruitment.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
@ConditionalOnProperty(name = "app.mail.queue-enabled", havingValue = "true")
public class RabbitMqConfig {

	public static final String EMAIL_EXCHANGE = "email.exchange";
	public static final String EMAIL_QUEUE = "email.send";
	public static final String EMAIL_ROUTING_KEY = "email.send";
	public static final String EMAIL_DLX = "email.dlx";
	public static final String EMAIL_DLQ = "email.send.dlq";

	@Value("${spring.rabbitmq.host:localhost}")
	private String host;

	@Value("${spring.rabbitmq.port:5672}")
	private int port;

	@Value("${spring.rabbitmq.username:guest}")
	private String username;

	@Value("${spring.rabbitmq.password:guest}")
	private String password;

	@Bean
	ConnectionFactory rabbitConnectionFactory() {
		CachingConnectionFactory factory = new CachingConnectionFactory(host, port);
		factory.setUsername(username);
		factory.setPassword(password);
		return factory;
	}

	@Bean
	RabbitAdmin rabbitAdmin(ConnectionFactory rabbitConnectionFactory) {
		return new RabbitAdmin(rabbitConnectionFactory);
	}

	@Bean
	MessageConverter jacksonMessageConverter() {
		return new JacksonJsonMessageConverter();
	}

	@Bean
	RabbitTemplate rabbitTemplate(ConnectionFactory rabbitConnectionFactory, MessageConverter jacksonMessageConverter) {
		RabbitTemplate template = new RabbitTemplate(rabbitConnectionFactory);
		template.setMessageConverter(jacksonMessageConverter);
		return template;
	}

	@Bean
	SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
			ConnectionFactory rabbitConnectionFactory,
			MessageConverter jacksonMessageConverter) {
		SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
		factory.setConnectionFactory(rabbitConnectionFactory);
		factory.setMessageConverter(jacksonMessageConverter);
		factory.setDefaultRequeueRejected(false);
		factory.setAdviceChain(RetryInterceptorBuilder.stateless()
				.maxRetries(3)
				.backOffOptions(1_000, 2.0, 10_000)
				.recoverer(new RejectAndDontRequeueRecoverer())
				.build());
		return factory;
	}

	@Bean
	DirectExchange emailExchange() {
		return new DirectExchange(EMAIL_EXCHANGE, true, false);
	}

	@Bean
	Queue emailQueue() {
		return QueueBuilder.durable(EMAIL_QUEUE)
				.deadLetterExchange(EMAIL_DLX)
				.deadLetterRoutingKey(EMAIL_DLQ)
				.build();
	}

	@Bean
	Binding emailBinding(
			@Qualifier("emailQueue")
			Queue emailQueue,
			@Qualifier("emailExchange")
			DirectExchange emailExchange) {
		return BindingBuilder.bind(emailQueue).to(emailExchange).with(EMAIL_ROUTING_KEY);
	}

	@Bean
	DirectExchange emailDeadLetterExchange() {
		return new DirectExchange(EMAIL_DLX, true, false);
	}

	@Bean
	Queue emailDeadLetterQueue() {
		return QueueBuilder.durable(EMAIL_DLQ).build();
	}

	@Bean
	Binding emailDeadLetterBinding(
			@Qualifier("emailDeadLetterQueue")
			Queue emailDeadLetterQueue,
			@Qualifier("emailDeadLetterExchange")
			DirectExchange emailDeadLetterExchange) {
		return BindingBuilder.bind(emailDeadLetterQueue).to(emailDeadLetterExchange).with(EMAIL_DLQ);
	}
}
