package org.matsim.roadpricingV2;

import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.AbstractModule;
import org.matsim.core.router.costcalculators.RandomizingTimeDistanceTravelDisutilityFactory;
import org.matsim.roadpricingV2.ControlerDefaultsWithRoadPricingModule.RoadPricingInitializer;
import org.matsim.roadpricingV2.ControlerDefaultsWithRoadPricingModule.RoadPricingSchemeProvider;
import org.matsim.roadpricingV2.ControlerDefaultsWithRoadPricingModule.TravelDisutilityIncludingTollFactoryProvider;

import com.google.inject.Singleton;

public final class RoadPricingModule extends AbstractModule {
	private RoadPricingScheme scheme;

	public RoadPricingModule() {

	}

	public RoadPricingModule( RoadPricingScheme scheme ) {
		this.scheme = scheme;

	}

	@Override
	public void install() {
		ConfigUtils.addOrGetModule(getConfig(), RoadPricingConfigGroup.GROUP_NAME, RoadPricingConfigGroup.class);
		// TODO sort out different ways to set toll schemes; reduce automagic
		if ( scheme != null) {
			// scheme has come in from the constructor, use that one:
			bind(RoadPricingScheme.class).toInstance(scheme);
		} else {
			// no scheme has come in from the constructor, use a class that reads it from file:
			bind(RoadPricingScheme.class).toProvider(RoadPricingSchemeProvider.class).in(Singleton.class);
		}
		// also add RoadPricingScheme as ScenarioElement.  yyyy TODO might try to get rid of this; binding it is safer
		bind(RoadPricingInitializer.class).asEagerSingleton();

		// add the toll to the routing disutility for all modes from MainModes.  also includes "randomizing":
		for(String mode : getConfig().qsim().getMainModes()) {
			addTravelDisutilityFactoryBinding(mode).toProvider(TravelDisutilityIncludingTollFactoryProvider.class).asEagerSingleton();
		}

		// specific re-routing strategy for area toll:
		// yyyy TODO could probably combine them somewhat
		bind(PlansCalcRouteWithTollOrNot.class);
		addPlanStrategyBinding("ReRouteAreaToll").toProvider(ReRouteAreaToll.class).asEagerSingleton();
		for(String mode : getConfig().qsim().getMainModes()) {
			String tollMode = mode+"_with_payed_area_toll";
			addTravelDisutilityFactoryBinding(tollMode).toInstance(new RandomizingTimeDistanceTravelDisutilityFactory(mode, getConfig().planCalcScore()));
			addRoutingModuleBinding(tollMode).toProvider(new RoadPricingNetworkRouting(mode));
		}
		
		// yyyy TODO It might be possible that the area stuff is adequatly resolved by the randomizing approach.  Would need to try 
		// that out.  kai, sep'16

		// this is what makes the mobsim compute tolls and generate money events
		// TODO yyyy could probably combine the following two:
		addControlerListenerBinding().to(RoadPricingControlerListener.class);
		bind(CalcPaidToll.class).in(Singleton.class);
		// this is for analysis only:
		bind(CalcAverageTolledTripLength.class).in(Singleton.class);

	}
}