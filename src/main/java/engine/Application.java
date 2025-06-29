package engine;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;

import static java.lang.Runtime.getRuntime;
import static java.lang.System.out;
import static java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory;
import static org.springframework.boot.SpringApplication.run;

@SpringBootApplication
@EnableCaching
public class Application {
    private final static UncaughtExceptionHandler HANDLER;

    static {
        HANDLER = (t, e) -> out.printf("ForkJoinPool threw '%s' in '%s'\n", e.getMessage(), t.getName());
    }

    public static void main(String[] args) {
        Thread.setDefaultUncaughtExceptionHandler(HANDLER);
        run(Application.class, args);
    }

    @Bean
    @Scope("prototype")
    public ForkJoinPool forkJoinPool() {
        ForkJoinPool.ForkJoinWorkerThreadFactory factory = pool -> {
            ForkJoinWorkerThread worker = defaultForkJoinWorkerThreadFactory.newThread(pool);
            worker.setUncaughtExceptionHandler(HANDLER);
            return worker;
        };

        return new ForkJoinPool(getRuntime().availableProcessors(),
                factory,
                HANDLER,
                true);
    }

}
