# OsmMapPlayground
## Displays an open street map on android using hardcoded coordinates.  
## Overview

- uses the jit.io maven version of osmdroid and osmbonuspack
- currently uses hardcoded GPS Latitude and Longitude to draw map
- overlays a route on the map using values from run.gpx
  - run.gpx needs to be manually added to the files dir on the devices project file
  - example /data/data/osmMapPlayground/files/run.gpx
- points are added to the map at approximately 1 mile intervals

## Purpose
This application shows the basics of downloading and displaying a streetmap.  The streetmap is
overlayed with values parsed from a gpx file, run.gpx.  Points are added to the map at 1 mile
increments on the overlay.  Empty click listener and long press listener are defined for the
initial release.  The overlay is a capture of a round trip run from point A returning to point A.

## ToDo
- support click/long press events on points to display performance information from point to point
- visually indicate segements for outbound versus return splits


