package ru.practicum.shareit.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.shareit.booking.BookingClient;
import ru.practicum.shareit.item.ItemClient;
import ru.practicum.shareit.request.ItemRequestClient;
import ru.practicum.shareit.user.UserClient;

@Configuration
public class WebClientConfig {

    @Value("${shareit-server.url}")
    private String serverUrl;

    @Bean
    public RestTemplate bookingsRestTemplate() {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/bookings"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    @Bean
    public RestTemplate itemsRestTemplate() {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/items"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    @Bean
    public RestTemplate requestsRestTemplate() {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/requests"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    @Bean
    public RestTemplate usersRestTemplate() {
        return new RestTemplateBuilder()
                .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + "/users"))
                .requestFactory(() -> new HttpComponentsClientHttpRequestFactory())
                .build();
    }

    @Bean
    public BookingClient bookingClient(RestTemplate bookingsRestTemplate) {
        return new BookingClient(bookingsRestTemplate);
    }

    @Bean
    public ItemClient itemClient(RestTemplate itemsRestTemplate) {
        return new ItemClient(itemsRestTemplate);
    }

    @Bean
    public ItemRequestClient requestClient(RestTemplate requestsRestTemplate) {
        return new ItemRequestClient(requestsRestTemplate);
    }

    @Bean
    public UserClient userClient(RestTemplate usersRestTemplate) {
        return new UserClient(usersRestTemplate);
    }
}


