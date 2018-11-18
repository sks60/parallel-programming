package completablefuture_assignment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;

/**
 * A CompletableFuture assignment template class
 * 
 * @Author Shaoqun Wu
 * 
 */
public class Mortality {
	protected static final String DIE_MSG = "Die at %d\n";
	protected static final String WORKING_YEARS_MSG = "Working years=%d\n";
	protected static final String RETIREMENT_YEARS_MSG = "Retirement years=%d\n";
	protected static final String SUPER_PAYOUT_MSG = "Super payout=%.1f (median salaries)\n";
	protected static final String LIFESTYLE_MSG = "You live on %.1f%% of median salary\n";
	protected static Random random = new Random();
	// use debug flag to see the threads in action
	public static boolean debug = true;

	/*
	 * a helper method that delays the execution of the current thread
	 */
	protected static void delay() {
		try {
			// delay a bit
			Thread.sleep(random.nextInt(10) * 100);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * a Supplier that provides the retirement age between 60-70 (inclusive)
	 */
	static Supplier<Integer> RetirementAgeSupplier = () -> {
		String currentThreadName = Thread.currentThread().getName();
		if (debug) {
			// System.out.println("^^"+currentThreadName + "^^ Retrieving Retirement age
			// ....");
		}
		delay();
		int retirementAge = 60 + new Random().nextInt(11);
		if (debug) {
			// System.out.println("^^"+currentThreadName + "^^ returned Retirement age: "+
			// retirementAge);
		}
		return retirementAge;
	};

	/*
	 * Calculates the death age given a gender and birth year.
	 * 
	 * @param gender ("male" or female)
	 * 
	 * @param birthYear (between 1928 and 2018)
	 * 
	 * @return a death age
	 */
	protected static int caculateDeathAge(String gender, int birthYear) {
		final int deathAge;
		if ("male".equals(gender)) {
			deathAge = 79 + (2018 - birthYear) / 20;
		} else {
			deathAge = 83 + (2018 - birthYear) / 30;
		}
		return deathAge;
	}

	/*
	 * calculate performance percentage given a strategy name
	 * 
	 * @param a strategy name
	 * 
	 * @return performance percentage (double)
	 */
	protected static double performance(String strategy) {
		switch (strategy.toLowerCase()) {
		case "growth":
			return 1.045;
		case "balanced":
			return 1.035;
		case "conservative":
			return 1.025;
		case "cash":
			return 1.01;
		default:
			throw new RuntimeException("Unknown Super strategy: " + strategy);
		}
	}

	/**
	 * Displays the final output messages, given the final node of the dataflow
	 * graph.
	 *
	 * @param sependlevel
	 *            calculated based on the performance, contribution and working
	 *            years.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public static void displayResult(double spendLevel) {
		// Get the final output of the dataflow calculation and analyze it.
		// Median income in New Zealand is $44,011.
		// See http://www.superannuation.asn.au/resources/retirement-standard
		// Modest single lifestyle requires $24,506, which is about 0.56 of median
		// income.
		// Comfortable lifestyle requires $44,011, which is about same as median income.
		if (spendLevel < 0.56) {
			System.out.format("Miserable poverty...");
		} else if (spendLevel < 1.0) {
			System.out.format("A modest lifestyle...");
		} else {
			System.out.format("Comfortable!...");
		}
	}

	/*
	 * a method to show how "join" works "join" method is blocking. Async calls are
	 * executed sequentially by one worker (thread) one at a time.
	 */
	static void testWithJoin(String fullname) {
		System.out.println("JOIN method is blocking... (single thread)");
		CompletableFuture.supplyAsync(() -> PersonInfoSupplier.getPersonInfo(fullname)).join();
		CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge).join();
		CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy).join();
		CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution).join();
		CompletableFuture.supplyAsync(RetirementAgeSupplier).join();

	}

	/*
	 * a method to show to how "get" works "get" method is also blocking. Async
	 * calls are executed sequentially by one worker (thread) one at a time.
	 */
	static void testWithGet(String fullname) {
		System.out.println("\nGET method is blocking... (single thread)");
		try {
			CompletableFuture.supplyAsync(() -> PersonInfoSupplier.getPersonInfo(fullname)).get();
			CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge).get();
			CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy).get();
			CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution).get();
			CompletableFuture.supplyAsync(RetirementAgeSupplier).get();

		} catch (Exception e) {
		}

	}

	/*
	 * a method to show how to make Async calls and use "join" at the end to wait
	 * for thread's returning Async calls are executed asynchronously by more than
	 * one workers (threads).
	 */
	static void testWithoutJoinAndGet(String fullname) {
		System.out.println("\nNon-blocking Async calls ... (multiple threads)");

		CompletableFuture[] cfutures = new CompletableFuture[5];

		cfutures[0] = CompletableFuture.supplyAsync(() -> PersonInfoSupplier.getPersonInfo(fullname));
		cfutures[1] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge);
		cfutures[2] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy);
		cfutures[3] = CompletableFuture.supplyAsync(SuperannuatationStrategySupplier::getContribution);
		cfutures[4] = CompletableFuture.supplyAsync(RetirementAgeSupplier);

		CompletableFuture.allOf(cfutures).join();

	}

	public static Integer workingYears(Integer retirement, Integer superAge) {

		return retirement - superAge;
	}

	public static Double callCalcSuper(Integer contribution, Integer workingYears, String strategy) {
		// iterates WorkingYears times, calculating balance = balance *
		// performance(strategy) + contribution / 100.0
		Double balance = 0.0;
		for (int i = 0; i < workingYears; i++) {
			balance = balance * performance(strategy) + contribution / 100.0;
		}
		return balance;
	}

	public static Integer death(Optional<PersonInfo> personInfo) {

		Integer birthYear = personInfo.get().getBirthYear();
		String sex = personInfo.get().getGender();
		return caculateDeathAge(sex, birthYear);
	}

	public static Integer retire(Integer retireAge, Integer deathAge) {
		// outputs the DeathAge result minus 'retirement age'
		return deathAge - retireAge;
	}

	public static Double lifeStyle(Integer retireYear, Double superBalance) {
		// outputs your SuperBalance result divided by the number of years of retirement
		return superBalance / retireYear;
	}

	/**
	 * 
	 * @param fullName
	 *            a person's name (see all the names in the "fullname" file in the
	 *            project directory) to search for
	 */
	public static void query(String fullName) {
		System.out.println(fullName);
		// provide your implementation here
		CompletableFuture<Optional<PersonInfo>> personInfo = CompletableFuture
				.supplyAsync(() -> PersonInfoSupplier.getPersonInfo(fullName));
		CompletableFuture<Integer> superAge = CompletableFuture
				.supplyAsync(SuperannuatationStrategySupplier::getStartSuperAge);
		CompletableFuture<String> strategy = CompletableFuture
				.supplyAsync(SuperannuatationStrategySupplier::getSuperStrategy);
		CompletableFuture<Integer> contribution = CompletableFuture
				.supplyAsync(SuperannuatationStrategySupplier::getContribution);
		CompletableFuture<Integer> retirementAge = CompletableFuture.supplyAsync(RetirementAgeSupplier);

		CompletableFuture<Integer> workingYears = retirementAge.thenCombine(superAge, Mortality::workingYears);
		CompletableFuture<Double> superBalance = workingYears
				.thenCompose(wY -> strategy.thenCombine(contribution, (s, c) -> callCalcSuper(c, wY, s)));
		CompletableFuture<Integer> deathAge = personInfo.thenApply(Mortality::death);
		CompletableFuture<Integer> retireAge = retirementAge.thenCombine(deathAge, Mortality::retire);
		CompletableFuture<Double> lifeStyle = retireAge.thenCombine(superBalance, Mortality::lifeStyle);

		try {
			System.out.println("	 start super age = " + superAge.get());
			System.out.println("	 retirement age = " + retirementAge.get());
			System.out.println("Working years = " + workingYears.get());
			System.out.println("	 super strategy == " + strategy.get());
			System.out.println("	 contribution = " + contribution.get() + "%");
			System.out.println("Super payout = " + String.format("%.1f", superBalance.get()) + " (median salaries)");
			System.out.println("	 birth year = " + PersonInfoSupplier.getPersonInfo(fullName).get().getBirthYear());
			System.out.println("	 sex = " + PersonInfoSupplier.getPersonInfo(fullName).get().getGender());
			System.out.println("Die at " + deathAge.get());
			System.out.println("Retirement years = " + retireAge.get());
			System.out.println("You live on " + String.format("%.1f", lifeStyle.get() * 100.0) + "% of median salary");// format to nearest whole number
			displayResult(lifeStyle.get());
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		String fullName = "Mia Collins";
		// uncomment to experiment with "join" and "get" methods
		// testWithJoin(fullName);
		// testWithGet(fullName);
		// testWithoutJoinAndGet(fullName);

		query(fullName);
	}
}
