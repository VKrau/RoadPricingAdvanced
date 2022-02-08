package org.matsim.tools;

import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.population.io.PopulationReader;
import org.matsim.core.population.io.PopulationWriter;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.utils.objectattributes.ObjectAttributesXmlWriter;

import java.util.logging.Logger;

public class MergingPlans {
    private static Logger logger = Logger.getLogger(MergingPlans.class.getName());

    private static void createAttributes(Scenario scenario) {
        logger.warning("Creating attributes");
        for(Person person : scenario.getPopulation().getPersons().values()) {
            if(person.getId().toString().contains("truck")) {
                scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(), "subpopulation", "truck");
            } else {
                scenario.getPopulation().getPersonAttributes().putAttribute(person.getId().toString(), "subpopulation", "car");
            }
        }
    }

    public static void run(String fileWithGeneralPlans, String fileWithAdditionalPlans, boolean createSubpopulationAttributes) {
        Scenario scenario = ScenarioUtils.createScenario(ConfigUtils.createConfig());
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(fileWithGeneralPlans);
        populationReader.readFile(fileWithAdditionalPlans);
        if (createSubpopulationAttributes) {
            createAttributes(scenario);
        }
        new PopulationWriter(scenario.getPopulation()).writeV6("output_merging_plans.xml.gz");
        logger.warning("Saved merging plans to output_merging_plans.xml.gz");
        new ObjectAttributesXmlWriter(scenario.getPopulation().getPersonAttributes()).writeFile("output_attributes_merging_plans.xml.gz");
        logger.warning("Saved merging attributes to output_attributes_merging_plans.xml.gz");
    }

    public static void run(Scenario scenario, String fileWithAdditionalPlans, boolean createSubpopulationAttributes) {
        PopulationReader populationReader = new PopulationReader(scenario);
        populationReader.readFile(fileWithAdditionalPlans);
        if(createSubpopulationAttributes) {
            createAttributes(scenario);
        }
    }
}
