package main;

import org.jgap.gp.CommandGene;
import org.jgap.gp.GPFitnessFunction;
import org.jgap.gp.IGPProgram;
import org.jgap.gp.impl.DeltaGPFitnessEvaluator;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Variable;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final Object[] NO_ARGS= new Object[0];
    public static final double MIN_ER = 0.02;//minimum error
    public static final int MAX_EVO = 300;//max evolutions

    private MathsProblem prob;//problem
    private GPConfiguration config;
    private String attrNamesFileName;
    private final List<Patient> trainPat;//training patients
    private final List<Patient> testPat;


    /**
     * Start the algorithm. It starts by loading in the data.
     * @param trainingFile
     * @param testFile
     * @param nameFile
     */
    private Main(String trainingFile, String testFile, String nameFile) {
        // Init
        attrNamesFileName=nameFile;
        trainPat = new ArrayList<>();
        testPat = new ArrayList<>();


        Scanner scan;
        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(trainingFile)));
        }catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

        for (String line = scan.nextLine(); scan.hasNextLine(); line = scan.nextLine()) {
            String[] data = line.split(",");
            trainPat.add(new Patient(data));
        }

        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(testFile)));
        }catch (NullPointerException e){
            e.printStackTrace();
            return;
        }

        // Read in data
        for (String line = scan.nextLine(); scan.hasNextLine(); line = scan.nextLine()) {
            String[] data = line.split(",");
            testPat.add(new Patient(data));
        }
    }


    /**
     * Initialise the <code>config</code> used for the program and creates a <code>problem</code>
     *
     * @throws Exception
     */
    private void initConfig() throws Exception {

        config = new GPConfiguration();
        config.setGPFitnessEvaluator(new DeltaGPFitnessEvaluator());
        config.setFitnessFunction(new PatientFitnessFunction(trainPat));

        config.setMaxInitDepth(4);
        config.setPopulationSize(1000);
        config.setMaxCrossoverDepth(6);
        config.setStrictProgramCreation(true);
        config.setCrossoverProb(0.9f);
        config.setMutationProb(0.2f);
        config.setReproductionProb(0.05f);

        Variable[] vars = createVariables(config);
        prob = new MathsProblem(config, vars);
    }

    public Variable[] createVariables(GPConfiguration config) throws Exception {
        // Init
        Variable[] variables = new Variable[9];

        Scanner scan;
        try {
            scan = new Scanner(new InputStreamReader(ClassLoader.getSystemResourceAsStream(attrNamesFileName)));
        }catch (NullPointerException e){
            throw new RuntimeException("Invalid file specified");
        }

        for (int i=0; i < 9; i++) {
            String name = scan.nextLine();
            variables[i] = new Variable(config, name, CommandGene.DoubleClass);
        }

        return variables;
    }


    /**
     * Evolve the problem and get the best solution
     *
     * @throws Exception
     */
    private void run() throws Exception {
        GPGenotype gp = prob.create();
        gp.setGPConfiguration(config);
        gp.setVerboseOutput(true);
        evolve(gp);

        gp.outputSolution(gp.getAllTimeBest());
        prob.showTree(gp.getAllTimeBest(), "best-solution.png");
        testAlgorithm(gp);
    }


    /**
     * @param program
     */
    private void evolve(GPGenotype program) {
        int offset = program.getGPConfiguration().getGenerationNr();

        int i;
        for (i = 0; i < MAX_EVO; ++i) {
            program.evolve();
            program.calcFitness();
            double fitness = program.getAllTimeBest().getFitnessValue();

            if (fitness < MIN_ER) {
                break;
            }


            if (i % 25 == 0) {
                System.out.println("Evolving generation " + (i + offset) + "Fittest program: " + fitness);
            }
        }
    }


    /**
     * Run the algorithm on the test data and see the performance
     * @param gp
     */
    private void testAlgorithm(GPGenotype gp) {
        // Training accuracy
        PatientFitnessFunction fitnessFunction = new PatientFitnessFunction(trainPat);
        double result = fitnessFunction.evaluate(gp.getAllTimeBest()) * 100; // convert incorrect to percentage
        result = 100 - result;
        System.out.println("\nPercentage of training instances correctly classified: " + String.format( "%.4f", result) + "%");

        fitnessFunction = new PatientFitnessFunction(testPat);
        result = fitnessFunction.evaluate(gp.getAllTimeBest()) * 100; // convert incorrect to percentage
        result = 100 - result;
        System.out.println("\nPercentage of test instances correctly classified: " + String.format( "%.4f", result) + "%");
    }


    /**
     * The function that evaluates the fitness of a program
     */
    public class PatientFitnessFunction extends GPFitnessFunction {

        private List<Patient> instances;

        public PatientFitnessFunction(List<Patient> instances){
            this.instances = instances;
        }

        @Override
        protected double evaluate(IGPProgram igpProgram) {
            double correct = 0;

            for (Patient patient : instances) {
                prob.setVariablesOfPatient(patient);
                double result = igpProgram.execute_double(0, NO_ARGS);

                int predictedClass;
                if (result < 0) {
                    predictedClass = 2;
                } else {
                    predictedClass = 4;
                }
                if (predictedClass == patient.getCondition()) {
                    correct++;
                }
            }

            if (correct < MIN_ER) {
                correct = 0.0;
            }
            return correct / instances.size();
        }
    }


    /**
     * Entry point of the program
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            System.out.println("Invalid program arguments");
        }
        Main main = new Main(args[0], args[1], args[2]);
        main.initConfig();
        main.run();
    }



}