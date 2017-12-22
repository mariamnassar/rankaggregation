# MaSyMoS rank aggregation add-on

This describes a rank aggregation add-on for a models retrieval client (MORRE) in a biological graph database (MaSyMoS).
In *documentationTex/RankAggregationMethods.tex*, the used rank aggregation methods and algorithms are described:
  * **Adjacent Pairs (a modified version)**
  * **CombMNZ**
  * **Local Kemenization**
  * **Supervised Local Kemenization**

These methods have been adapted for our use case to make the rank aggregation faster.

Furthermore, **RankerHandler** has been used. This is a data structure that makes dealing with rankers
easier and faster for rank aggregation proposes. This is described in *documentationTex/RankerHandler.tex*.

The rank aggregation code has been integrated into MaSyMoS core/morre. 

The runnable MaSyMoS core and morre including the rank aggregation add-on can be downloaded at the following links:

https://github.com/ronhenkel/masymos-core/releases/tag/v0.9-rankAggregation

https://github.com/ronhenkel/masymos-morre/releases/tag/v0.9-rankAggregation

