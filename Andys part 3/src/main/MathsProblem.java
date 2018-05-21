package main;

import org.jgap.InvalidConfigurationException;
import org.jgap.gp.CommandGene;
import org.jgap.gp.GPProblem;
import org.jgap.gp.function.*;
import org.jgap.gp.impl.GPConfiguration;
import org.jgap.gp.impl.GPGenotype;
import org.jgap.gp.terminal.Terminal;
import org.jgap.gp.terminal.Variable;



public class MathsProblem extends GPProblem {

    private GPConfiguration config;
    private Variable[] variables;


    public MathsProblem(GPConfiguration config, Variable[] variables) throws InvalidConfigurationException {
        super(config);
        this.config = config;
        this.variables = variables;
    }

    public void setVariablesOfPatient(Patient patient){
        // Get patient variables
        int[] patientAttributes = patient.getAttributes();
        assert patientAttributes.length == variables.length;

        // Set variables
        for (int i = 0; i < variables.length; i++) {
            variables[i].set((double)patientAttributes[i]);
        }
    }


    @Override
    public GPGenotype create() throws InvalidConfigurationException {
        Class[] types = {CommandGene.DoubleClass};
        Class[][] argTypes = {{},};

        CommandGene[] mathsCommands = {

                new Add(config, CommandGene.DoubleClass),
                new Multiply(config, CommandGene.DoubleClass),
                new Divide(config, CommandGene.DoubleClass),
                new Subtract(config, CommandGene.DoubleClass),
                new Terminal(config, CommandGene.DoubleClass, -1.0d, 10.0d, true),

        };

        CommandGene[] allCommandGenes = new CommandGene[mathsCommands.length + variables.length];
        for(int i = 0; i < variables.length; i++){
            allCommandGenes[i] = variables[i];
        }
        for (int i = variables.length; i < allCommandGenes.length; i++) {
            allCommandGenes[i] = mathsCommands[i-variables.length];
        }

        CommandGene[][] nodeSets = new CommandGene[2][allCommandGenes.length];
        nodeSets[0] = allCommandGenes;
        nodeSets[1] = new CommandGene[0];

        return GPGenotype.randomInitialGenotype(config, types, argTypes, nodeSets, 20, true);
    }

}
