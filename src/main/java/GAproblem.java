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

        double sum = 0.0;

        for(int i=0; i<dt.length; i++){
            sum += dt[i];
        }
        return sum;
    }

    private static Crossover getCrossOverFunction(String type, double args){
        Crossover function;
        switch(type){
            case "SimulatedBinary":
                function =  new SimulatedBinaryCrossover(args);
                break;
            case "SinglePoint":
                function =  new SinglePointCrossover(args);
                break;
            case "DoublePoint":
                function =  new MultiPointCrossover(args);
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

    private static Mutator getMutationFunction(String type, double args){
        Mutator function;
        switch(type){
            case "Gaussian":
                function =  new GaussianMutator(args);
                break;
            default:
                function = new Mutator(args);
        }
        return function;
    }

    private static Selector getSelectionFunction(String type, int args){
        Selector function;
        switch(type){
            case "Tournament":
                function =  new TournamentSelector(args);
                break;
            case "RouletteWheel":
                function =  new RouletteWheelSelector();
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

        Factory<Genotype<DoubleGene>> gtf =
                Genotype.of(DoubleChromosome.of(-5.5, 5.5, 10));


        final Engine<DoubleGene, Double> engine = Engine
                .builder(GAproblem::eval, gtf)
                .populationSize(500)
                .alterers(
                        getCrossOverFunction("SinglePoint", 1),
                        getMutationFunction("Default",1.0/5)
                )
                //.offspringSelector(getSelectionFunction("Roulette", 5))
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