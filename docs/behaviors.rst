Behaviors
=========

Person Agents in BEAM exhibit several within-day behaviors that govern their use of the transportation system.

Mode Choice
-----------

The most prominent behavior is mode choice. Mode choice can be specified either exogensously as a field in the persons plans, or it can be selected during replanning, or it can remain unset and be selected within the day.

Within day mode choice is selected based on the attributes of the first trip of each tour. Once a mode is selected for the tour, the person attempts to stick with that mode for the duration of the tour. 

In all cases (whether mode is specified before the day or chosen within the day) person agents use WALK as a fallback option throughout if constraints otherwise prevent their previously determined mode from being possible for any given trip. E.g. if a person is in the middle of a RIDE_HAIL tour, but the Ride Hail Manager is unable to match a driver to the person, then the person will walk.

In BEAM the following modes are considered:

* Walk
* Bike
* Drive (alone)
* Walk to Transit
* Drive to Transit (Park and Ride)
* Ride Hail (Solo, Pooled)
* Ride Hail to/from Transit

There are two mode choice models that are possible within BEAM. 

Multinomial Logit Mode Choice
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The first is a simple multinomial logit choice model that has the following form for modal alternative j:

V_j = ASC_j + Beta_cost * cost + Beta_time * time + Beta_xfer * num_transfers + Beta_occup * transit_occupancy

The ASC (alternative specific constant) parameters as well as the Beta parameters can be configured in the BEAM configuration file and default to the following values:

::

    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.cost = -1.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.time = -0.0047
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.transfer = -1.4
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.car_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.walk_transit_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.drive_transit_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.ride_hail_transit_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.ride_hail_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.walk_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.bike_intercept = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.transit_crowding = 0.0
    beam.agentsim.agents.modalBehaviors.multinomialLogit.params.transit_crowding_percentile = 90

Latent Class Mode Choice
~~~~~~~~~~~~~~~~~~~~~~~~

Parking
-------

In BEAM, parking is issued at the granularity of a Traffic Analysis Zone (TAZ) or a Link. Upon initialization, parking alternatives are read in from the CSV file listed in the BEAM config parameter *beam.agentsim.taz.parking*. Each row identifies the attributes of a parking alternative for a given TAZ, of which a given combination of attributes should be unique. Parking attributes include the following:

+---------------------+----------------------------------------------+
| attribute           | values                                       |
+=====================+==============================================+
| *parkingType*       | Workplace, Public, Residential               |
+---------------------+----------------------------------------------+
| *pricingModel*      | FlatFee, Block                               |
+---------------------+----------------------------------------------+
| *chargingPointType* | NoCharger, Level1, Level2, DCFast, UltraFast |
+---------------------+----------------------------------------------+
| *numStalls*         | *integer*                                    |
+---------------------+----------------------------------------------+
| *feeInCents*        | *integer*                                    |
+---------------------+----------------------------------------------+
| *reservedFor*       | Any, RideHailManager                         |
+---------------------+----------------------------------------------+

BEAM agents seek parking mid-tour, from within a leg of their trip. A search is run which starts at the trip destination and expands outward, seeking to find the closest TAZ centers with increasing search radii. Agents will pick the closest and cheapest parking alternative with attributes which match their use case.

The following should be considered when configuring a set of parking alternatives. The default behavior is to provide a nearly unbounded number of parking stalls for each combination of attributes, per TAZ, for the public, and provide no parking alternatives for ride hail agents. This behavior can be overridden manually by providing replacement values in the parking configuration file. Parking which is *reservedFor* a RideHailManager should only appear as *Workplace* parking. Free parking can be instantiated by setting *feeInCents* to zero. *numStalls* should be non-negative. Charging behavior is currently implemented for ride hail agents only.

the *chargingPointType* attribute will result in the following charger power in kW:

+----------------+--------+
| *chargingPointType* | kW|
+================+========+
| NoCharger      | 0.0    |
+----------------+--------+
| Level1         | 1.5    |
+----------------+--------+
| Level2         | 6.7    |
+----------------+--------+
| DCFast         | 50.0   |
+----------------+--------+
| UltraFast      | 250.0  |
+----------------+--------+

Refueling
---------

Refueling is based on remaining range of the vehicle. In order to control refueling behavior one need to modify parameters
in the following spaces

#. beam.agentsim.agents.vehicles.enroute
#. beam.agentsim.agents.vehicles.destination
#. beam.agentsim.agents.rideHail.human
#. beam.agentsim.agents.rideHail.cav

See :ref:`model-inputs` for more information about these parameters.

Reposition
----------

In BEAM, reposition of shared vehicles is based on availability. minAvailabilityMap stores the TAZs with lowest availability of vehicles, and we reposition the number of matchLimit vehicles from the TAZs with available fleets more than matchLimit based on statTimeBin and repositionTimeBin to determine when we start doing reposition and the frequency of repositioning.

There are several parameters we can adjust in repositioning:

+----------------+--------------------------------------------------+
| Parameters          | Meaning                                     |
+================+==================================================+
| *matchLimit*        | How many vehicles we want to reposition     |
+----------------+--------------------------------------------------+
| *repositionTimeBin* | How often we do repositioning               |
+----------------+--------------------------------------------------+
| *statTimeBin*       | When do we start repositioning              |
+----------------+--------------------------------------------------+

