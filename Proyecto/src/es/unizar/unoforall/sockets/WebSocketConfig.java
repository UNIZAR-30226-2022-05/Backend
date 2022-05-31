/*package es.unizar.unoforall.sockets;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurationSupport;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.handler.invocation.HandlerMethodArgumentResolver;
import org.springframework.messaging.handler.invocation.HandlerMethodReturnValueHandler;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
	//extends WebSocketMessageBrokerConfigurationSupport 
//	@Override
//    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
//        super.configureWebSocketTransport(registry);
//    }
//
//    @Override
//    public boolean configureMessageConverters(List<MessageConverter> messageConverters) {
//        return super.configureMessageConverters(messageConverters);
//    }
//
//    @Override
//    public void configureClientInboundChannel(ChannelRegistration registration) {
//        super.configureClientInboundChannel(registration);
//    }
//
//    @Override
//    public void configureClientOutboundChannel(ChannelRegistration registration) {
//        super.configureClientOutboundChannel(registration);
//    }
//
//
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        super.addArgumentResolvers(argumentResolvers);
//    }
//
//    @Override
//    public void addReturnValueHandlers(List<HandlerMethodReturnValueHandler> returnValueHandlers) {
//        super.addReturnValueHandlers(returnValueHandlers);
//    }
	
	
  @Override
  public void configureMessageBroker(MessageBrokerRegistry config) {
    config.enableSimpleBroker("/topic");
    config.setApplicationDestinationPrefixes("/app");
  }

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint("/unoforall").setAllowedOrigins("*").setHandshakeHandler(new DefaultHandshakeHandler());
  }
  
//  @Bean
//  public WebSocketHandler subProtocolWebSocketHandler() {
//	  System.out.println("holo");
//      return new CustomSubProtocolWebSocketHandler(clientInboundChannel(null), clientOutboundChannel(null));
//  }
  
  
  @Override
  public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
      registration.addDecoratorFactory(new WebSocketHandlerDecoratorFactory() {
          @Override
          public WebSocketHandler decorate(final WebSocketHandler handler) {
              return new WebSocketHandlerDecorator(handler) {

                  @Override
                  public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
                      SessionHandler.register(session);

                      super.afterConnectionEstablished(session);
                  }
              };
          }
      });
  }

}*/