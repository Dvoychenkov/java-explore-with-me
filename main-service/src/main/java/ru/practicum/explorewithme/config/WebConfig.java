package ru.practicum.explorewithme.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.practicum.explorewithme.web.StatsInterceptor;

import static ru.practicum.explorewithme.util.AppConstants.EVENTS_URL_PREFIX;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final StatsInterceptor statsInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(statsInterceptor)
                .addPathPatterns(
                        EVENTS_URL_PREFIX.substring(0, EVENTS_URL_PREFIX.length() - 1),
                        EVENTS_URL_PREFIX + "*"
                );
    }
}