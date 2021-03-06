import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.ext.SimulatedBinaryCrossover;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.Seq;

import java.util.Iterator;

import static io.jenetics.engine.Limits.bySteadyFitness;


public class GAproblem {

    //GenoType
    private static Factory<Genotype<DoubleGene>> gtf =
            Genotype.of(DoubleChromosome.of(0.1, 10, 6));

    //fitness function for performance
    private static double eval(Genotype<DoubleGene> gt) {
        double[] dt = gt.chromosome()
                .as(DoubleChromosome.class)
                .toArray();

        // index 0 - Weight / 1000
        // index 1 - Fuel Consumption/10
        // index 2 - Engine capacity/1000
        // index 3 - Passenger capacity
        // index 4 - Engine power/100
        // index 5 - Battery capacity

        double ratio1 = dt[0] / dt[1];
        double ratio2 = dt[2] / dt[1];
        double ratio3 = dt[3] / dt[1];
        double ratio4 = dt[4] / dt[1];
        double ratio5 = 1 / (dt[5] * dt[1]);

        return ratio1 + ratio2 + (0.65 * ratio3) + (0.35 * ratio4) + ratio5;
    }

    private static Crossover getCrossOverFunction(String type, double args) {
        Crossover function;
        switch (type) {
            case Constants.SIMULATED_BINARY:
                function = new SimulatedBinaryCrossover(args);
                break;
            case Constants.SINGLE_POINT:
                function = new SinglePointCrossover(args);
                break;
            case Constants.MULTI_POINT:
                function = new MultiPointCrossover(args);
                break;
            case Constants.PMX:
                function = new PartiallyMatchedCrossover(args);
                break;
            case Constants.LINE:
                function = new LineCrossover(args);
                break;
            default:
                function = new Crossover(args) {
                    @Override
                    protected int crossover(MSeq mSeq, MSeq mSeq1) {
                        return 0;
                    }
                };
        }
        return function;
    }

    private static Mutator<DoubleGene, Double> getMutationFunction(String type, double args) {
        Mutator function;
        switch (type) {
            case Constants.GAUSSIAN_MUTATOR:
                function = new GaussianMutator(args);
                break;
            case Constants.SWAP_MUTATOR:
                function = new SwapMutator(args);
                break;
            default:
                function = new Mutator(args);
        }
        return function;
    }

    private static Selector<DoubleGene, Double> getSelectionFunction(String type, int args) {
        Selector function;
        switch (type) {
            case Constants.TOURNAMENT_SELECTOR:
                return new TournamentSelector(args);
            case Constants.ROULETTEWHEEL_SELECTOR:
                function = new RouletteWheelSelector();
                break;
            case Constants.TRUNCATION_SELECTOR:
                function = new TruncationSelector();
                break;
            case Constants.MONTECARLO_SELECTOR:
                function = new MonteCarloSelector();
                break;
            default:
                function = new Selector() {
                    @Override
                    public ISeq<Phenotype> select(Seq seq, int i, Optimize optimize) {
                        return null;
                    }
                };
        }
        return function;

    }


    public static void main(String[] args) {

        Engine<DoubleGene, Double> engine = Engine
                .builder(GAproblem::eval, gtf)
                .populationSize(500)
                .alterers(
                        getCrossOverFunction(Constants.SINGLE_POINT, 0.5),
                        getMutationFunction("", 1.0 / 5)

                )
                .offspringFraction(0.7)
                .offspringSelector(getSelectionFunction(Constants.TOURNAMENT_SELECTOR, 5))
                .survivorsSelector(getSelectionFunction(Constants.TRUNCATION_SELECTOR, 5))
                .maximizing()
                .build();

        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        final ISeq<EvolutionResult<DoubleGene, Double>> results = engine
                .stream()
                .limit(bySteadyFitness(7))
                .limit(100)
                .peek(statistics)
                //.flatMap(MinMax.toStrictlyIncreasing())
                .collect(ISeq.toISeq());

        System.out.println(statistics);
        System.out.println("\n\n");

        Iterator<EvolutionResult<DoubleGene, Double>> iterator = results.iterator();
        int interation = 0;
        while (iterator.hasNext()) {
//            Double value = iterator.next().bestPhenotype().fitness();
//            System.out.println("Fitness score of the best Phenotype - interaction" + (++interation));
//            System.out.println(value);

            Phenotype solution = iterator.next().bestPhenotype();
            System.out.println("Best Phenotype - interaction" + (++interation));
            System.out.println(solution);
        }
    }
}