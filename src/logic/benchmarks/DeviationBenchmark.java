package logic.benchmarks;

import logic.Benchmark;
import sort.Sorter;
import utils.Settings;
import utils.options.Option;
import utils.options.OptionType;

import javax.swing.*;
import java.util.*;

public class DeviationBenchmark extends Benchmark {

    public DeviationBenchmark() {
        addOption(new Option("Array-Größe", OptionType.NUMBER, 500000));
        addOption(new Option("Iterationen", OptionType.NUMBER, 50));
    }

    @Override
    public String getName() {
        return "Abweichung Benchmark";
    }

    @Override
    public void benchmark(List<Sorter> sortPool) {
        int arraySize = (int) getValue("Array-Größe");
        int iterations = (int) getValue("Iterationen");

        int[] arr = new int[arraySize];
        getArray(arr);
        ArrayList<Long> tempResults = new ArrayList<>();
        for (Sorter sorter : sortPool) {
            if (!sorter.passedTests()) continue;
            tempResults.clear();
            for (int i = 0; i < iterations; i++) {
                int[] toSort = Arrays.copyOf(arr, arr.length);
                long start = System.currentTimeMillis();
                sorter.sort(toSort);
                long stop = System.currentTimeMillis();
                tempResults.add(stop - start);
            }

            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;

            for (long value : tempResults) {
                if (value < min) min = value;
                if (value > max) max = value;
            }

            long sum = 0;
            for (long l : tempResults) {
                sum += l;
            }
            double mean = sum / (double) tempResults.size();

            long range = max - min;
            double standardDeviation = calculateSD(tempResults, mean);
            double coefficientOfVariation = (standardDeviation / mean) * 100;

            HashMap<String, Object> result = new HashMap<>();
            result.put("range", range);
            result.put("standardDeviation", standardDeviation);
            result.put("coefficientOfVariation", coefficientOfVariation);

            updateResult(sorter, result);
        }
    }


    // Standard Deviation / Standardabweichung
    private double calculateSD(ArrayList<Long> list, double mean) {
        double variance = 0;
        for (long l : list) {
            variance += Math.pow(l - mean, 2);
        }
        variance /= list.size() - 1;
        return Math.sqrt(variance);
    }

    @Override
    protected void updateResult(JPanel panel, Object data) {
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        HashMap<String, Object> result = (HashMap<String, Object>) data;

        long range = (Long) result.get("range");
        double standardDeviation = (Double) result.get("standardDeviation");
        double coefficientOfVariation = (Double) result.get("coefficientOfVariation");

        JLabel rangeLabel = new JLabel("Spannweite: " + range + " ms");
        rangeLabel.setFont(Settings.font);

        JLabel deviationLabel = new JLabel("Standardabweichung: " + Math.round(standardDeviation * 100) / 100d + " ms");
        deviationLabel.setFont(Settings.font);

        JLabel variationLabel = new JLabel("Variationskoeffizient: " + Math.round(coefficientOfVariation * 100) / 100d + "%");
        variationLabel.setFont(Settings.font);

        panel.add(rangeLabel);
        panel.add(deviationLabel);
        panel.add(variationLabel);
    }
}
