OSMemory
========

Library for OSM data processing. It can be used for validators, extractors, etc.

When I created some validators, there were some requirements:

* Validators should be simple for write, installation and running
* They should work fast. They shouldn't use Postgres which requires long data loading.
* Since it should work fast, it should store data in memory

That's why this library was written.

Memory is cheap today, but it's impossible to load full planet in memory yet. Today it's enough for loading region.
If it will be required, there are some additional ways for memory usage optimization.

Some numbers about library for Belarus file processing:

* Data file: ~190 MiB o5m file from http://download.geofabrik.de/europe/belarus-latest.osm.pbf with up-to-date updates by osmupdate.
* Object counts: ~10 mln. nodes, ~1.5 mln. ways, ~15 ths. relations
* Loading time: ~8 seconds on the my old Core2 Duo E7500 2.93GHz box using jdk 1.8.0_20. It's ~4 times faster than o5m4j. Osmosis requires ~10 times more for load data from pbf file.
* Memory required: ~550 MiB heap

Comments are welcome to alex73mail@gmail.com

How to load data
----------------

Usually it can be done by 

    MemoryStorage data = new O5MReader().read(new File("tmp/belarus-updated.o5m"));

if you want to limit region, you can use bounding box:

    String borderWKT = FileUtils.readFileToString(new File("/Belarus.wkt));
    Area Belarus = Area.fromWKT(borderWKT);
    MemoryStorage data = new O5MReader(Belarus.getBoundingBox()).read(new File("tmp/belarus-updated.o5m"));

How to iterate by objects
-------------------------

Usually validators iterate by some set of object. It can be some tag-specific objects:

    // this code prints names of all cities
    storage.byTag("place", o -> System.out.println(o.getTag("name"));

allHasTag() function is very fast operation. If you can filter your object by having some tag, processing will be faster.

How to check area
-----------------

Most usable operation for validator is check "Is object relate to area ?". FastArea class is designed for that.
Idea is to split big polygon into ~400 parts, then check every part. Since most parts will be inside polygon, we can cache results and check enough fast.

     // this code prints names of all streets in city
     FastArea city = new FastArea(cityPolygon, storage);
     storage.byTag("highway", o -> city.contains(o), o -> System.out.println(o.getTag("name"));

Library uses JTS for some spatial calculations.
