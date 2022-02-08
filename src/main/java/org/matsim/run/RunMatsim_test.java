/* *********************************************************************** *
 * project: org.matsim.*												   *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */
package org.matsim.run;



import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.population.Person;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.roadpricingV2.RoadPricingModule;
import org.matsim.tools.CheckAndRepairRoutes;
import org.matsim.tools.MergingPlans;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;



import java.io.IOException;


abstract public class RunMatsim_test {

	private static boolean checkAndRepairLinksAndRoutes = true;
	private static boolean mergeWithTrucks = false;
	private static boolean disableReRoute = false;


	public static void main(String[] args) throws IOException {
		Config config = ConfigUtils.loadConfig("input/truckTest/config_2019_car_truck.xml");

		if (disableReRoute){
			int lastIteration = config.controler().getLastIteration();
			config.strategy().getStrategySettings()
					.stream()
					.filter(strategy -> strategy.getStrategyName().contains("ReRoute"))
					.forEach(i -> i.setDisableAfter(lastIteration-10));
		}

		config.controler().setLastIteration(0);

		PlanCalcScoreConfigGroup.ModeParams truck2_ = new PlanCalcScoreConfigGroup.ModeParams("truck2");
		truck2_.setMarginalUtilityOfDistance(0.0);
		truck2_.setMarginalUtilityOfTraveling(-6.0);
		truck2_.setMode("truck2");
		config.planCalcScore().addModeParams(truck2_);

		PlanCalcScoreConfigGroup.ModeParams truck3_ = new PlanCalcScoreConfigGroup.ModeParams("truck3");
		truck3_.setMarginalUtilityOfDistance(0.0);
		truck3_.setMarginalUtilityOfTraveling(-6.0);
		truck3_.setMode("truck3");
		config.planCalcScore().addModeParams(truck3_);

		PlanCalcScoreConfigGroup.ModeParams truck4_ = new PlanCalcScoreConfigGroup.ModeParams("truck4");
		truck4_.setMarginalUtilityOfDistance(0.0);
		truck4_.setMarginalUtilityOfTraveling(-6.0);
		truck4_.setMode("truck4");
		config.planCalcScore().addModeParams(truck4_);


		Scenario scenario = ScenarioUtils.loadScenario(config);

		if (mergeWithTrucks){
			MergingPlans.run(scenario,"input/plans_trucks.xml.gz", false);
		}


		Controler controler = new Controler(scenario);

		config.controler().setOutputDirectory(config.controler().getOutputDirectory() +
				LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME).split(":")[0] + "_" +
				"output_car_truck_road_price_adv");

		for(Person person : scenario.getPopulation().getPersons().values()) {
			Plan selectedPlan = person.getSelectedPlan();
			person.getPlans().clear();
			person.addPlan(selectedPlan);
			person.setSelectedPlan(selectedPlan);
		}

		if (checkAndRepairLinksAndRoutes){
			CheckAndRepairRoutes.run(scenario);
		}

		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.install(new RoadPricingModule());
			}
		});



		controler.run();
	}

}