package com.jiandong.legendaryai.agent.mail;

import com.jiandong.legendaryai.agent.mail.model.ClassifierResult;
import com.jiandong.legendaryai.agent.mail.model.ResolverResult;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.stereotype.Component;

@Component
class MailAgent {

	private static final String CLASSIFY_PROMPT = """
			Analyze the following email. Determine if it is a customer support ticket.
			If yes, classify the issue strictly as BILLING, DELIVERY, or TECHNICAL.
			If it is not a support ticket (e.g., spam, newsletter, internal), classify it as IGNORED.
			
			Email Body:
			%s
			""";

	private static final String RESOLVE_PROMPT = """
			You are a strict policy engine. Based on the ticket category, determine the resolution.
			- If DELIVERY or TECHNICAL: Offer the customer a replacement.
			- If BILLING: Enforce the '50 feet or 50 seconds' policy. Reject the request and explain that no
			  refunds are provided for billing inquiries.
			Ticket Category: %s
			Original Email: %s
			Output the exact action to take and the internal reasoning.
			""";

	private static final String DRAFT_PROMPT = """
			You are a customer support representative. Draft a polite, empathetic email reply to the customer.
			Use the original email for context.
			Apply the following resolution strictly: %s
			Do not invent new policies. Keep the tone professional but warm. Sign off as 'Automated Support Team'.
			
			Original Email: %s
			""";

	private final ChatClient chatClient;

	MailAgent(ChatClient.Builder chatClientBuilder) {
		this.chatClient = chatClientBuilder.build();
	}

	MailAgentContext classify(MailAgentContext context) {
		ClassifierResult result = this.chatClient.prompt()
				.user(String.format(CLASSIFY_PROMPT, context.originalEmailBody))
				.call()
				.entity(new BeanOutputConverter<>(ClassifierResult.class));

		if (result != null) {
			context.category = result.isSupportTicket() ? result.category() : "TECHNICAL";
		}
		else {
			context.category = "IGNORED";
		}
		return context;
	}

	MailAgentContext resolve(MailAgentContext context) {
		ResolverResult result = this.chatClient.prompt()
				.user(String.format(RESOLVE_PROMPT, context.category, context.originalEmailBody))
				.call()
				.entity(new BeanOutputConverter<>(ResolverResult.class));

		if (result != null) {
			context.resolutionAction = result.action();
			context.policyReasoning = result.reasoning();
		}
		return context;
	}

	MailAgentContext draftReply(MailAgentContext context) {
		context.draftedReplyBody = this.chatClient.prompt()
				.user(String.format(DRAFT_PROMPT, context.resolutionAction, context.originalEmailBody))
				.call()
				.content();

		return context;
	}

}
