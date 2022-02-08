package org.matsim.roadpricingV2;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.population.Leg;
import org.matsim.api.core.v01.population.Person;

import java.util.HashMap;
import java.util.Map;

abstract class LegInfo {
    private static Map<Id<Person>, Leg> currentLeg = new HashMap<>();
    static void addCurrentLeg(Id<Person> personId, Leg leg) {
        currentLeg.put(personId, leg);
    }
    static Leg getCurrentLeg(Id<Person> personId) {
        return currentLeg.get(personId);
    }
    static void reset() {
        currentLeg.clear();
    }
}
