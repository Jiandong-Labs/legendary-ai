package com.jiandong.legendaryai.agent.mail;

import com.jiandong.legendaryai.agent.mail.model.ClassifierResult;
import com.jiandong.legendaryai.agent.mail.model.ResolverResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClient.CallResponseSpec;
import org.springframework.ai.chat.client.ChatClient.ChatClientRequestSpec;
import org.springframework.ai.converter.BeanOutputConverter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailAgentTest {

	@Mock
	ChatClient.Builder chatClientBuilder;

	@InjectMocks
	MailAgent mailAgent;

	@Mock
	ChatClient chatClient;

	@Mock
	ChatClientRequestSpec requestSpec;

	@Mock
	CallResponseSpec responseSpec;

	@BeforeEach
	void setUp() {
		when(chatClientBuilder.build()).thenReturn(chatClient);
		mailAgent = new MailAgent(chatClientBuilder);
	}

	@Test
	void shouldClassifyEmailCorrectly() {
		MailAgentContext context = new MailAgentContext();
		context.originalEmailBody = "I want a refund for invoice #123";
		ClassifierResult mockResult = new ClassifierResult(true, "BILLING");

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(any(BeanOutputConverter.class))).thenReturn(mockResult);

		MailAgentContext updatedContext = mailAgent.classify(context);

		assertThat(updatedContext.category).isEqualTo("BILLING");
	}

	@Test
	void shouldResolveTicketCorrectly() {

		MailAgentContext context = new MailAgentContext();
		context.category = "BILLING";
		context.originalEmailBody = "I want a refund for invoice #123";
		ResolverResult mockResult = new ResolverResult("REJECT", "Violates 50 feet policy");

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.entity(any(BeanOutputConverter.class))).thenReturn(mockResult);

		MailAgentContext updatedContext = mailAgent.resolve(context);

		assertThat(updatedContext.resolutionAction).isEqualTo("REJECT");
		assertThat(updatedContext.policyReasoning).isEqualTo("Violates 50 feet policy");
	}

	@Test
	void shouldDraftReplyCorrectly() {

		MailAgentContext context = new MailAgentContext();
		context.resolutionAction = "REJECT";
		context.originalEmailBody = "I want a refund for invoice #123";
		String expectedReply = "Dear customer, we cannot fulfill your refund request due to company policy.";

		when(chatClient.prompt()).thenReturn(requestSpec);
		when(requestSpec.user(anyString())).thenReturn(requestSpec);
		when(requestSpec.call()).thenReturn(responseSpec);
		when(responseSpec.content()).thenReturn(expectedReply);

		MailAgentContext updatedContext = mailAgent.draftReply(context);

		assertThat(updatedContext.draftedReplyBody).isEqualTo(expectedReply);
	}

}
