package org.matsim.tools;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.PopulationUtils;
import org.matsim.core.router.DijkstraFactory;
import org.matsim.core.router.util.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CheckAndRepairRoutes {
    private static Logger log = Logger.getLogger(CheckAndRepairRoutes.class);
    private static int clearedRoutesCounts = 0;
    private static int clearedActivityLinks = 0;
    private static int fixedRoutesCounts = 0;
    public static void run(Scenario scenario) {
        clearedRoutesCounts = 0;
        clearedActivityLinks = 0;
        fixedRoutesCounts = 0;
        Network network = scenario.getNetwork();
        Population population = scenario.getPopulation();
        TravelDisutility travelDisutility = TravelDisutilityUtils.createFreespeedTravelTimeAndDisutility(scenario.getConfig().planCalcScore());
        TravelTime travelTime = TravelTimeUtils.createFreeSpeedTravelTime();
        LeastCostPathCalculator calc = new DijkstraFactory().createPathCalculator(network, travelDisutility, travelTime);

        cleaningNonexistentStartLinks(population, network);
        cleaningNonexistentLinksInRoute(population, network);
        repairRoutes(population, network, calc);
        cleaningRoutesWithBadStartAndEndLinks(population);
        log.warn("-----------------------");
        log.warn("Statistics of repaired");
        log.warn("Cleared Routes: "+clearedRoutesCounts);
        log.warn("Cleared Activity Links: "+clearedActivityLinks);
        log.warn("Fixed Routes: "+fixedRoutesCounts);
    }

    private static void cleaningNonexistentStartLinks(Population population, Network network) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                int indx = 0;
                for (Activity activity : PopulationUtils.getActivities(plan, null)) {
                    if (!network.getLinks().containsKey(activity.getLinkId())) {
                        activity.setLinkId(null);
                        clearedActivityLinks++;
                        PopulationUtils.getLegs(plan).get(indx).setRoute(null);
                        clearedRoutesCounts++;
                    }
                    indx++;
                }
            }
        }
    }

    private static void cleaningNonexistentLinksInRoute(Population population, Network network) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (Leg leg : PopulationUtils.getLegs(plan)) {
                    Route route = leg.getRoute();
                    if (route != null) {
                        List<Id<Link>> currentLinksInRouteList = Arrays.stream(route.getRouteDescription().split(" ")).map(Id::createLinkId).collect(Collectors.toList());
                        for (Id<Link> linkId : currentLinksInRouteList) {
                            if (!network.getLinks().containsKey(linkId)) {
                                route.setRouteDescription("");
                                leg.setRoute(null);
                                clearedRoutesCounts++;
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    private static void repairRoutes(Population population, Network network, LeastCostPathCalculator calc) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                for (Leg leg : PopulationUtils.getLegs(plan)) {
                    Route route = leg.getRoute();
                    if (route != null) {
                        List<Id<Link>> currentLinksInRouteList = Arrays.stream(route.getRouteDescription().split(" ")).map(Id::createLinkId).collect(Collectors.toList());
                        List<Id<Link>> newLinksInRouteList = new ArrayList<>();

                        for (Id<Link> linkId : currentLinksInRouteList) {
                            if (newLinksInRouteList.size() == 0) {
                                newLinksInRouteList.add(linkId);
                            } else {
                                Link prevLink = network.getLinks().get(newLinksInRouteList.get(newLinksInRouteList.size() - 1));
                                Node prevLinkToNode = prevLink.getToNode();
                                Link currLink = network.getLinks().get(linkId);
                                Node currLinkFromNode = currLink.getFromNode();
                                if (prevLinkToNode == currLinkFromNode) {
                                    newLinksInRouteList.add(linkId);
                                } else {
                                    List<Link> pathFromDijsktra = calc.calcLeastCostPath(prevLinkToNode, currLinkFromNode, 0, null, null).links;
                                    if (pathFromDijsktra != null && pathFromDijsktra.size() > 0) {
                                        log.warn("Dijkstra built path between links " + prevLink.getId() + " and " + currLink.getId());
                                        fixedRoutesCounts++;
                                        newLinksInRouteList.addAll(NetworkUtils.getLinkIds(pathFromDijsktra));
                                        newLinksInRouteList.add(currLink.getId());
                                    } else {
                                        log.error("Something went wrong! Path is null. Aborting...");
                                        System.exit(0);
                                    }
                                }
                            }
                        }
                        String newRouteDescription = newLinksInRouteList.stream().map(Object::toString).collect(Collectors.joining(" "));
                        route.setRouteDescription(newRouteDescription);
                    }
                }
            }
        }
    }

    private static void cleaningRoutesWithBadStartAndEndLinks(Population population) {
        for (Person person : population.getPersons().values()) {
            for (Plan plan : person.getPlans()) {
                int indx = 0;
                for(Leg leg : PopulationUtils.getLegs(plan)) {
                    Activity activity = PopulationUtils.getActivities(plan, null).get(indx);
                    if(activity.getLinkId()==null || leg.getRoute()==null || leg.getRoute().getStartLinkId()==null) {
                        continue;
                    } else {
                        if(activity.getLinkId()!=leg.getRoute().getStartLinkId()) {
                            clearedRoutesCounts++;
                            leg.setRoute(null);
                        }
                    }
                    indx++;
                }
            }
        }
    }
}
