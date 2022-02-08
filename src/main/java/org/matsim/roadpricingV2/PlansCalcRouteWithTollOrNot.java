/*
 *  *********************************************************************** *
 *  * project: org.matsim.*
 *  * PlansCalcRouteWithTollOrNot.java
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  * copyright       : (C) 2015 by the members listed in the COPYING, *
 *  *                   LICENSE and WARRANTY file.                            *
 *  * email           : info at matsim dot org                                *
 *  *                                                                         *
 *  * *********************************************************************** *
 *  *                                                                         *
 *  *   This program is free software; you can redistribute it and/or modify  *
 *  *   it under the terms of the GNU General Public License as published by  *
 *  *   the Free Software Foundation; either version 2 of the License, or     *
 *  *   (at your option) any later version.                                   *
 *  *   See also COPYING, LICENSE and WARRANTY file                           *
 *  *                                                                         *
 *  * *********************************************************************** 
 */

package org.matsim.roadpricingV2;

import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Plan;
import org.matsim.api.core.v01.population.PlanElement;
import org.matsim.core.population.algorithms.PlanAlgorithm;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.router.PlanRouter;
import org.matsim.core.router.TripRouter;
import org.matsim.core.router.costcalculators.TravelDisutilityFactory;
import org.matsim.core.router.util.TravelTime;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

class PlansCalcRouteWithTollOrNot implements PlanAlgorithm {

   private RoadPricingScheme roadPricingScheme;
   private Provider<TripRouter> tripRouterFactory;

   @Inject
   PlansCalcRouteWithTollOrNot(RoadPricingScheme roadPricingScheme, Provider<TripRouter> tripRouterFactory, Map<String, TravelDisutilityFactory> travelDisutilityFactory, Map<String, TravelTime> travelTime) {
       this.roadPricingScheme = roadPricingScheme;
       this.tripRouterFactory = tripRouterFactory;
   }

   @Override
   public void run(final Plan plan) {
       handlePlan(plan);
   }

   private void handlePlan(Plan plan) {
       System.out.println("look");
       System.exit(0);
       // This calculates a best-response plan from the two options, paying area toll or not.
       // From what I understand, it may be simpler/better to just throw a coin and produce
       // one of the two options.
       replaceCarModeWithTolledCarMode(plan);
       PlanRouter untolledPlanRouter = new PlanRouter(tripRouterFactory.get());
       untolledPlanRouter.run(plan);
       double areaToll = roadPricingScheme.getTypicalCosts().iterator().next().amount;
       double routeCostWithAreaToll = sumNetworkModeCosts(plan) + areaToll;
       replaceTolledCarModeWithCarMode(plan);
       new PlanRouter(tripRouterFactory.get()).run(plan);
       double routeCostWithoutAreaToll = sumNetworkModeCosts(plan);
       if (routeCostWithAreaToll < routeCostWithoutAreaToll) {
           replaceCarModeWithTolledCarMode(plan);
           untolledPlanRouter.run(plan);
       }
   }

   private void replaceCarModeWithTolledCarMode(Plan plan) {
       for (PlanElement planElement : plan.getPlanElements()) {
           if (planElement instanceof Leg) {
               if (!((Leg) planElement).getMode().contains("_with_payed_area_toll")) {
                   String mode = ((Leg) planElement).getMode();
                   ((Leg) planElement).setMode(mode);
               }
           }
       }
   }

   private void replaceTolledCarModeWithCarMode(Plan plan) {
       for (PlanElement planElement : plan.getPlanElements()) {
           if (planElement instanceof Leg) {
               if (((Leg) planElement).getMode().contains("_with_payed_area_toll")) {
                   String mode = ((Leg) planElement).getMode().split("_with_payed_area_toll")[0];
                   ((Leg) planElement).setMode(mode);
               }
           }
       }
   }

   private double sumNetworkModeCosts(Plan plan) {
       double sum = 0.0;
       for (PlanElement planElement : plan.getPlanElements()) {
           if (planElement instanceof Leg) {
               Leg leg = (Leg) planElement;
               if (leg.getRoute() instanceof NetworkRoute) {
                   NetworkRoute networkRoute = (NetworkRoute) leg.getRoute();
                   sum += networkRoute.getTravelCost();
               }
           }
       }
       return sum;
   }

}