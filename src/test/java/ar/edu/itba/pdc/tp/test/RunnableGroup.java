package ar.edu.itba.pdc.tp.test;

public class RunnableGroup {
	boolean stop = false;

	public void runInThread(Runnable runnable) {
		TestRunnable testRunnable = new TestRunnable(runnable);
		Thread thread = new Thread(testRunnable);
		thread.start();
	}

	public void stop() {
		stop = true;
	}

	private class TestRunnable implements Runnable {
		Runnable runnable;

		TestRunnable(Runnable runnable) {
			this.runnable = runnable;
		}

		@Override
		public void run() {
			try {
				if (stop) {
					return;
				}
				runnable.run();
			} catch (Exception e) {
				stop = true;
				throw new RuntimeException(e);
			}
		}
	}
}
