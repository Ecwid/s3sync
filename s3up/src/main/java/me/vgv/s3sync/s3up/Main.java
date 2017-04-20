package me.vgv.s3sync.s3up;

import me.vgv.s3sync.s3up.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author Vasily Vasilkov (vgv@vgv.me)
 */
public class Main {

	public static void main(String[] args) throws Throwable {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				System.err.println(e.getMessage());
				System.exit(1);
			}
		});

		// parse config
		Config config = Utils.parseConfig(args);
		System.out.println("Will upload to bucket " + config.getS3Settings().getBucket());

		long start = System.currentTimeMillis();

		// submit all tasks
		ExecutorService executorService = Executors.newFixedThreadPool(config.getThreads());
		List<Future> futures = new ArrayList<Future>();
		for (UploadFile uploadFile : config.getUploadFiles()) {
			Future<?> future = executorService.submit(new UploadTask(config, uploadFile));
			futures.add(future);
		}

		// wait all tasks to complete
		executorService.shutdown();
		for (Future<?> future : futures) {
			try {
				future.get();
			} catch (ExecutionException e) {
			   throw e.getCause();
			}
		}

		System.out.println("Uploaded in " + (System.currentTimeMillis() - start) / 1000 + " seconds.");
	}


}
