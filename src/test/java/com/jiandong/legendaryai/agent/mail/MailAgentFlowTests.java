package com.jiandong.legendaryai.agent.mail;

import java.io.IOException;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.jiandong.legendaryai.agent.mail.model.InboundMailProps;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.mail.autoconfigure.MailProperties;
import org.springframework.boot.mail.autoconfigure.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.endpoint.SourcePollingChannelAdapter;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@EnableIntegration
@DirtiesContext
@SpringBootTest(classes = {MailAgentFlow.class})
@ImportAutoConfiguration({MailSenderAutoConfiguration.class})
class MailAgentFlowTests {

	@Autowired
	MailProperties senderMailProperties;

	@Autowired
	InboundMailProps receiverMailProperties;

	@Autowired
	JavaMailSender javaMailSender;

	@Autowired
	@Qualifier("mailReceiverAdapter")
	SourcePollingChannelAdapter mailReceiverAdapter;

	@MockitoBean
	MailAgent mailAgent;

	GreenMail mailServer;

	String sender;

	String receiver;

	@BeforeEach
	void setup() {
		mailServer = new GreenMail(ServerSetupTest.SMTP_IMAP);

		sender = senderMailProperties.getUsername();
		receiver = receiverMailProperties.username();

		mailServer.setUser(sender, senderMailProperties.getPassword());
		mailServer.setUser(receiver, receiverMailProperties.password());
		mailServer.start();
	}

	@AfterEach
	void tearDown() {
		mailServer.stop();
	}

	@Test
	void happyFlow() throws MessagingException, IOException {
		// send a request email
		String subject = "request help for my desktop issue";
		String content = "my desktop can not startup, can please check?";
		SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
		simpleMailMessage.setFrom(sender);
		simpleMailMessage.setTo(receiver);
		simpleMailMessage.setSubject(subject);
		simpleMailMessage.setText(content);
		javaMailSender.send(simpleMailMessage);

		// ensure email is in server
		mailServer.waitForIncomingEmail(10000, 1);
		MimeMessage[] mimeMessages = mailServer.getReceivedMessagesForDomain("cn");
		assertThat(mimeMessages.length > 0).isTrue();
		MimeMessage message = mimeMessages[0];
		assertThat(message.getFrom()).containsOnly(new InternetAddress(sender));
		assertThat(message.getRecipients(MimeMessage.RecipientType.TO)).containsOnly(new InternetAddress(receiver));
		assertThat(message.getSubject()).isEqualTo(subject);
		assertThat(message.getContent()).asString().isEqualTo(content);

		// mock mail agent classify/resolve/draftReply
		MailAgentContext agentContext = new MailAgentContext();
		agentContext.subject = subject;
		agentContext.sender = sender;
		agentContext.originalEmailBody = content;
		agentContext.category = "TECHNICAL";

		when(mailAgent.classify(any())).thenReturn(agentContext);

		agentContext.resolutionAction = "Offer a replacement";
		agentContext.policyReasoning = "customer desktop is broken";
		when(mailAgent.resolve(any())).thenReturn(agentContext);

		agentContext.draftedReplyBody = "Dear " + sender + ", Sorry, and we will give you a new desktop!";
		when(mailAgent.draftReply(any())).thenReturn(agentContext);

		// start receiving
		mailReceiverAdapter.start();

		// very reply is in mail server
		mailServer.waitForIncomingEmail(10000, 2);
		MimeMessage[] replyMimeMessages = mailServer.getReceivedMessagesForDomain("com");
		assertThat(replyMimeMessages.length > 0).isTrue();
		MimeMessage replyMessage = replyMimeMessages[0];
		assertThat(replyMessage.getFrom()).containsOnly(new InternetAddress(receiver));
		assertThat(replyMessage.getRecipients(MimeMessage.RecipientType.TO)).containsOnly(new InternetAddress(sender));
		assertThat(replyMessage.getSubject()).isEqualTo("Re: " + subject);
		assertThat(replyMessage.getContent()).asString().isEqualTo(agentContext.draftedReplyBody);

		mailReceiverAdapter.stop();
	}

}
