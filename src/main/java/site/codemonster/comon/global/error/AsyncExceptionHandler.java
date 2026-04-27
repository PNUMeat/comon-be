package site.codemonster.comon.global.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Method;

@Configuration
@Slf4j
public class AsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        log.error("예외 메서드 = {},  예외 메세지 {}", method.getName(), ex.getMessage());

    }
}
