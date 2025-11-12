package ru.practicum.explorewithme.web;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.stats.client.StatsClient;
import ru.practicum.stats.dto.NewHitDto;

import java.lang.reflect.Field;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class StatsInterceptorTest {

    @RestController
    static class DummyController {
        @GetMapping("/events")
        public String e1() {
            return "ok";
        }

        @GetMapping("/events/123")
        public String e2() {
            return "ok";
        }

        @GetMapping("/categories")
        public String c() {
            return "ok";
        }
    }

    @Test
    void onlyEventsAreLogged() throws Exception {
        StatsClient client = Mockito.mock(StatsClient.class);
        Mockito.when(client.saveHit(ArgumentMatchers.any(NewHitDto.class)))
                .thenReturn(ResponseEntity.ok().build());

        StatsInterceptor interceptor = new StatsInterceptor(client);
        Field f = StatsInterceptor.class.getDeclaredField("appName");
        f.setAccessible(true);
        f.set(interceptor, "ewm-main-service");

        MockMvc mvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .addInterceptors(interceptor)
                .build();

        mvc.perform(get("/events").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mvc.perform(get("/events/123").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        mvc.perform(get("/categories").accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

        Mockito.verify(client, Mockito.times(2)).saveHit(ArgumentMatchers.any(NewHitDto.class));
    }
}
