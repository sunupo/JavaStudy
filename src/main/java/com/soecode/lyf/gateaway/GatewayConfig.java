package com.soecode.lyf.gateaway;

// 网关用来转发请求到不同的服务，可以实现负载均衡、路由、过滤等功能。在这个例子中，我们使用Spring Cloud Gateway来构建网关，通过配置路由规则，将请求转发到指定的服务。

// 以下是一个完整的Spring Cloud Gateway的demo，包括了路由规则、过滤器等功能。

// 首先，我们需要在pom.xml文件中添加以下依赖：
// <dependency>
//   <groupId>org.springframework.cloud</groupId>
//   <artifactId>spring-cloud-starter-gateway</artifactId>
// </dependency>

// 接着，我们可以在一个Java类中配置我们的网关：

//import org.springframework.cloud.gateway.filter.factory.RequestRateLimiterGatewayFilterFactory;
//import org.springframework.cloud.gateway.route.RouteLocator;
//import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sunupo
 */
//@Configuration
public class GatewayConfig {
    ConcurrentHashMap concurrentHashMap;

//    @Bean
//    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
//        return builder.routes()
//                .route("example_route", r -> r.path("/example")
//                        .uri("http://example.com"))
//                .route("test_route", r -> r.path("/test/**")
//                        .filters(f -> f.stripPrefix(1)
//                                .filter(new RequestRateLimiterGatewayFilterFactory().apply(c -> c.setKeyResolver((exchange) -> Mono.just(exchange.getRequest().getRemoteAddress().getHostName())))))
//                        .uri("lb://test-service"))
//                .build();
//    }
}

// 这个配置设置了两个路由规则：
// 1. 请求路径为"/example"的请求将被转发到"http://example.com"。
// 2. 请求路径为"/test/**"的请求将被转发到名为"test-service"的服务，同时使用了一个过滤器，用于限制请求速率。

// 我们还可以添加更多的路由规则和过滤器，以满足不同的需求。


