/* *********************************************************************** *
 * project: kai
 * GautengRoadPricingScheme.java
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2012 by the members listed in the COPYING,        *
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

package org.matsim.roadpricingV2;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.population.Person;
import org.matsim.roadpricingV2.RoadPricingSchemeImpl.Cost;
import org.matsim.vehicles.Vehicle;

/**
 * @author nagel
 *
 */
public final class RoadPricingSchemeUsingTollFactor implements RoadPricingScheme {
	// needs to be public. kai, sep'14

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(RoadPricingSchemeUsingTollFactor.class ) ;

	private RoadPricingScheme delegate = null ;
	private final TollFactor tollFactor ;

	public RoadPricingSchemeUsingTollFactor(RoadPricingScheme scheme, TollFactor tollFactor ) {
		this.delegate = scheme;
		this.tollFactor = tollFactor;

	}

	public RoadPricingSchemeUsingTollFactor( String pricingSchemeFileName, TollFactor tollFactor ) {
		
		// read the road pricing scheme from file
		RoadPricingSchemeImpl scheme = new RoadPricingSchemeImpl();
		RoadPricingReaderXMLv2 rpReader = new RoadPricingReaderXMLv2(scheme);
		try {
			rpReader.readFile( pricingSchemeFileName  );
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		this.delegate = scheme ;
		this.tollFactor = tollFactor ;
				
	}

	@Override
	public String getDescription() {
		return delegate.getDescription();
	}

	@Override
	public Cost getLinkCostInfo(Id<Link> linkId, double time, Id<Person> personId, Id<Vehicle> vehicleId, String mode) {
		Cost baseToll = delegate.getLinkCostInfo(linkId, time, personId, vehicleId, mode);
		if (baseToll == null) {
			return null ;
		}
		final double tollFactorVal = tollFactor.getTollFactor(personId, vehicleId, linkId, time, mode);
		return new Cost( baseToll.startTime, baseToll.endTime, baseToll.amount * tollFactorVal, mode);
	}
	
	@Override
	public Cost getTypicalLinkCostInfo( Id<Link> linkId, double time, String mode) {
		return delegate.getTypicalLinkCostInfo(linkId, time, mode) ;
	}

	@Override
	public Set<Id<Link>> getTolledLinkIds() {
		return delegate.getTolledLinkIds();
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public String getType() {
		return delegate.getType();
	}

	@Override
	public Iterable<Cost> getTypicalCosts() {
		return delegate.getTypicalCosts() ;
	}

	@Override
	public Map<Id<Link>, List<Cost>> getTypicalCostsForLink() {
		return delegate.getTypicalCostsForLink() ;
	}

}
