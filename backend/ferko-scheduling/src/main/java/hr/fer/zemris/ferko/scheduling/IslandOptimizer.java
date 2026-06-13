package hr.fer.zemris.ferko.scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Coarse-grained parallel ("island") metaheuristic: runs several independent optimizers
 * concurrently and returns the best solution found across all of them. This realises the
 * population-level / algorithm-level parallelization described in chapter 6 of Čupić's thesis and
 * works with any mix of {@link Optimizer} implementations (e.g. several seeds of the same algorithm
 * or a hybrid of different families).
 */
public final class IslandOptimizer implements Optimizer {

  private final List<Optimizer> islands;

  public IslandOptimizer(List<Optimizer> islands) {
    if (islands == null || islands.isEmpty()) {
      throw new IllegalArgumentException("at least one island optimizer is required");
    }
    this.islands = List.copyOf(islands);
  }

  @Override
  public String name() {
    return "PARALLEL_ISLAND";
  }

  @Override
  public OptimizationResult optimize(Problem problem) {
    int threads = Math.min(islands.size(), Math.max(1, Runtime.getRuntime().availableProcessors()));
    ExecutorService pool = Executors.newFixedThreadPool(threads);
    try {
      List<Future<OptimizationResult>> futures = new ArrayList<>();
      for (Optimizer island : islands) {
        Callable<OptimizationResult> task = () -> island.optimize(problem);
        futures.add(pool.submit(task));
      }
      OptimizationResult best = null;
      for (Future<OptimizationResult> future : futures) {
        OptimizationResult result = future.get();
        if (best == null || result.penalty() < best.penalty()) {
          best = result;
        }
      }
      return new OptimizationResult(
          name(), best.assignment(), best.penalty(), best.iterations(), best.penaltyHistory());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Parallel optimization was interrupted", ex);
    } catch (ExecutionException ex) {
      throw new IllegalStateException("Island optimization failed", ex.getCause());
    } finally {
      pool.shutdownNow();
    }
  }
}
