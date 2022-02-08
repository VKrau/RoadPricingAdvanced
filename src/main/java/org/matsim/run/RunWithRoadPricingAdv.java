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
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.config.groups.PlanCalcScoreConfigGroup;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.controler.Controler;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.roadpricingV2.RoadPricingModule;

abstract public class RunWithRoadPricingAdv {

	public static void main(String[] args) {
		Config config = ConfigUtils.loadConfig("input/SPbTest/config.xml");
		config.controler().setLastIteration(10);
		PlanCalcScoreConfigGroup.ModeParams truck_ = new PlanCalcScoreConfigGroup.ModeParams("truck");
		truck_.setMarginalUtilityOfDistance(0.0);
		truck_.setMarginalUtilityOfTraveling(-6.0);
		truck_.setMode("truck");
		config.planCalcScore().addModeParams(truck_);
		Scenario scenario = ScenarioUtils.loadScenario(config);
		Controler controler = new Controler(scenario);
		controler.addOverridingModule(new AbstractModule() {
			@Override
			public void install() {
				this.install(new RoadPricingModule());
			}
		});
		controler.run();
	}

}