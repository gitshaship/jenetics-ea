import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.ext.SimulatedBinaryCrossover;
import io.jenetics.util.Factory;
import io.jenetics.util.ISeq;
import io.jenetics.util.MSeq;
import io.jenetics.util.Seq;


public class GAproblem {
    // 2.) Definition of the fitness function.
    private static double eval(Genotype<DoubleGene> gt) {
        double[] dt =  gt.chromosome()
                .as(DoubleChromosome.class)
                .toArray();

        // index 0 - Wight / 1000
        // index 1 - Fuel Consumption
        // index 2 - Engine capacity
        // index 3 - Passenger capacity
        // index 4 - Engine power
        // index 5 - Battery capacity

        double ratio1 = (dt[0] * 1000) / dt[1];
        double ratio2 = dt[2] / dt[1];
        double ratio3 = dt[3] / dt[1];
        double ratio4 = dt[4] / dt[1];
        double ratio5 = 1 / (dt[5] * dt[1]);

        return ratio1 + ratio2 + (0.65 * ratio3) + (0.35 * ratio4) + ratio5;
    }

    private static Crossover getCrossOverFunction(String type, double args){
        return switch (type) {
            case "SimulatedBinary" -> new SimulatedBinaryCrossover(args);
            case "SinglePoint" -> new SinglePointCrossover(args);
            case "DoublePoint" -> new MultiPointCrossover(args);
            default -> new Crossover(args) {
                @Override
                protected int crossover(MSeq mSeq, MSeq mSeq1) {
                    return 0;
                }
            };
        };
    }

    private static Mutator getMutationFunction(String type, double args){
        return switch (type) {
            case "Gaussian" -> new GaussianMutator(args);
            default -> new Mutator(args);
        };
    }

    private static Selector getSelectionFunction(String type, int args){
        return switch (type) {
            case "Tournament" -> new TournamentSelector(args);
            case "RouletteWheel" -> new RouletteWheelSelector();
            default -> (seq, i, optimize) -> null;
        };
    }

    public static void main(String[] args) {

        Factory<Genotype<DoubleGene>> gtf =
                Genotype.of(DoubleChromosome.of(0, 10, 10));


        final Engine<DoubleGene, Double> engine = Engine
                .builder(GAproblem::eval, gtf)
                .populationSize(500)
                .alterers(
                        getCrossOverFunction("SinglePoint", 1),
                        getMutationFunction("Default",1.0/5)
                )
                .offspringSelector(getSelectionFunction("Tournament", 5))
                .maximizing()
                .build();

        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene, Double> best  = engine.stream()
                .limit(bySteadyFitness(7))
                .limit(100)
                .peek(statistics)
                .collect(toBestPhenotype());

        System.out.println(statistics);
        System.out.println(best);
        System.out.println("\n\n");
        System.out.printf(
                "Genotype of best item: %s%n",
                best.genotype()
        );

    }
}