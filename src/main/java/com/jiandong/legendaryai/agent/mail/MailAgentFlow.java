package com.jiandong.legendaryai.agent.mail;

import java.util.Objects;
import java.util.function.Function;

import com.jiandong.legendaryai.agent.mail.model.InboundMailProps;
import jakarta.mail.internet.MimeMessage;
import org.simplejavamail.api.email.Email;
import org.simplejavamail.converter.EmailConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.mail.dsl.Mail;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.messaging.Message;

@Configuration
@EnableConfigurationProperties(InboundMailProps.class)
class MailAgentFlow {

	private static final Logger log = LoggerFactory.getLogger(MailAgentFlow.class);

	@Value("${spring.mail.username}")
	private String fromForReply;

	@Bean
	IntegrationFlow imapMailFlow(InboundMailProps mailProps, MailAgent mailAgent, JavaMailSender javaMailSender) {
		var user = mailProps.username().replace("@", "%40");
		var url = "%s://%s:%s@%s:%d/INBOX".formatted(mailProps.protocol(), user, mailProps.password(), mailProps.host(), mailProps.port());
		return IntegrationFlow.from(Mail.imapInboundAdapter(url)
						.shouldDeleteMessages(false)
						.shouldMarkMessagesAsRead(true)
						.autoCloseFolder(false), e -> e
						.autoStartup(true)
						.poller(p -> p
								.fixedDelay(120 * 1000)
								.errorChannel("mailErrorChannel")))
				.transform(this::convertToMailAgentContext)

				.transform(mailAgent::classify)
				.log((Function<Message<MailAgentContext>, Object>) agentCtx -> "the mail: " + agentCtx.getPayload().subject + ", is classified to category: " + agentCtx.getPayload().category)

				.filter(MailAgentContext.class, agentCtx -> !"IGNORED".equals(agentCtx.category))

				.transform(mailAgent::resolve)
				.log((Function<Message<MailAgentContext>, Object>) agentCtx -> "resolutionAction: " + agentCtx.getPayload().resolutionAction + ", policyReasoning: " + agentCtx.getPayload().policyReasoning)

				.transform(mailAgent::draftReply)
				.transform(this::convertToMimeMessage)
				.handle(Mail.outboundAdapter(javaMailSender))
				.get();
	}

	private MailAgentContext convertToMailAgentContext(MimeMessage mimeMessage) {
		MailAgentContext agentContext = new MailAgentContext();
		Email email = EmailConverter.mimeMessageToEmail(mimeMessage);

		agentContext.sender = Objects.requireNonNull(email.getFromRecipient()).getAddress();
		agentContext.subject = email.getSubject();
		agentContext.originalEmailBody = email.getPlainText();

		return agentContext;
	}

	private SimpleMailMessage convertToMimeMessage(MailAgentContext agentContext) {
		SimpleMailMessage mailMessage = new SimpleMailMessage();
		mailMessage.setFrom(fromForReply);
		mailMessage.setTo(agentContext.sender);
		mailMessage.setSubject("Re: " + agentContext.subject);
		mailMessage.setText(agentContext.draftedReplyBody);
		return mailMessage;
	}

	@Bean
	IntegrationFlow mailErrorFlow() {
		return IntegrationFlow.from("mailErrorChannel")
				.handle(message -> {
					Throwable cause = (Throwable) message.getPayload();
					log.error("Error processing email", cause);
				})
				.get();
	}

}
