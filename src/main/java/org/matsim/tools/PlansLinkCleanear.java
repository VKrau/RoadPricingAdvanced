package org.matsim.tools;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.population.*;
import org.matsim.core.population.PopulationUtils;

import javax.swing.text.html.HTMLDocument;
import java.util.Iterator;
import java.util.Map;

public class PlansLinkCleanear {
    private final Network network;
    private final Population population;

    public PlansLinkCleanear(Network network, Population population) {
        this.network = network;
        this.population = population;
    }

    public void cleanLinkIdsFromPopulation(){

       for (Person person : population.getPersons().values()){
           for(Plan plan : person.getPlans()) {
               for(Activity activity : PopulationUtils.getActivities(plan, null)) {
                   if(!network.getLinks().containsKey(activity.getLinkId())) {
                       activity.setLinkId(null);
                   }
               }
           }
//           person.getPlans().stream().forEach(plan-> {
//               ((Plan) plan).getPlanElements().stream().filter(a->a instanceof Activity)
//                       .map(a->(Activity)a).forEach(a -> a.setLinkId(null));
//           });

       }
    }
}

