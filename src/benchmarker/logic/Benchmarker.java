package benchmarker.logic;

import benchmarker.sort.Sorter;
import benchmarker.utils.CSVUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Benchmarker {

    public static final int testsCount = 5;

    private Benchmark currentBenchmark;
    private final HashMap<String, Benchmark> benchmarks;

    private final HashMap<String, Class<? extends Sorter>> sortClasses;
    private final List<Sorter> sortPool;


    public Benchmarker() {
        benchmarks = new HashMap<>();
        sortClasses = new HashMap<>();
        sortPool = new ArrayList<>();
    }


    public void addBenchmark(Benchmark benchmark) {
        String name = benchmark.getName();
        benchmarks.put(name, benchmark);
    }

    public List<String> getAvailableBenchmarks() {
        return new ArrayList<>(benchmarks.keySet());
    }

    public Benchmark getBenchmark(String benchmark) {
        return benchmarks.get(benchmark);
    }


    public List<String> getAvailableSorter() {
        return new ArrayList<>(sortClasses.keySet());
    }

    public List<Sorter> getSortPool() {
        return sortPool;
    }

    public void addSorterClass(Class<? extends Sorter> sorter) {
        String name = sorter.getSimpleName();
        sortClasses.put(name, sorter);
    }

    public Sorter initOne(String name) {
        return initOne(name, true);
    }

    public Sorter initOne(String name, boolean addToSortPool) {
        Class<? extends Sorter> sorterClass = sortClasses.get(name);
        try {
            Sorter sorter = sorterClass.getDeclaredConstructor().newInstance();
            if (addToSortPool) {
                sortPool.add(sorter);
                sorter.initSorter();
            }
            return sorter;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void addToSortPool(Sorter sorter) {
        try {
            sorter.initSorter();
            this.sortPool.add(sorter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeSorter(Sorter sorter) {
        sortPool.remove(sorter);
    }

    public void initAll() {
        for (String name : sortClasses.keySet()) {
            Class<? extends Sorter> sorterClass = sortClasses.get(name);
            try {
                Sorter sorter = sorterClass.getDeclaredConstructor().newInstance();
                addToSortPool(sorter);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public HashMap<Sorter, Integer> testAlgorithms() {
        HashMap<Sorter, Integer> result = new HashMap<>();

        for (Sorter sorter : sortPool) {
            result.put(sorter, 0);
        }

        int[] arr = null;
        for (int test = 0; test < testsCount; test++) {
            switch (test) {
                case 0:
                    arr = new int [1000];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = i;
                    }
                    break;
                case 1:
                    arr = new int [1000];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = arr.length - i - 1;
                    }
                    break;
                case 2:
                    arr = new int [1000];
                    for (int i = 0; i < arr.length; i++) {
                        arr[i] = (int) (Math.random() * 100);
                    }
                    break;
                case 3:
                    arr = new int[] {0};
                    break;
                case 4:
                    arr = new int[0];
                    break;
            }
            for (Sorter sorter : sortPool) {
                int[] toSort = Arrays.copyOf(arr, arr.length);
                int[] sorted = sorter.sort(toSort);
                if (isSorted(sorted, toSort.length)) {
                    result.put(sorter, result.get(sorter) + 1);
                    if (result.get(sorter) == testsCount) {
                        sorter.setTestsPassed(true);
                    }
                }
            }
        }
        return result;
    }

    private boolean isSorted(int[] arr, int length) {
        if (arr.length != length) return false;
        int direction = 0; // 1 = ASC; 2 = DESC
        for (int i = 0; i < arr.length - 1; i++) {
            if (arr[i] < arr[i + 1]) {
                if (direction == 2) {
                    return false;
                }
                direction = 1;
            } else if (arr[i] > arr[i + 1]) {
                if (direction == 1) {
                    return false;
                }
                direction = 2;
            }
        }
        return true;
    }

    public void sort(int[] array) {
        for (Sorter sorter : sortPool) {
            int[] toSort = Arrays.copyOf(array, array.length);
            System.out.println("Soter: " + sorter.getName());
            long start = System.currentTimeMillis();
            sorter.sort(toSort);
            long stop = System.currentTimeMillis();
            System.out.println("Duration: " + (stop - start) + " ms");
            System.out.println();
        }
    }

    public void benchmark(String benchmark) {
        if (benchmarks.get(benchmark) != null) {
            benchmark(benchmarks.get(benchmark));
        }
    }

    public Benchmark getCurrentBenchmark() {
        return currentBenchmark;
    }

    public void benchmark(Benchmark benchmark) {
        currentBenchmark = benchmark;
        benchmark.beforeBenchmark();
        benchmark.benchmark(sortPool);
    }

    public boolean exportResults() throws IOException {
        if (getCurrentBenchmark() == null) {
            return false;
        }

        File exportFolder = new File("exports");
        if (!exportFolder.exists()) {
            exportFolder.mkdir();
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdc = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        String exportFileName = getCurrentBenchmark().getName() + " " + sdc.format(cal.getTime());

        File file = new File("exports/" + exportFileName + ".csv");

        CSVUtils csvUtils = new CSVUtils(file);
        getCurrentBenchmark().exportResults(csvUtils);
        return csvUtils.writeFile();
    }
}
